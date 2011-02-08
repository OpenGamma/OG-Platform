/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_settings_h
#define __inc_og_language_connector_settings_h

// Runtime configuration options

#define SETTINGS_DISPLAY_ALERTS				TEXT ("displayAlerts")
#define SETTINGS_LOG_CONFIGURATION			TEXT ("connectorLogConfiguration")

class CSettings : public CAbstractSettings {
private:
	bool IsDisplayAlerts (bool bDefault)  { return Get (SETTINGS_DISPLAY_ALERTS, bDefault ? 1 : 0) ? true : false; }
	const TCHAR *GetLogConfiguration (const TCHAR *pszDefault) { return Get (SETTINGS_LOG_CONFIGURATION, pszDefault); }
public:
	CSettings ();
	~CSettings ();
	bool IsDisplayAlerts ();
	const TCHAR *GetLogConfiguration ();
};

#endif /* ifndef __inc_og_language_connector_settings_h */