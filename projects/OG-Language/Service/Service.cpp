/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Main service control functions

#include "JVM.h"
#include "Settings.h"
#include "ConnectionPipe.h"
#include "Service.h"

LOGGING(com.opengamma.language.service.Service);

static CMutex g_oMutex;
static CJVM *g_poJVM = NULL;
static CConnectionPipe *g_poPipe = NULL;
static unsigned long g_lBusyTimeout = 0;
#ifdef _WIN32
static SERVICE_STATUS_HANDLE g_hServiceStatus = NULL;
static DWORD g_dwServiceCheckPoint = 0;
#endif /* ifdef _WIN32 */
static volatile bool g_bServiceRunning = false;

#ifdef _WIN32
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
static void _ReportStateImpl (bool bInfo, const TCHAR *pszLabel) {
	if (bInfo) {
		LOGINFO (pszLabel);
	} else {
		LOGDEBUG (pszLabel);
	}
}
#define _ReportState(_win32state, _win32exitCode, _info, _label) _ReportStateImpl (_info, _label)
#endif

static void _ReportStateStarting () {
	_ReportState (SERVICE_START_PENDING, 0, false, TEXT ("Service starting"));
	g_bServiceRunning = false;
}

static void _ReportStateRunning () {
	_ReportState (SERVICE_RUNNING, 0, true, TEXT ("Service started"));
	g_bServiceRunning = true;
}

static void _ReportStateStopping () {
	_ReportState (SERVICE_STOP_PENDING, 0, false, TEXT ("Service stopping"));
	g_bServiceRunning = false;
}

static void _ReportStateStopped () {
	_ReportState (SERVICE_STOPPED, 0, true, TEXT ("Service stopped"));
	g_bServiceRunning = false;
}

static void _ReportStateErrored () {
	_ReportState (SERVICE_STOPPED, ERROR_INVALID_ENVIRONMENT, true, TEXT ("Service stopped"));
	g_bServiceRunning = false;
}

void ServiceStop (bool bForce) {
	g_oMutex.Enter ();
	if (bForce) {
		_ReportStateStopping ();
		g_poPipe->Close ();
	} else {
		g_poPipe->LazyClose ();
	}
	g_oMutex.Leave ();
}

void ServiceSuspend () {
	_ReportStateStopping ();
	g_oMutex.Enter ();
	g_poPipe->Close ();
	// Never leave the critical section - this function is designed specifically to fcuk up the
	// execution of the service to test a hung JVM. IT IS NOT THE WINDOWS SERVICE SUSPEND/RESUME.
}

#ifdef _WIN32
static void WINAPI ServiceHandler (DWORD dwAction) {
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
#endif /* ifdef _WIN32 */

static void _ServiceStartup (int nReason) {
	CSettings oSettings;
#ifdef _WIN32
	if (nReason == SERVICE_RUN_SCM) {
		g_hServiceStatus = RegisterServiceCtrlHandler (oSettings.GetServiceName (), ServiceHandler);
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
#endif /* ifdef _WIN32 */
	g_lBusyTimeout = oSettings.GetBusyTimeout ();
	_ReportStateStarting ();
}

void ServiceRun (int nReason) {
	_ServiceStartup (nReason);
	g_poJVM = CJVM::Create ();
	if (!g_poJVM) {
		LOGERROR (TEXT ("Couldn't create JVM"));
		_ReportStateErrored ();
		return;
	}
	g_poJVM->Start ();
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
			PJAVACLIENT_CONNECT pjcc = g_poPipe->ReadMessage ();
			if (pjcc) {
				LOGINFO (TEXT ("Connection received from ") << JavaClientGetUserName (pjcc));
				LOGDEBUG (TEXT ("C++ -> Java = ") << JavaClientGetCPPToJavaPipe (pjcc));
				LOGDEBUG (TEXT ("Java -> C++ = ") << JavaClientGetJavaToCPPPipe (pjcc));
				// TODO [XLS-181] Use challenge/response to verify the user name
				g_poJVM->UserConnection (JavaClientGetUserName (pjcc), JavaClientGetCPPToJavaPipe (pjcc), JavaClientGetJavaToCPPPipe (pjcc));
				free (pjcc);
				if (!g_poJVM->IsStopped ()) {
					g_poPipe->CancelLazyClose ();
					if (g_poJVM->IsStopped ()) {
						// Stop might have occurred between the check and the cancel, so restore the cancel
						ServiceStop (false);
					}
				}
				g_oMutex.Enter ();
				if (g_poPipe->IsClosed ()) {
					LOGINFO (TEXT ("Pipe closed with pending connection - reopening"));
					delete g_poPipe;
					g_poPipe = CConnectionPipe::Create ();
					if (g_poPipe) {
						_ReportStateRunning ();
					} else {
						LOGERROR (TEXT ("Couldn't create IPC pipe - shutting down JVM"));
						g_poJVM->Stop ();
					}
				}
				g_oMutex.Leave ();
			} else {
				LOGERROR (TEXT ("Shutting down JVM after failing to read from pipe"));
				g_poJVM->Stop ();
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
}

bool ServiceRunning () {
	return g_bServiceRunning;
}