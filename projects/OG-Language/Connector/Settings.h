/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_settings_h
#define __inc_og_language_connector_settings_h

#include <Service/Public.h>

#define SETTINGS_CONNECTION_PIPE			SERVICE_SETTINGS_CONNECTION_PIPE
#define SETTINGS_CONNECT_TIMEOUT			TEXT ("connectTimeout")
#define SETTINGS_DISPLAY_ALERTS				TEXT ("displayAlerts")
#define SETTINGS_HEARTBEAT_TIMEOUT			TEXT ("heartbeatTimeout")
#define SETTINGS_INPUT_PIPE_PREFIX			TEXT ("inputPipePrefix")
#define SETTINGS_LOG_CONFIGURATION			TEXT ("connectorLogConfiguration")
#define SETTINGS_MAX_PIPE_ATTEMPTS			TEXT ("maxPipeAttempts")
#define SETTINGS_OUTPUT_PIPE_PREFIX			TEXT ("outputPipePrefix")
#define SETTINGS_REQUEST_TIMEOUT			TEXT ("requestTimeout")
#define SETTINGS_SEND_TIMEOUT				TEXT ("sendTimeout")
#define SETTINGS_SERVICE_EXECUTABLE			TEXT ("serviceExecutable")
#define SETTINGS_SERVICE_NAME				SERVICE_SETTINGS_SERVICE_NAME
#define SETTINGS_SERVICE_POLL				TEXT ("servicePoll")
#define SETTINGS_START_TIMEOUT				TEXT ("startTimeout")
#define SETTINGS_STOP_TIMEOUT				TEXT ("stopTimeout")
#ifndef _WIN32
#define SETTINGS_SERVICE_QUERY_CMD			SERVICE_SETTINGS_QUERY_CMD
#define SETTINGS_SERVICE_START_CMD			SERVICE_SETTINGS_START_CMD
#define SETTINGS_SERVICE_STOP_CMD			SERVICE_SETTINGS_STOP_CMD
#endif /* ifndef _WIN32 */

#define CSettings CConnectorSettings

/// Configuration settings for the Connector library components.
class CSettings : public CAbstractSettings {
private:
	const TCHAR *GetConnectionPipe (const TCHAR *pszDefault) const { return Get (SETTINGS_CONNECTION_PIPE, pszDefault); }
	int GetConnectTimeout (int nDefault) const { return Get (SETTINGS_CONNECT_TIMEOUT, nDefault); }
	bool IsDisplayAlerts (bool bDefault)  const { return Get (SETTINGS_DISPLAY_ALERTS, bDefault ? 1 : 0) ? true : false; }
	int GetHeartbeatTimeout (int nDefault) const { return Get (SETTINGS_HEARTBEAT_TIMEOUT, nDefault); }
	const TCHAR *GetInputPipePrefix (const TCHAR *pszDefault) const { return Get (SETTINGS_INPUT_PIPE_PREFIX, pszDefault); }
	const TCHAR *GetLogConfiguration (const TCHAR *pszDefault) const { return Get (SETTINGS_LOG_CONFIGURATION, pszDefault); }
	int GetMaxPipeAttempts (int nDefault) const { return Get (SETTINGS_MAX_PIPE_ATTEMPTS, nDefault); }
	const TCHAR *GetOutputPipePrefix (const TCHAR *pszDefault) const { return Get (SETTINGS_OUTPUT_PIPE_PREFIX, pszDefault); }
	int GetRequestTimeout (int nDefault) const { return Get (SETTINGS_REQUEST_TIMEOUT, nDefault); }
	int GetSendTimeout (int nDefault) const { return Get (SETTINGS_SEND_TIMEOUT, nDefault); }
	const TCHAR *GetServiceExecutable (const CAbstractSettingProvider *poDefault) const { return Get (SETTINGS_SERVICE_EXECUTABLE, poDefault); }
	const TCHAR *GetServiceName (const TCHAR *pszDefault) const { return Get (SETTINGS_SERVICE_NAME, pszDefault); }
	int GetServicePoll (int nDefault) const { return Get (SETTINGS_SERVICE_POLL, nDefault); }
#ifndef _WIN32
	const TCHAR *GetServiceQueryCmd (const CAbstractSettingProvider *poDefault) const { return Get (SETTINGS_SERVICE_QUERY_CMD, poDefault); }
	const TCHAR *GetServiceStartCmd (const CAbstractSettingProvider *poDefault) const { return Get (SETTINGS_SERVICE_START_CMD, poDefault); }
	const TCHAR *GetServiceStopCmd (const CAbstractSettingProvider *poDefault) const { return Get (SETTINGS_SERVICE_STOP_CMD, poDefault); }
#endif /* ifndef _WIN32 */
	int GetStartTimeout (int nDefault) const { return Get (SETTINGS_START_TIMEOUT, nDefault); }
	int GetStopTimeout (int nDefault) const { return Get (SETTINGS_STOP_TIMEOUT, nDefault); }
public:
	const TCHAR *GetConnectionPipe () const;
	int GetConnectTimeout () const;
	bool IsDisplayAlerts () const;
	int GetHeartbeatTimeout () const;
	const TCHAR *GetInputPipePrefix () const;
	const TCHAR *GetLogConfiguration () const;
	int GetMaxPipeAttempts () const;
	const TCHAR *GetOutputPipePrefix () const;
	int GetRequestTimeout () const;
	int GetSendTimeout () const;
	const TCHAR *GetServiceExecutable () const;
	const TCHAR *GetServiceName () const;
	int GetServicePoll () const;
#ifndef _WIN32
	const TCHAR *GetServiceQueryCmd () const;
	const TCHAR *GetServiceStartCmd () const;
	const TCHAR *GetServiceStopCmd () const;
#endif /* ifndef _WIN32 */
	int GetStartTimeout () const;
	int GetStopTimeout () const;
};

#endif /* ifndef __inc_og_language_connector_settings_h */
