/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Runtime configuration options

#include "Settings.h"
#ifdef _WIN32
#include <Util/DllVersion.h>
#endif /* ifdef _WIN32 */

LOGGING (com.opengamma.language.connector.Settings);

#ifdef _WIN32
#define DEFAULT_PIPE_PREFIX			TEXT ("\\\\.\\pipe\\OpenGammaLanguageAPI-Client-")
#else
#define DEFAULT_PIPE_PREFIX			TEXT ("/var/run/OG-Language/Client-")
#endif

#define DEFAULT_CONNECTION_PIPE		SERVICE_DEFAULT_CONNECTION_PIPE
#define DEFAULT_CONNECT_TIMEOUT		3000	/* 3s default */
#define DEFAULT_DISPLAY_ALERTS		true
#define DEFAULT_HEARTBEAT_TIMEOUT	3000	/* 3s default */
#define DEFAULT_INPUT_PIPE_PREFIX	DEFAULT_PIPE_PREFIX	TEXT ("Input-")
#define DEFAULT_LOG_CONFIGURATION	NULL
#define DEFAULT_MAX_PIPE_ATTEMPTS	3
#define DEFAULT_OUTPUT_PIPE_PREFIX	DEFAULT_PIPE_PREFIX TEXT ("Output-")
#define DEFAULT_SEND_TIMEOUT		1500	/* 1.5s default */
#ifdef _WIN32
#define DEFAULT_SERVICE_EXECUTABLE	TEXT ("ServiceRunner.exe")
#else
#define DEFAULT_SERVICE_EXECUTABLE	TEXT ("ServiceRunner")
#endif
#define DEFAULT_SERVICE_NAME		SERVICE_DEFAULT_SERVICE_NAME
#define DEFAULT_SERVICE_POLL		250		/* 1/4s default */
#define DEFAULT_START_TIMEOUT		30000	/* 30s default */
#define DEFAULT_STOP_TIMEOUT		2000	/* 2s default */

CSettings::CSettings () : CAbstractSettings () {
	m_pszDefaultServiceExecutable = NULL;
}

CSettings::~CSettings () {
	if (m_pszDefaultServiceExecutable) {
		free (m_pszDefaultServiceExecutable);
	}
}

const TCHAR *CSettings::GetConnectionPipe () {
	return GetConnectionPipe (DEFAULT_CONNECTION_PIPE);
}

int CSettings::GetConnectTimeout () {
	return GetConnectTimeout (DEFAULT_CONNECT_TIMEOUT);
}

bool CSettings::IsDisplayAlerts () {
	return IsDisplayAlerts (DEFAULT_DISPLAY_ALERTS);
}

int CSettings::GetHeartbeatTimeout () {
	return GetHeartbeatTimeout (DEFAULT_HEARTBEAT_TIMEOUT);
}

const TCHAR *CSettings::GetInputPipePrefix () {
	return GetInputPipePrefix (DEFAULT_INPUT_PIPE_PREFIX);
}

const TCHAR *CSettings::GetLogConfiguration () {
	return GetLogConfiguration (DEFAULT_LOG_CONFIGURATION);
}

int CSettings::GetMaxPipeAttempts () {
	return GetMaxPipeAttempts (DEFAULT_MAX_PIPE_ATTEMPTS);
}

const TCHAR *CSettings::GetOutputPipePrefix () {
	return GetOutputPipePrefix (DEFAULT_OUTPUT_PIPE_PREFIX);
}

int CSettings::GetSendTimeout () {
	return GetSendTimeout (DEFAULT_SEND_TIMEOUT);
}

const TCHAR *CSettings::GetServiceExecutable () {
	if (!m_pszDefaultServiceExecutable) {
#ifdef _WIN32
		// TODO: if the service is installed, get the executable from the service settings
#endif /* ifdef _WIN32 */
		do {
			TCHAR szPath[260];
			if (!CProcess::GetCurrentModule (szPath, (sizeof (szPath) / sizeof (TCHAR)) - _tcslen (DEFAULT_SERVICE_EXECUTABLE))) {
				LOGWARN (TEXT ("Couldn't get current module filename, error ") << GetLastError ());
				break;
			}
			TCHAR *pszFolder = _tcsrchr (szPath, PATH_CHAR);
			if (pszFolder) {
				pszFolder++;
			} else {
				pszFolder = szPath;
			}
			// The memcpy is safe because we gave a short buffer length to GetCurrentModule to guarantee room
			memcpy (pszFolder, DEFAULT_SERVICE_EXECUTABLE, (_tcslen (DEFAULT_SERVICE_EXECUTABLE) + 1) * sizeof (TCHAR));
			LOGDEBUG (TEXT ("Executable ") << szPath);
#ifdef _WIN32
			HANDLE hFile = CreateFile (szPath, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, 0, NULL);
			if (!hFile) {
				LOGWARN (TEXT ("Couldn't open executable file ") << szPath << TEXT (", error ") << GetLastError ());
				break;
			}
			if (!GetFinalPathNameByHandle (hFile, szPath, MAX_PATH, FILE_NAME_NORMALIZED)) {
				LOGWARN (TEXT ("Couldn't get normalized executable name, error ") << GetLastError ());
				break;
			}
			if (!_tcsncmp (szPath, TEXT ("\\\\?\\UNC\\"), 8)) {
				LOGDEBUG (TEXT ("Removing \\\\?\\UNC\\ prefix"));
				memmove (szPath + 2, szPath + 8, (_tcslen (szPath + 8) + 1) * sizeof (TCHAR));
			} else if (!_tcsncmp (szPath, TEXT ("\\\\?\\"), 4)) {
				LOGDEBUG (TEXT ("Removing \\\\?\\ prefix"));
				memmove (szPath, szPath + 4, (_tcslen (szPath + 4) + 1) * sizeof (TCHAR));
			}
			CloseHandle (hFile);
#else
			int file = open (szPath, O_RDONLY);
			if (file <= 0) {
				LOGWARN (TEXT ("Couldn't open executable file ") << szPath << TEXT (", error ") << GetLastError ());
				break;
			}
			close (file);
#endif /* ifdef _WIN32 */
			m_pszDefaultServiceExecutable = _tcsdup (szPath);
		} while (false);
		if (m_pszDefaultServiceExecutable) {
			LOGDEBUG (TEXT ("Default service executable ") << m_pszDefaultServiceExecutable);
		} else {
			m_pszDefaultServiceExecutable = _tcsdup (DEFAULT_SERVICE_EXECUTABLE);
		}
	}
	return GetServiceExecutable (m_pszDefaultServiceExecutable);
}

const TCHAR *CSettings::GetServiceName () {
	return GetServiceName (DEFAULT_SERVICE_NAME);
}

int CSettings::GetServicePoll () {
	return GetServicePoll (DEFAULT_SERVICE_POLL);
}

int CSettings::GetStartTimeout () {
	return GetStartTimeout (DEFAULT_START_TIMEOUT);
}

int CSettings::GetStopTimeout () {
	return GetStopTimeout (DEFAULT_STOP_TIMEOUT);
}
