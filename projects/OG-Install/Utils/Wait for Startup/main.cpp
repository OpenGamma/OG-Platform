/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <windows.h>
#include "resource.h"
#include "wait.h"

static CParamFlag g_oSilent ("silent");
static CParamFlagInvert g_oGUI ("gui", &g_oSilent);
static CParam *g_apoParams[6] = { &CWait::s_oServiceName, &CWait::s_oHost, &CWait::s_oPort, &CFeedback::s_oTitle, &g_oSilent, &g_oGUI };
static CParams g_oParams (sizeof (g_apoParams) / sizeof (*g_apoParams), g_apoParams);

DWORD CALLBACK _main (LPVOID pReserved) {
	CFeedback *poFeedback = (CFeedback*)pReserved;
	BOOL bResult = CWait::WaitForStartup (poFeedback);
	if (poFeedback) {
		poFeedback->Destroy ();
	}
	return bResult ? EXIT_SUCCESS : EXIT_FAILURE;
}

int WINAPI WinMain (HINSTANCE hInstance, HINSTANCE hPrevInstance, char *pszCmdLine, int nCmdShow) {
	DWORD dwResult = EXIT_FAILURE;
	CFeedback *poFeedback = NULL;
	do {
		if (!g_oParams.Process (GetCommandLineW ())) break;
		if (g_oSilent.IsSet ()) {
			dwResult = _main (NULL);
		} else {
			if (!CFeedbackWindow::Register(hInstance, IDI_OPENGAMMA)) break;
			poFeedback = new CFeedback (hInstance);
			poFeedback->Show (nCmdShow);
			poFeedback->BringToTop ();
			HANDLE hThread = CreateThread (NULL, 0, _main, poFeedback, 0, NULL);
			if (!hThread) break;
			if (CFeedbackWindow::DispatchMessages ()) {
				// Non-zero exit code -- stop checking and return success
				TerminateThread (hThread, EXIT_SUCCESS);
			}
			WaitForSingleObject (hThread, 30000);
			GetExitCodeThread (hThread, &dwResult);
			CloseHandle (hThread);
		}
	} while (FALSE);
	if (poFeedback) CFeedbackWindow::Release (poFeedback);
	return dwResult;
}
