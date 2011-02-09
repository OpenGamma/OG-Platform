/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_settings_h
#define __inc_og_language_connector_settings_h

// Runtime configuration options

#define SETTINGS_DISPLAY_ALERTS				TEXT ("displayAlerts")
#define SETTINGS_INPUT_PIPE_PREFIX			TEXT ("inputPipePrefix")
#define SETTINGS_LOG_CONFIGURATION			TEXT ("connectorLogConfiguration")
#define SETTINGS_MAX_PIPE_ATTEMPTS			TEXT ("maxPipeAttempts")
#define SETTINGS_OUTPUT_PIPE_PREFIX			TEXT ("outputPipePrefix")

class CSettings : public CAbstractSettings {
private:
	bool IsDisplayAlerts (bool bDefault)  { return Get (SETTINGS_DISPLAY_ALERTS, bDefault ? 1 : 0) ? true : false; }
	const TCHAR *GetInputPipePrefix (const TCHAR *pszDefault) { return Get (SETTINGS_INPUT_PIPE_PREFIX, pszDefault); }
	const TCHAR *GetLogConfiguration (const TCHAR *pszDefault) { return Get (SETTINGS_LOG_CONFIGURATION, pszDefault); }
	int GetMaxPipeAttempts (int nDefault) { return Get (SETTINGS_MAX_PIPE_ATTEMPTS, nDefault); }
	const TCHAR *GetOutputPipePrefix (const TCHAR *pszDefault) { return Get (SETTINGS_OUTPUT_PIPE_PREFIX, pszDefault); }
public:
	CSettings ();
	~CSettings ();
	bool IsDisplayAlerts ();
	const TCHAR *GetInputPipePrefix ();
	const TCHAR *GetLogConfiguration ();
	int GetMaxPipeAttempts ();
	const TCHAR *GetOutputPipePrefix ();
};

#endif /* ifndef __inc_og_language_connector_settings_h */