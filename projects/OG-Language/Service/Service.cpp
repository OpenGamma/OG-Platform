/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "JVM.h"
#include "Settings.h"
#include "ConnectionPipe.h"
#include "Service.h"

LOGGING(com.opengamma.language.service.Service);

#define SERVICE_STATE_RUNNING	0
#define SERVICE_STATE_STOPPING	1
#define SERVICE_STATE_STOPPED	2

/// Mutex to guard access to other global variables.
static CMutex g_oMutex;

/// JVM instance running the OpenGamma Java stack
static CJVM *g_poJVM = NULL;

/// Source of incoming connections from client processes
static CConnectionPipe *g_poPipe = NULL;

/// Timeout in milliseconds to wait for the asynchronous JVM operations before giving
/// feedback to the user (e.g. logging or reporting to a service control manager).
static unsigned long g_lBusyTimeout = 0;

#ifdef _WIN32
/// Service status handle for reporting to the SCM.
static SERVICE_STATUS_HANDLE g_hServiceStatus = NULL;

/// Status checkpoint number for reporting to the SCM. See the Windows SDK documentation for more information.
static DWORD g_dwServiceCheckPoint = 0;
#endif /* ifdef _WIN32 */

/// Service status; one of SERVICE_STATE_RUNNING, SERVICE_STATE_STOPPING or SERVICE_STATE_STOPPED.
static volatile int g_nServiceState = SERVICE_STATE_STOPPED;

#ifdef _WIN32
/// Report the service state back to the service control manager.
///
/// @param[in] dwStateCode service state code
/// @param[in] dwExitCode service exit code (if applicable, use 0 otherwise)
/// @param[in] bInfo TRUE to log at INFO level, FALSE to log at DEBUG only
/// @param[in] pszLabel textual description of the state to log, never NULL
static void _ReportState (DWORD dwStateCode, DWORD dwExitCode, bool bInfo, PCTSTR pszLabel) {
	SERVICE_STATUS sta;
	ZeroMemory (&sta, sizeof (sta));
	if (bInfo) {
		LOGINFO (pszLabel);
	} else {
		LOGDEBUG (pszLabel);
	}
	switch (dwStateCode) {
	case SERVICE_START_PENDING :
		sta.dwCheckPoint = ++g_dwServiceCheckPoint;
		break;
	case SERVICE_RUNNING :
		sta.dwControlsAccepted = SERVICE_ACCEPT_STOP;
		break;
	case SERVICE_STOP_PENDING :
		sta.dwCheckPoint = ++g_dwServiceCheckPoint;
		break;
	case SERVICE_STOPPED :
		break;
	default :
		LOGFATAL (TEXT ("Unexpected service state code ") << dwStateCode);
		break;
	}
	if (g_hServiceStatus) {
		sta.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
		sta.dwCurrentState = dwStateCode;
		sta.dwWin32ExitCode = dwExitCode;
		sta.dwWaitHint = g_lBusyTimeout * 2;
		SetServiceStatus (g_hServiceStatus, &sta);
	}
}
#else
/// Report the service state back to the user (by writing to the logs).
///
/// @param[in] bInfo TRUE to log at INFO level, FALSE to log at DEBUG only
/// @param[in] pszLabel textual description of the state to log, never NULL
static void _ReportStateImpl (bool bInfo, const TCHAR *pszLabel) {
	if (bInfo) {
		LOGINFO (pszLabel);
	} else {
		LOGDEBUG (pszLabel);
	}
}
#define _ReportState(_win32state, _win32exitCode, _info, _label) _ReportStateImpl (_info, _label)
#endif

/// Reports a starting state to the user. This may happen any number of times (or not at all) before
/// the service enters its running state.
static void _ReportStateStarting () {
	_ReportState (SERVICE_START_PENDING, 0, false, TEXT ("Service starting"));
	g_nServiceState = SERVICE_STATE_STOPPED;
}

/// Reports a running state to the user.
static void _ReportStateRunning () {
	_ReportState (SERVICE_RUNNING, 0, true, TEXT ("Service started"));
	g_nServiceState = SERVICE_STATE_RUNNING;
}

/// Reports a stopping state to the user. This may happen any number of times (or not at all) before
/// the service enters its stopped state.
static void _ReportStateStopping () {
	_ReportState (SERVICE_STOP_PENDING, 0, false, TEXT ("Service stopping"));
	g_nServiceState = SERVICE_STATE_STOPPING;
}

/// Reports a stopped state to the user.
static void _ReportStateStopped () {
	_ReportState (SERVICE_STOPPED, 0, true, TEXT ("Service stopped"));
	g_nServiceState = SERVICE_STATE_STOPPED;
}

