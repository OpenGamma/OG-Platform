/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <windows.h>
#include <strsafe.h>
#include "resource.h"
#include "control.h"
#include "status.h"
#include "startstop.h"
#include "autostart.h"

static CParam *g_apoParams[4] = { &CControl::s_oServiceName, &CControl::s_oHost, &CControl::s_oPort, &CControl::s_oElevate };
static CParams g_oParams (sizeof (g_apoParams) / sizeof (*g_apoParams), g_apoParams);

BOOL ElevateProcess (HWND hwndParent, PCSTR pszCommand) {
	SHELLEXECUTEINFO sei;
	ZeroMemory (&sei, sizeof (sei));
	sei.cbSize = sizeof (sei);
	sei.fMask = SEE_MASK_NOCLOSEPROCESS;
	sei.lpVerb = "runas";
	sei.hwnd = hwndParent;
	sei.nShow = SW_SHOWDEFAULT;
	char szProcess[MAX_PATH];
	if (!GetModuleFileName (NULL, szProcess, sizeof (szProcess) / sizeof (char))) {
		return FALSE;
	}
	sei.lpFile = szProcess;
	char szParams[MAX_PATH];
	StringCbPrintf (szParams,
		sizeof (szParams),
		"-%s \"%s\" -%s \"%s\" -%s \"%s\" -%s \"%s\"",
		CControl::s_oServiceName.GetFlag (), CControl::s_oServiceName.GetString (),
		CControl::s_oHost.GetFlag (), CControl::s_oHost.GetString (),
		CControl::s_oPort.GetFlag (), CControl::s_oPort.GetString (),
		CControl::s_oElevate.GetFlag (), pszCommand);
	sei.lpParameters = szParams;
	if (ShellExecuteEx (&sei)) {
		CloseHandle (sei.hProcess);
		return TRUE;
	} else {
		return FALSE;
	}
}

int WINAPI WinMain (HINSTANCE hInstance, HINSTANCE hPrevInstance, char *pszCmdLine, int nCmdShow) {
	do {
		if (!g_oParams.Process (GetCommandLineW ())) break;
		if (!CControl::Register (hInstance, IDI_OPENGAMMA)) break;
		if (!CStatus::Register (hInstance)) break;
		if (!CStartStop::Register (hInstance)) break;
		if (!CAutoStart::Register (hInstance)) break;
		if (!CControl::Create (hInstance, nCmdShow)) break;
		CControl::DispatchMessages ();
	} while (FALSE);
	return 0;
}
