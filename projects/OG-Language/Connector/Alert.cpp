/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Alerting API

#include "Alert.h"
#include "Settings.h"
#ifdef _WIN32
#include <Util/DllVersion.h>
#endif /* ifdef _WIN32 */

LOGGING (com.opengamma.connector.Alert);

static CMutex g_oMutex;
static bool g_bEnabled = false;

#ifdef _WIN32

static TCHAR *g_pszTitle = NULL;
static HICON g_hIcon = NULL;

static void _InitialiseNID (NOTIFYICONDATA *pnid) {
	ZeroMemory (pnid, sizeof (NOTIFYICONDATA));
	pnid->cbSize = sizeof (NOTIFYICONDATA);
	// TODO: Can we route a window handle in if it's available? Should we create a dummy window for ourselves?
	pnid->hWnd = HWND_DESKTOP;
	pnid->uID = 1732; // arbitrary guess?
}

static HICON _GetIcon () {
	if (!g_hIcon) {
		g_hIcon = LoadIcon (CDllVersion::GetCurrentModule (), MAKEINTRESOURCE (100));
		if (!g_hIcon) {
			LOGWARN (TEXT ("Couldn't load logo from module, error ") << GetLastError ());
			return NULL;
		}
	}
	return g_hIcon;
}

static void _Display (PCTSTR pszMessage, DWORD dwFlags) {
	assert (g_pszTitle);
	NOTIFYICONDATA nid;
	_InitialiseNID (&nid);
	nid.uFlags = NIF_ICON | NIF_INFO | NIF_TIP;
	nid.hIcon = _GetIcon ();
	StringCbPrintf (nid.szInfo, sizeof (nid.szInfo), TEXT ("%s"), pszMessage);
	StringCbPrintf (nid.szInfoTitle, sizeof (nid.szInfoTitle), TEXT ("%s"), g_pszTitle);
	StringCbPrintf (nid.szTip, sizeof (nid.szTip), TEXT ("%s"), g_pszTitle);
	nid.dwInfoFlags = dwFlags | NIIF_NOSOUND;
	if (!Shell_NotifyIcon (NIM_MODIFY, &nid)) {
		LOGWARN (TEXT ("Couldn't display tray alert, error ") << GetLastError ());
	}
}

#endif /* ifdef _WIN32 */

static bool _EnableImpl () {
	CSettings settings;
	if (!settings.IsDisplayAlerts ()) {
		LOGINFO (TEXT ("Suppressing user alerts"));
		return false;
	}
	LOGDEBUG (TEXT ("Enabling user alerts"));
	CDllVersion version;
#ifdef _WIN32
	NOTIFYICONDATA nid;
	_InitialiseNID (&nid);
	nid.uFlags = NIF_ICON | NIF_TIP;
	nid.hIcon = _GetIcon ();
	StringCbPrintf (nid.szTip, sizeof (nid.szTip), TEXT ("%s"), g_pszTitle = _tcsdup (version.GetProductName ()));
	if (!Shell_NotifyIcon (NIM_ADD, &nid)) {
		LOGWARN (TEXT ("Couldn't set notification tray icon, error ") << GetLastError ());
		return false;
	}
#else
	TODO (TEXT ("Enable user alert"));
#endif
	return true;
}

static bool _DisableImpl () {
	LOGDEBUG (TEXT ("Disabling user alerts"));
#ifdef _WIN32
	NOTIFYICONDATA nid;
	_InitialiseNID (&nid);
	if (!Shell_NotifyIcon (NIM_DELETE, &nid)) {
		LOGWARN (TEXT ("Couldn't delete notification tray icon, error ") << GetLastError ());
		return false;
	}
	if (g_pszTitle) {
		free (g_pszTitle);
		g_pszTitle = NULL;
	}
	if (g_hIcon) {
		DestroyIcon (g_hIcon);
		g_hIcon = NULL;
	}
#else
	TODO (TEXT ("Disable user alert"));
#endif
	return true;
}

void CAlert::Bad (const TCHAR *pszMessage) {
	g_oMutex.Enter ();
	if (g_bEnabled) {
		LOGERROR (TEXT ("Alert: ") << pszMessage);
#ifdef _WIN32
		_Display (pszMessage, NIIF_ERROR);
#else
		TODO (TEXT ("Display ") << pszMessage);
#endif
	} else {
		LOGDEBUG (TEXT ("Alert (disabled): ") << pszMessage);
	}
	g_oMutex.Leave ();
}

void CAlert::Good (const TCHAR *pszMessage) {
	g_oMutex.Enter ();
	if (g_bEnabled) {
		LOGINFO (TEXT ("Alert: ") << pszMessage);
#ifdef _WIN32
		_Display (pszMessage, NIIF_INFO);
#else
		TODO (TEXT ("Display ") << pszMessage);
#endif
	} else {
		LOGDEBUG (TEXT ("Alert (disabled): ") << pszMessage);
	}
	g_oMutex.Leave ();
}

bool CAlert::Enable () {
	bool bResult = false;
	g_oMutex.Enter ();
	if (!g_bEnabled) {
		if (_EnableImpl ()) {
			g_bEnabled = true;
			bResult = true;
		}
	}
	g_oMutex.Leave ();
	return bResult;
}

bool CAlert::Disable () {
	bool bResult = false;
	g_oMutex.Enter ();
	if (g_bEnabled) {
		_DisableImpl ();
		g_bEnabled = false;
		bResult = true;
	}
	g_oMutex.Leave ();
	return bResult;
}
