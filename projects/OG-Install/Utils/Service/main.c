/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <Windows.h>
#include <strsafe.h>
#include "../MainRunner/jvm.h"

static const char *g_pszServiceName;
static SERVICE_STATUS_HANDLE g_hStatus;

static void WINAPI _ServiceHandler (DWORD dwAction) {
	if (dwAction == SERVICE_CONTROL_STOP) {
		SERVICE_STATUS sta;
		ZeroMemory (&sta, sizeof (sta));
		sta.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
		sta.dwCurrentState = SERVICE_STOP_PENDING;
		sta.dwWaitHint = 30000;
		SetServiceStatus (g_hStatus, &sta);
		InvokeStop (NULL);
	}
}

static void WINAPI _ServiceMain (DWORD dwArgs, char **pspzArgs) {
	g_hStatus = RegisterServiceCtrlHandler (g_pszServiceName, _ServiceHandler);
	if (FindJava () && CreateJavaVM ()) {
		SERVICE_STATUS sta;
		ZeroMemory (&sta, sizeof (sta));
		sta.dwControlsAccepted = SERVICE_ACCEPT_STOP;
		sta.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
		sta.dwCurrentState = SERVICE_RUNNING;
		sta.dwWaitHint = 30000;
		SetServiceStatus (g_hStatus, &sta);
		InvokeMain (NULL);
		sta.dwCurrentState = SERVICE_STOPPED;
		SetServiceStatus (g_hStatus, &sta);
	}
}

static void _setWorkingFolder (const char *pszPath) {
	size_t i;
	int n;
	if (*pszPath == '\"') pszPath++;
	i = strlen (pszPath);
	n = 2;
	while (--i > 0) {
		if (pszPath[i] == '\\') {
			if (!--n) {
				char *pszWorkingFolder = (char*)malloc (i + 1);
				if (pszWorkingFolder) {
					memcpy (pszWorkingFolder, pszPath, i);
					pszWorkingFolder[i] = 0;
					SetCurrentDirectory (pszWorkingFolder);
					free (pszWorkingFolder);
				}
				break;
			}
		}
	}
}

/// Service executable entry point. First parameter is the INI file describing the Java
/// launch. Second parameter is the name of the service.
///
/// @param[in] argc number of arguments (should be 3)
/// @param[in] argv the arguments
/// @return 1 if there is a problem, 0 if all is okay
int main (int argc, char **argv) {
	SERVICE_TABLE_ENTRY ste[2];
	if (!ReadConfigurationFile (argv[1])) return 1;
	_setWorkingFolder (argv[1]);
	g_pszServiceName = argv[2];
	ste[0].lpServiceName = argv[2];
	ste[0].lpServiceProc = _ServiceMain;
	ste[1].lpServiceName = NULL;
	ste[1].lpServiceProc = NULL;
	StartServiceCtrlDispatcher (ste);
	return 0;
}