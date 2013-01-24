/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include <Service/Service.h>
#include <Service/Settings.h>

/// Suppress the warnings that come from the LOGGING macros.
static CSuppressLoggingWarning g_oSuppressLoggingWarning;

LOGGING(com.opengamma.language.service.Main);

/// Make sure Fudge is initialised.
static CFudgeInitialiser g_oFudgeInitialiser;

/// Start up the logging subsystem.
static void _InitialiseLogging () {
	CSettings oSettings;
	LoggingInit (&oSettings);
}

/// Called before ServiceRun for any standard initialisation code.
static void _mainStart () {
	_InitialiseLogging ();
	LOGINFO (TEXT ("Starting service host process"));
}

/// Called after ServiceRun for any standard termination code.
static void _mainEnd () {
	LOGINFO (TEXT ("Stopping service host process"));
}

#ifdef _WIN32
/// Service entry point. Calls ServiceRun with reason SERVICE_RUN_SCM.
///
/// @param[in] dwArgs number arguments from the service control manager
/// @param[in] ppszArgs arguments from the service control manager
static void WINAPI ServiceMain (DWORD dwArgs, PTSTR *ppszArgs) {
	ServiceRun (SERVICE_RUN_SCM);
}

/// Creates a description of the service for registering with the service control
/// manager.
///
/// @return the table
static SERVICE_TABLE_ENTRY *_CreateDispatchTable () {
	CSettings oSettings;
	PCTSTR pszServiceName = oSettings.GetServiceName ();
	size_t cchServiceName = _tcslen (pszServiceName);
	SERVICE_TABLE_ENTRY *pEntry = (SERVICE_TABLE_ENTRY*)malloc (sizeof (SERVICE_TABLE_ENTRY) * 2 + (cchServiceName + 1) * sizeof (TCHAR));
	if (!pEntry) {
		LOGFATAL (TEXT ("Out of memory"));
		return NULL;
	}
	memcpy (pEntry[0].lpServiceName = (PTSTR)(pEntry + 1), pszServiceName, cchServiceName * sizeof (TCHAR));
	pEntry[0].lpServiceProc = ServiceMain;
	pEntry[1].lpServiceName = NULL;
	pEntry[1].lpServiceProc = NULL;
	return pEntry;
}

/// Program entry point. If invoked from the command line with parameter "run" will run the service. If
/// invoked from the service control manager will register the service handlers and then start the service
/// dispatch logic.
///
/// @param[in] argc number of parameters
/// @param[in] argv parameters
int _tmain (int argc, _TCHAR* argv[]) {
#ifndef _DEBUG
	FreeConsole ();
#endif /*ifndef _DEBUG */
	_mainStart ();
	if (argc == 2) {
		if (!_tcscmp (argv[1], TEXT ("run"))) {
			LOGDEBUG (TEXT ("Running inline"));
			ServiceRun (SERVICE_RUN_INLINE);
		} else if (!_tcscmp (argv[1], TEXT ("configure"))) {
			SHELLEXECUTEINFO sei;
			ZeroMemory (&sei, sizeof (sei));
			sei.cbSize = sizeof (sei);
			sei.fMask = SEE_MASK_NOCLOSEPROCESS;
			sei.lpVerb = TEXT ("runas");
			sei.lpFile = argv[0];
			sei.lpParameters = TEXT ("configure-impl");
			if (ShellExecuteEx (&sei)) {
				LOGINFO (TEXT ("Started priviledged process ") << GetProcessId (sei.hProcess));
				WaitForSingleObject (sei.hProcess, INFINITE);
				LOGINFO (TEXT ("Priviledged process terminated"));
				CloseHandle (sei.hProcess);
			} else {
				LOGERROR (TEXT ("Couldn't launch priviledged form"));
			}
		} else if (!_tcscmp (argv[1], TEXT ("configure-impl"))) {
			LOGDEBUG (TEXT ("Configuring service"));
			ServiceConfigure ();
		} else {
			LOGERROR (TEXT ("Unrecognised parameter - ") << argv[1]);
		}
	} else {
		LOGDEBUG (TEXT ("Running as service"));
		SERVICE_TABLE_ENTRY *pServiceEntry = _CreateDispatchTable ();
		if (!StartServiceCtrlDispatcher (pServiceEntry)) {
			LOGFATAL (TEXT ("Couldn't start service control dispatcher, error ") << GetLastError ());
		}
		delete pServiceEntry;
	}
	_mainEnd ();
	return 0;
}
#else
/// Program entry point. If invoked with no parameters, runs as the service. If the "configure" parameter is
/// given runs the interactive configuration dialogs.
///
/// @param[in] argc number of parameters
/// @param[in] argv parameters
int main (int argc, char **argv) {
	_mainStart ();
	if (argc == 2) {
		if (!_tcscmp (argv[1], TEXT ("run"))) {
			ServiceRun (SERVICE_RUN_INLINE);
		} else if (!_tcscmp (argv[1], TEXT ("configure"))) {
			LOGDEBUG (TEXT ("Configuring service"));
			ServiceConfigure ();
		} else {
			LOGERROR (TEXT ("Unrecognised parameter - ") << argv[1]);
		}
	} else if (argc == 3) {
		int nResult;
		if (!_tcscmp (argv[1], TEXT ("jvm"))) {
			nResult = ServiceTestJVM (argv[2]) ? 0 : 1;
		} else {
			LOGERROR (TEXT ("Unrecognised parameter - ") << argv[1]);
			nResult = 2;
		}
		_mainEnd ();
		return nResult;
	} else {
		LOGDEBUG (TEXT ("Running as a daemon"));
		pid_t pid = fork ();
		if (pid < 0) {
			LOGERROR (TEXT ("Couldn't fork daemon process"));
			return 1;
		}
		if (pid > 0) {
			LOGDEBUG (TEXT ("Exiting parent process"));
			return 0;
		}
		pid_t sid = setsid ();
		if (sid < 0) {
			LOGERROR (TEXT ("Couldn't create SID for daemon process"));
			return 1;
		}
		chdir ("/");
		close (STDIN_FILENO);
		close (STDOUT_FILENO);
		close (STDERR_FILENO);
		ServiceRun (SERVICE_RUN_DAEMON);
	}
	_mainEnd ();
	return 0;
}
#endif
