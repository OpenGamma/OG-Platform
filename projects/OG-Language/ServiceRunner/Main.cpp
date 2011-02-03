/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Links the static service library as a service executable

LOGGING(com.opengamma.language.service.Main);

static void _InitialiseLogging () {
	CSettings oSettings;
	LoggingInit (&oSettings);
}

static void _mainStart () {
	_InitialiseLogging ();
	LOGINFO (TEXT ("Starting service host process"));
}

static void _mainEnd () {
	LOGINFO (TEXT ("Stopping service host process"));
}

#ifdef _WIN32
static void WINAPI ServiceMain (DWORD dwArgs, PTSTR *ppszArgs) {
	ServiceRun (SERVICE_RUN_SCM);
}

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

int _tmain (int argc, _TCHAR* argv[]) {
	_mainStart ();
	if ((argc == 2) && !_tcscmp (argv[1], TEXT ("run"))) {
		LOGDEBUG (TEXT ("Running inline"));
		ServiceRun (SERVICE_RUN_INLINE);
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
int main (int argc, char **argv) {
	_mainStart ();
	ServiceRun (SERVICE_RUN_INLINE);
	_mainEnd ();
	return 0;
}
#endif