/// Reports an errored state to the user.
static void _ReportStateErrored () {
	_ReportState (SERVICE_STOPPED, ERROR_INVALID_ENVIRONMENT, true, TEXT ("Service stopped"));
	g_nServiceState = SERVICE_STATE_STOPPED;
}

/// Attempts to stop the service.
///
/// @param[in] bForce TRUE to stop the service immediately by closing the IPC, FALSE to initiate a
/// lazy close when the IPC goes idle.
void ServiceStop (bool bForce) {
	g_oMutex.Enter ();
	if (bForce) {
		_ReportStateStopping ();
		if (g_poPipe) {
			g_poPipe->Close ();
		} else {
			LOGINFO (TEXT ("Pipe already destroyed at hard stop"));
		}
	} else {
		if (g_poPipe) {
			g_poPipe->LazyClose ();
		} else {
			LOGINFO (TEXT ("Pipe already destroyed at soft stop"));
		}
	}
	g_oMutex.Leave ();
}

/// Breaks the service. This is provided for the unit tests only to simulate a broken JVM or service and
/// test the recovery mechanism. Do not call it intentionally otherwise.
void ServiceSuspend () {
	_ReportStateStopping ();
	g_oMutex.Enter ();
	if (g_poPipe) {
		g_poPipe->Close ();
	} else {
		LOGERROR (TEXT ("Pipe destroyed at suspend"));
	}
	// Never leave the critical section - this function is designed specifically to ruin the
	// execution of the service to test a hung JVM. IT IS NOT THE WINDOWS SERVICE SUSPEND/RESUME.
}

#ifdef _WIN32
/// Win32 service signal handler. Responds to the STOP request only.
///
/// @param[in] dwAction signal to handle
static void WINAPI _SignalHandler (DWORD dwAction) {
	switch (dwAction) {
	case SERVICE_CONTROL_STOP :
		LOGINFO (TEXT ("STOP signal received from SCM"));
		ServiceStop (TRUE);
		break;
	case SERVICE_CONTROL_INTERROGATE :
		LOGDEBUG (TEXT ("INTERROGATE signal received from SCM"));
		break;
	default :
		LOGWARN (TEXT ("Unrecognised signal ") << dwAction << TEXT (" received from SCM"));
		break;
	}
}
#else /* ifdef _WIN32 */
/// Posix signal handler to catch the TERM signal.
///
/// @param[in] nSignal the signal number to handle
static void _SignalHandler (int nSignal) {
	switch (nSignal) {
	case SIGTERM :
		LOGINFO (TEXT ("TERM signal received from O/S"));
		ServiceStop (TRUE);
		break;
	default :
		LOGWARN (TEXT ("Unrecognised signal ") << nSignal << TEXT (" received from O/S"));
		break;
	}
}
#endif /* ifdef _WIN32 */

