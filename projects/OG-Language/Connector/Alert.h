/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_alert_h
#define __inc_og_language_connector_alert_h

// Alerting API

class CAlert {
private:
	// Stop construction
	CAlert ();
public:
	static void Bad (const TCHAR *pszMessage);
	static void Good (const TCHAR *pszMessage);
	static bool Enable ();
	static bool Disable ();
};

#endif /* ifndef __inc_og_language_connector_alert_h */
