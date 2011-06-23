/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_alert_h
#define __inc_og_language_connector_alert_h

/// Alerting service. The Win32 environment will use an icon in the system tray and
/// provide notification popup alerts. The Posix implementation is unfinished.
class CAlert {
private:

	/// Not implemented to prevent construction of object instances.
	CAlert ();

public:
	static void Bad (const TCHAR *pszMessage);
	static void Good (const TCHAR *pszMessage);
#ifdef _WIN32
	static bool Enable (HWND hwnd);
#else /* ifdef _WIN32 */
	static bool Enable ();
#endif /* ifdef _WIN32 */
	static bool Disable ();
};

#endif /* ifndef __inc_og_language_connector_alert_h */