/// Prelude actions to start up the service, e.g. to set any global variables from the settings or perform
/// any system specific actions. E.g. the Windows implementation registers with the service control manager
/// and can optionally set the security descriptor on the process to allow clients to kill/restart it.
///
/// @param[in] nReason how the startup is occuring (e.g. SERVICE_RUN_INLINE) - different actions may be
/// required depending on whether the code is running direct from main() or through another mechansim
static void _ServiceStartup (int nReason) {
	CSettings oSettings;
#ifdef _WIN32
	if (nReason == SERVICE_RUN_SCM) {
		g_hServiceStatus = RegisterServiceCtrlHandler (oSettings.GetServiceName (), _SignalHandler);
	}
	PCTSTR pszSDDL = oSettings.GetServiceSDDL ();
	if (pszSDDL) {
		LOGDEBUG (TEXT ("Setting security descriptor ") << pszSDDL);
		PSECURITY_DESCRIPTOR psdRelative;
		if (ConvertStringSecurityDescriptorToSecurityDescriptor (pszSDDL, SDDL_REVISION_1, &psdRelative, NULL)) {
			DWORD cbAbsolute = 1024;
			PSECURITY_DESCRIPTOR psdAbsolute = (PSECURITY_DESCRIPTOR)malloc (cbAbsolute);
			DWORD cbD = 1024;
			PACL paclD = (PACL)malloc (cbD);
			DWORD cbS = 1024;
			PACL paclS = (PACL)malloc (cbS);
			DWORD cbOwner = 1024;
			PSID psidOwner = (PSID)malloc (cbOwner);
			DWORD cbPGroup = 1024;
			PSID psidPGroup = (PSID)malloc (cbPGroup);
			if (MakeAbsoluteSD (psdRelative, psdAbsolute, &cbAbsolute, paclD, &cbD, paclS, &cbS, psidOwner, &cbOwner, psidPGroup, &cbPGroup)) {
				DWORD dwError = SetSecurityInfo (GetCurrentProcess (), SE_KERNEL_OBJECT, DACL_SECURITY_INFORMATION, NULL, NULL, paclD, NULL);
				if (dwError == ERROR_SUCCESS) {
					LOGINFO (TEXT ("Security descriptor set on process handle"));
				} else {
					LOGWARN (TEXT ("Couldn't set security descriptor on process handle, error ") << GetLastError ());
				}
				if (nReason == SERVICE_RUN_SCM) {
					SC_HANDLE hSCM = OpenSCManager (NULL, NULL, GENERIC_READ);
				    if (hSCM) {
						SC_HANDLE hService = OpenService (hSCM, oSettings.GetServiceName (), GENERIC_WRITE | WRITE_DAC);
						if (hService) {
							dwError = SetSecurityInfo (hService, SE_SERVICE, DACL_SECURITY_INFORMATION, NULL, NULL, paclD, NULL);
							if (dwError == ERROR_SUCCESS) {
								LOGINFO (TEXT ("Security descriptor set on service"));
							} else {
								LOGWARN (TEXT ("Couldn't set security descriptor on service, error ") << GetLastError ());
							}
							CloseServiceHandle (hService);
						} else {
							LOGWARN (TEXT ("Couldn't open service, error ") << GetLastError ());
						}
						CloseServiceHandle (hSCM);
					} else {
						LOGWARN (TEXT ("Couldn't open SCM, error ") << GetLastError ());
					}
				}
			} else {
				LOGWARN (TEXT ("Couldn't create absolute security description, error ") << GetLastError ());
			}
			free (psdAbsolute);
			free (paclD);
			free (paclS);
			free (psidOwner);
			free (psidPGroup);
			LocalFree (psdRelative);
		} else {
			LOGWARN (TEXT ("Couldn't parse SDDL ") << pszSDDL << TEXT (", error ") << GetLastError ());
		}
	} else {
		LOGDEBUG (TEXT ("No security descriptor specified"));
	}
#else /* ifdef _WIN32 */
	if (nReason == SERVICE_RUN_DAEMON) {
		const TCHAR *pszPID = oSettings.GetPidFile ();
		if (pszPID) {
			LOGDEBUG (TEXT ("Setting signal handler"));
			sigset (SIGTERM, _SignalHandler);
			LOGINFO (TEXT ("Creating PID file ") << pszPID);
			FILE *f = fopen (pszPID, "wt");
			if (f) {
				fprintf (f, "%d", getpid ());
				fclose (f);
			} else {
				LOGWARN (TEXT ("Couldn't write to PID file ") << pszPID << TEXT (", error ") << GetLastError ());
			}
		} else {
			LOGWARN (TEXT ("No PID file"));
		}
	}
#endif /* ifdef _WIN32 */
	g_lBusyTimeout = oSettings.GetBusyTimeout ();
	_ReportStateStarting ();
}

/// Exitlude actions to stop the service, e.g. to remove any state that was created as part of _ServiceStartup.
///
/// @param[in] nReason how the service was run (e.g. SERVICE_RUN_INLINE) - different actions may be required
/// depending on whether the code is running direct from main() or through another mechanism.
static void _ServiceStop (int nReason) {
	CSettings oSettings;
#ifndef _WIN32
	if (nReason == SERVICE_RUN_DAEMON) {
		const TCHAR *pszPID = oSettings.GetPidFile ();
		if (pszPID) {
			LOGINFO (TEXT ("Removing PID file ") << pszPID);
			unlink (pszPID);
		}
	}
#endif /* ifndef _WIN32 */
}

