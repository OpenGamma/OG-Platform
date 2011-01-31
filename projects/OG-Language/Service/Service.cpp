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

LOGGING(com.opengamma.language.service.Service);

#define DEFAULT_ERROR_CODE	10
#define WAIT_HINT			2000
#define IS_BUSY_TIMEOUT		(WAIT_HINT / 2)

static CRITICAL_SECTION g_cs;
static CJVM *g_poJVM = NULL;
static CConnectionPipe *g_poPipe = NULL;
static SERVICE_STATUS_HANDLE g_hServiceStatus = NULL;
static DWORD g_dwServiceCheckPoint = 0;

static void _ReportState (DWORD dwStateCode, DWORD dwExitCode = NO_ERROR) {
	SERVICE_STATUS sta;
	ZeroMemory (&sta, sizeof (sta));
	switch (dwStateCode) {
	case SERVICE_START_PENDING :
		LOGDEBUG (TEXT ("Service starting"));
		sta.dwCheckPoint = ++g_dwServiceCheckPoint;
		break;
	case SERVICE_RUNNING :
		LOGINFO (TEXT ("Service started"));
		sta.dwControlsAccepted = SERVICE_ACCEPT_STOP;
		break;
	case SERVICE_STOP_PENDING :
		LOGDEBUG (TEXT ("Service stopping"));
		sta.dwCheckPoint = ++g_dwServiceCheckPoint;
		break;
	case SERVICE_STOPPED :
		LOGINFO (TEXT ("Service stopped"));
		break;
	default :
		LOGFATAL (TEXT ("Unexpected service state code ") << dwStateCode);
		break;
	}
	if (g_hServiceStatus) {
		sta.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
		sta.dwCurrentState = dwStateCode;
		sta.dwWin32ExitCode = dwExitCode;
		sta.dwWaitHint = WAIT_HINT;
		SetServiceStatus (g_hServiceStatus, &sta);
	}
}

void ServiceStop (BOOL bForce) {
	EnterCriticalSection (&g_cs);
	if (bForce) {
		_ReportState (SERVICE_STOP_PENDING);
		g_poPipe->Close ();
	} else {
		g_poPipe->LazyClose ();
	}
	LeaveCriticalSection (&g_cs);
}

void ServiceSuspend () {
	_ReportState (SERVICE_STOP_PENDING);
	EnterCriticalSection (&g_cs);
	g_poPipe->Close ();
	// Never leave the critical section - this function is designed specifically to fcuk up the
	// execution of the service to test a hung JVM. IT IS NOT THE WINDOWS SERVICE SUSPEND/RESUME.
}

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

static void _ServiceStartup () {
	CSettings oSettings;
	g_hServiceStatus = RegisterServiceCtrlHandler (oSettings.GetServiceName (), ServiceHandler);
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
	_ReportState (SERVICE_START_PENDING);
}

void ServiceRun () {
	_ServiceStartup ();
	g_poJVM = CJVM::Create ();
	if (!g_poJVM) {
		LOGERROR (TEXT ("Couldn't create JVM"));
		_ReportState (SERVICE_STOPPED, DEFAULT_ERROR_CODE);
		return;
	}
	g_poJVM->Start ();
	g_poPipe = CConnectionPipe::Create ();
	if (!g_poPipe) {
		LOGERROR (TEXT ("Couldn't create IPC pipe"));
	}
	while (g_poJVM->IsBusy (IS_BUSY_TIMEOUT)) {
		_ReportState (SERVICE_START_PENDING);
	}
	if (g_poPipe && g_poJVM->IsRunning ()) {
		_ReportState (SERVICE_RUNNING);
		do {
			LOGDEBUG (TEXT ("Waiting for user connection"));
			PJAVACLIENT_CONNECT pjcc = g_poPipe->ReadMessage ();
			if (pjcc) {
				LOGINFO (TEXT ("Connection received from ") << JavaClientGetUserName (pjcc));
				LOGDEBUG (TEXT ("C++ -> Java = ") << JavaClientGetCPPToJavaPipe (pjcc));
				LOGDEBUG (TEXT ("Java -> C++ = ") << JavaClientGetJavaToCPPPipe (pjcc));
				g_poJVM->UserConnection (JavaClientGetUserName (pjcc), JavaClientGetCPPToJavaPipe (pjcc), JavaClientGetJavaToCPPPipe (pjcc));
				free (pjcc);
				if (!g_poJVM->IsStopped ()) {
					g_poPipe->CancelLazyClose ();
					if (g_poJVM->IsStopped ()) {
						// Stop might have occurred between the check and the cancel, so restore the cancel
						ServiceStop (FALSE);
					}
				}
				EnterCriticalSection (&g_cs);
				if (g_poPipe->IsClosed ()) {
					LOGINFO (TEXT ("Pipe closed with pending connection - reopening"));
					delete g_poPipe;
					g_poPipe = CConnectionPipe::Create ();
					if (g_poPipe) {
						_ReportState (SERVICE_RUNNING);
					} else {
						LOGERROR (TEXT ("Couldn't create IPC pipe - shutting down JVM"));
						g_poJVM->Stop ();
					}
				}
				LeaveCriticalSection (&g_cs);
			} else {
				LOGERROR (TEXT ("Shutting down JVM after failing to read from pipe"));
				g_poJVM->Stop ();
			}
		} while (!g_poJVM->IsBusy (IS_BUSY_TIMEOUT) && g_poJVM->IsRunning ());
		_ReportState (SERVICE_STOP_PENDING);
		while (g_poJVM->IsBusy (IS_BUSY_TIMEOUT)) {
			_ReportState (SERVICE_STOP_PENDING);
		}
		_ReportState (SERVICE_STOPPED);
	} else {
		_ReportState (SERVICE_STOPPED, DEFAULT_ERROR_CODE);
	}
	if (g_poPipe) {
		delete g_poPipe;
		g_poPipe = NULL;
	}
	delete g_poJVM;
	g_poJVM = NULL;
}

void ServiceInit () {
	InitializeCriticalSection (&g_cs);
}

void ServiceDone () {
	DeleteCriticalSection (&g_cs);
}