/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_settings_h
#define __inc_og_language_connector_settings_h

// Runtime configuration options

#include <Service/Public.h>

#define SETTINGS_CONNECTION_PIPE			SERVICE_SETTINGS_CONNECTION_PIPE
#define SETTINGS_CONNECT_TIMEOUT			TEXT ("connectTimeout")
#define SETTINGS_DISPLAY_ALERTS				TEXT ("displayAlerts")
#define SETTINGS_HEARTBEAT_TIMEOUT			TEXT ("heartbeatTimeout")
#define SETTINGS_INPUT_PIPE_PREFIX			TEXT ("inputPipePrefix")
#define SETTINGS_LOG_CONFIGURATION			TEXT ("connectorLogConfiguration")
#define SETTINGS_MAX_PIPE_ATTEMPTS			TEXT ("maxPipeAttempts")
#define SETTINGS_OUTPUT_PIPE_PREFIX			TEXT ("outputPipePrefix")
#define SETTINGS_SEND_TIMEOUT				TEXT ("sendTimeout")
#define SETTINGS_SERVICE_EXECUTABLE			TEXT ("serviceExecutable")
#define SETTINGS_SERVICE_NAME				SERVICE_SETTINGS_SERVICE_NAME
#define SETTINGS_SERVICE_POLL				TEXT ("servicePoll")
#define SETTINGS_START_TIMEOUT				TEXT ("startTimeout")
#define SETTINGS_STOP_TIMEOUT				TEXT ("stopTimeout")

#define CSettings CConnectorSettings

class CSettings : public CAbstractSettings {
private:
	TCHAR *m_pszDefaultServiceExecutable;
	const TCHAR *GetConnectionPipe (const TCHAR *pszDefault) { return Get (SETTINGS_CONNECTION_PIPE, pszDefault); }
	int GetConnectTimeout (int nDefault) { return Get (SETTINGS_CONNECT_TIMEOUT, nDefault); }
	bool IsDisplayAlerts (bool bDefault)  { return Get (SETTINGS_DISPLAY_ALERTS, bDefault ? 1 : 0) ? true : false; }
	int GetHeartbeatTimeout (int nDefault) { return Get (SETTINGS_HEARTBEAT_TIMEOUT, nDefault); }
	const TCHAR *GetInputPipePrefix (const TCHAR *pszDefault) { return Get (SETTINGS_INPUT_PIPE_PREFIX, pszDefault); }
	const TCHAR *GetLogConfiguration (const TCHAR *pszDefault) { return Get (SETTINGS_LOG_CONFIGURATION, pszDefault); }
	int GetMaxPipeAttempts (int nDefault) { return Get (SETTINGS_MAX_PIPE_ATTEMPTS, nDefault); }
	const TCHAR *GetOutputPipePrefix (const TCHAR *pszDefault) { return Get (SETTINGS_OUTPUT_PIPE_PREFIX, pszDefault); }
	int GetSendTimeout (int nDefault) { return Get (SETTINGS_SEND_TIMEOUT, nDefault); }
	const TCHAR *GetServiceExecutable (const TCHAR *pszDefault) { return Get (SETTINGS_SERVICE_EXECUTABLE, pszDefault); }
	const TCHAR *GetServiceName (const TCHAR *pszDefault) { return Get (SETTINGS_SERVICE_NAME, pszDefault); }
	int GetServicePoll (int nDefault) { return Get (SETTINGS_SERVICE_POLL, nDefault); }
	int GetStartTimeout (int nDefault) { return Get (SETTINGS_START_TIMEOUT, nDefault); }
	int GetStopTimeout (int nDefault) { return Get (SETTINGS_STOP_TIMEOUT, nDefault); }
public:
	CSettings ();
	~CSettings ();
	const TCHAR *GetConnectionPipe ();
	int GetConnectTimeout ();
	bool IsDisplayAlerts ();
	int GetHeartbeatTimeout ();
	const TCHAR *GetInputPipePrefix ();
	const TCHAR *GetLogConfiguration ();
	int GetMaxPipeAttempts ();
	const TCHAR *GetOutputPipePrefix ();
	int GetSendTimeout ();
	const TCHAR *GetServiceExecutable ();
	const TCHAR *GetServiceName ();
	int GetServicePoll ();
	int GetStartTimeout ();
	int GetStopTimeout ();
};

#endif /* ifndef __inc_og_language_connector_settings_h */