/// Run the service, returning when it has stopped.
///
/// @param[in] nReason how the service is running, e.g. SERVICE_RUN_INLINE, in case actions are different depending
/// on how it was started.
void ServiceRun (int nReason) {
	_ServiceStartup (nReason);
	{
		CErrorFeedback oServiceErrors;
		g_poJVM = CJVM::Create (&oServiceErrors);
		if (!g_poJVM) {
			LOGERROR (TEXT ("Couldn't create JVM"));
			_ReportStateErrored ();
			_ServiceStop (nReason);
			return;
		}
		g_poJVM->Start (&oServiceErrors);
	}
	g_poPipe = CConnectionPipe::Create ();
	if (!g_poPipe) {
		LOGERROR (TEXT ("Couldn't create IPC pipe"));
	}
	while (g_poJVM->IsBusy (g_lBusyTimeout)) {
		_ReportStateStarting ();
	}
	if (g_poPipe && g_poJVM->IsRunning ()) {
		_ReportStateRunning ();
		do {
			LOGDEBUG (TEXT ("Waiting for user connection"));
			ClientConnect *pcc = g_poPipe->ReadMessage ();
			if (pcc) {
				LOGINFO (TEXT ("Connection received from ") << pcc->_userName);
				LOGDEBUG (TEXT ("C++ -> Java = ") << pcc->_CPPToJavaPipe);
				LOGDEBUG (TEXT ("Java -> C++ = ") << pcc->_JavaToCPPPipe);
				// TODO [PLAT-1117] Use challenge/response to verify the user name
				g_poJVM->UserConnection (pcc->_userName, pcc->_CPPToJavaPipe, pcc->_JavaToCPPPipe, pcc->_languageID);
				ClientConnect_free (pcc);
				if (!g_poJVM->IsStopped ()) {
					g_poPipe->CancelLazyClose ();
					if (g_poJVM->IsStopped ()) {
						// Stop might have occurred between the check and the cancel, so restore the cancel
						ServiceStop (false);
					}
				}
				g_oMutex.Enter ();
				if (g_poPipe->IsClosed ()) {
					delete g_poPipe;
					if (g_nServiceState == SERVICE_STATE_RUNNING) {
						LOGINFO (TEXT ("Pipe closed with pending connection - reopening"));
						g_poPipe = CConnectionPipe::Create ();
						if (!g_poPipe) {
							LOGERROR (TEXT ("Couldn't create IPC pipe"));
						}
					} else {
						LOGINFO (TEXT ("Ignoring pending connections after hard close"));
						g_poPipe = NULL;
					}
					if (!g_poPipe) {
						g_oMutex.Leave ();
						LOGINFO (TEXT ("Shutting down JVM after pipe close"));
						g_poJVM->Stop ();
						break;
					}
				}
				g_oMutex.Leave ();
			} else {
				LOGERROR (TEXT ("Shutting down JVM after failing to read from pipe"));
				g_poJVM->Stop ();
				break;
			}
		} while (!g_poJVM->IsBusy (g_lBusyTimeout) && g_poJVM->IsRunning ());
		_ReportStateStopping ();
		while (g_poJVM->IsBusy (g_lBusyTimeout)) {
			_ReportStateStopping ();
		}
		_ReportStateStopped ();
	} else {
		_ReportStateErrored ();
	}
	if (g_poPipe) {
		delete g_poPipe;
		g_poPipe = NULL;
	}
	delete g_poJVM;
	g_poJVM = NULL;
	_ServiceStop (nReason);
}

/// Tests if the service is running or not.
///
/// @return TRUE if the service is running, FALSE otherwise
bool ServiceRunning () {
	return (g_nServiceState != SERVICE_STATE_STOPPED);
}

/// Configure the service.
void ServiceConfigure () {
	CErrorFeedback oFeedback;
	g_poJVM = CJVM::Create (&oFeedback);
	if (!g_poJVM) {
		LOGERROR (TEXT ("Couldn't create JVM"));
		return;
	}
	bool bRestart = g_poJVM->Configure ();
	delete g_poJVM;
	g_poJVM = NULL;
	if (bRestart) {
		CSettings oSettings;
#ifdef _WIN32
		PCTSTR pszServiceName = oSettings.GetServiceName ();
		SC_HANDLE hSCM = OpenSCManager (NULL, NULL, GENERIC_READ);
		if (hSCM) {
			SC_HANDLE hService = OpenService (hSCM, pszServiceName, SERVICE_STOP);
			if (hService) {
				SERVICE_STATUS ss;
				if (ControlService (hService, SERVICE_CONTROL_STOP, &ss)) {
					LOGINFO (TEXT ("Stopped service ") << pszServiceName);
				} else {
					LOGWARN (TEXT ("Couldn't stop ") << pszServiceName << TEXT (", error ") << GetLastError ());
				}
				CloseServiceHandle (hService);
			} else {
				LOGWARN (TEXT ("Couldn't open ") << pszServiceName << TEXT (" service to restart"));
			}
			CloseServiceHandle (hSCM);
		} else {
			LOGWARN (TEXT ("Couldn't connect to service manager, error ") << GetLastError ());
		}
#else /* ifdef _WIN32 */
		// TODO: kill the service runner process
#endif /* ifdef _WIN32 */
	}
}
