/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Alert.h"
#include "Settings.h"
#ifdef _WIN32
#include <Util/DllVersion.h>
#endif /* ifdef _WIN32 */
#include <Util/Mutex.h>
#include <Util/String.h>

LOGGING (com.opengamma.language.connector.Alert);

/// Critical section to guard the global variables
static CMutex g_oMutex;

/// Flag indicating if alerts should be displayed/handled or discarded only to the logs. Set to
/// TRUE to enable, FALSE to only log them.
static bool g_bEnabled = false;

#ifdef _WIN32

/// Window handle of the owning application. Alerts can only be displayed in the system tray if
/// there is an associated window handle. NULL if the alerts are not enabled.
static HWND g_hwnd = NULL;

/// Title to use for alert popup windows. This is a copy of the Product Name held in the module's
/// resource file. NULL if the alerts are not enabled.
static TCHAR *g_pszTitle = NULL;

/// Product icon to use in alert popup windows. NULL if the alerts are not enabled.
static HICON g_hIcon = NULL;

/// Initialise a NOTIFYICONDATA structure with common values.
///
/// The caller must hold the critical section.
///
/// @param[out] pnid structure to initialise, never NULL
static void _InitialiseNID (NOTIFYICONDATA *pnid) {
	ZeroMemory (pnid, sizeof (NOTIFYICONDATA));
	pnid->cbSize = sizeof (NOTIFYICONDATA);
	pnid->hWnd = g_hwnd;
	pnid->uID = 1732; // arbitrary guess?
}

/// Returns the icon to use in alert popup windows. The first call will load the icon from the
/// module resources and store it in the global variable.
///
/// The caller must hold the critical section
///
/// @return the icon, or NULL if there is a problem
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

/// Display an alert popup in the system tray.
///
/// The caller must hold the critical section.
///
/// @param[in] pszMessage message to display, never NULL
/// @param[in] dwFlags message display flags (see Win32 documentation of NOTIFYICONDATA)
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

/// Enable the display of alerts. The global variables are set ready for other calls. Enabling
/// has no effect if the CSettings implementation does not allow for displaying alerts.
///
/// The caller must hold the crtical section.
///
/// @return TRUE if alerts were enabled, FALSE if there was an error or they are disabled in
///         the settings.
static bool _EnableImpl () {
	CSettings settings;
	if (!settings.IsDisplayAlerts ()) {
		LOGINFO (TEXT ("Suppressing user alerts"));
		return false;
	}
	LOGDEBUG (TEXT ("Enabling user alerts"));
#ifdef _WIN32
	CDllVersion version;
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

/// Disable the display of alerts. The global variables are cleared so further calls to display
/// an alert only write to the logs.
///
/// The caller must hold the critical section.
///
/// @return TRUE if alerts were disabled, FALSE if there was an error disabling them
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

/// Display a "Bad" alert. The implementation is operating system/environment dependent but
/// would probably involve a warning triangle style icon and a more severe level of logging
/// (LOGERROR) than "Good" alerts.
///
/// @param[in] pszMessage message to display, never NULL
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

/// Display a "Good" alert. The implementation is operating system/environment dependent but
/// would probably involve a discreet notification and a gentler level of logging (LOGINFO)
/// than "Bad" alerts.
///
/// @param[in] pszMessage message to display, never NULL
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

/// Enable the display of alerts.
///
/// @return TRUE if the alert display was enabled, FALSE if there was a problem
#ifdef _WIN32
/// @param[in] hwnd application window handle
bool CAlert::Enable (HWND hwnd) {
#else /* ifdef _WIN32 */
bool CAlert::Enable () {
#endif /* ifdef _WIN32 */
	bool bResult = false;
	g_oMutex.Enter ();
#ifdef _WIN32
	g_hwnd = hwnd;
#endif /* ifdef _WIN32 */
	if (!g_bEnabled) {
		if (_EnableImpl ()) {
			g_bEnabled = true;
			bResult = true;
		}
	}
	g_oMutex.Leave ();
	return bResult;
}

/// Disable the display of alerts
///
/// @return TRUE if the alerts were disabled, FALSE if there was a problem
bool CAlert::Disable () {
	bool bResult = false;
	g_oMutex.Enter ();
	if (g_bEnabled) {
		_DisableImpl ();
		g_bEnabled = false;
		bResult = true;
	}
#ifdef _WIN32
	g_hwnd = NULL;
#endif /* ifdef _WIN32 */
	g_oMutex.Leave ();
	return bResult;
}
