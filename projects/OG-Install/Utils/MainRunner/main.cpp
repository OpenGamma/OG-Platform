/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <Windows.h>
#include <strsafe.h>
#include "resource.h"
#include "runmain.h"
#include "feedback.h"
#include "Common/param.h"
#include "Common/errorref.h"

static CParamString g_oConfig ("config", NULL, TRUE);
static CParamFlag g_oSilent ("silent");
static CParamFlagInvert g_oGUI ("gui", &g_oSilent);
static CParam *g_apoParams[3] = { &g_oConfig, &g_oSilent, &g_oGUI };
static CParams g_oParams (sizeof (g_apoParams) / sizeof (*g_apoParams), g_apoParams);

static DWORD CALLBACK _main (PVOID pFeedback) {
	CFeedback *poFeedback = (CFeedback*)pFeedback;
	DWORD dwResult = EXIT_FAILURE;
	CJavaRT *poRuntime = NULL;
	CJavaVM *poJVM = NULL;
	do {
		DWORD dwError;
		poRuntime = CJavaRT::Init ();
		if (!poRuntime) {
			ReportErrorReference (ERROR_REF_MAIN);
			if (poFeedback) poFeedback->Alert ("Couldn't initialise the Java runtime", "Installation error", MB_ICONSTOP);
			break;
		}
		poJVM = poRuntime->CreateVM ();
		if (!poJVM) {
			ReportErrorReference (ERROR_REF_MAIN);
			if (poFeedback) poFeedback->Alert ("Couldn't create the Java virtual machine", "Installation error", MB_ICONSTOP);
			break;
		}
		if (poFeedback) {
			if (!poFeedback->Connect (poJVM)) {
				ReportErrorReference (ERROR_REF_MAIN);
				break;
			}
		}
		dwError = CMain::Run (poJVM);
		if (dwError) {
			ReportErrorReference (dwError);
			if (poFeedback) {
				char sz[256];
				StringCbPrintf (sz, sizeof (sz), "Couldn't run OpenGamma, error reference 0x%X", dwError);
				poFeedback->Alert (sz, "Installation error", MB_ICONSTOP);
			}
			break;
		}
		dwResult = EXIT_SUCCESS;
	} while (FALSE);
	CJavaVM::Release (poJVM);
	CFeedback::Disconnect ();
	delete poRuntime;
	if (poFeedback) poFeedback->Destroy ();
	return dwResult;
}

/// Launcher entry point. The parameter must be the INI file defining the launch.
///
/// @param[in] hInstance ignored
/// @param[in] hPrevInstance ignored
/// @param[in] pszCmdLine the command line - containing the path to the INI file
/// @param[in] nCmdShow ignored
/// @return 0 if the launch was okay, 1 if there was a problem
int WINAPI WinMain (HINSTANCE hInstance, HINSTANCE hPrevInstance, char *pszCmdLine, int nCmdShow) {
	DWORD dwResult = EXIT_FAILURE;
	CFeedback *poFeedback = NULL;
	do {
		if (!g_oParams.Process (GetCommandLineW ())) {
			ReportErrorReference (ERROR_REF_MAIN);
			break;
		}
		if (!CJavaRT::s_oConfig.Read (g_oConfig.GetString ())
		 || !CMain::s_oConfig.Read (g_oConfig.GetString ())) {
			ReportErrorReference (ERROR_REF_MAIN);
			break;
		}
		if (g_oSilent.IsSet ()) {
			dwResult = _main (NULL);
		} else {
			if (!CFeedbackWindow::Register(hInstance, IDI_OPENGAMMA)) {
				ReportErrorReference (ERROR_REF_MAIN);
				break;
			}
			poFeedback = new CFeedback (hInstance);
			poFeedback->Show (nCmdShow);
			poFeedback->BringToTop ();
			HANDLE hThread = CreateThread (NULL, 0, _main, poFeedback, 0, NULL);
			if (!hThread) {
				ReportErrorReference (ERROR_REF_MAIN);
				break;
			}
			if (CFeedbackWindow::DispatchMessages ()) {
				// Non-zero exit code -- abort the operation
				ReportErrorReference (ERROR_REF_MAIN);
				TerminateThread (hThread, EXIT_FAILURE);
			}
			WaitForSingleObject (hThread, 30000);
			GetExitCodeThread (hThread, &dwResult);
			CloseHandle (hThread);
		}
	} while (FALSE);
	CFeedbackWindow::Release (poFeedback);
	return dwResult;
}
