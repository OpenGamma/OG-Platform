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
#include <Util/File.h>
#include <Util/Process.h>
#include <Util/Error.h>
#include <Util/Quote.h>

LOGGING (com.opengamma.language.connector.Settings);

#ifndef DEFAULT_PIPE_PREFIX
# ifdef _WIN32
#  define DEFAULT_PIPE_PREFIX			TEXT ("\\\\.\\pipe\\OpenGammaLanguageAPI-Client-")
# else /* ifdef _WIN32 */
#  ifndef DEFAULT_PIPE_FOLDER
#   define DEFAULT_PIPE_FOLDER			TEXT ("/var/run/OG-Language/")
#  endif /* ifndef DEFAULT_PIPE_FOLDER */
#  define DEFAULT_PIPE_PREFIX			DEFAULT_PIPE_FOLDER TEXT ("Client-")
# endif /* ifdef _WIN32 */
#endif /* ifndef DEFAULT_PIPE_PREFIX */

#define DEFAULT_CONNECT_TIMEOUT		3000	/* 3s default */
#define DEFAULT_DISPLAY_ALERTS		true
#define DEFAULT_HEARTBEAT_TIMEOUT	3000	/* 3s default */
#define DEFAULT_INPUT_PIPE_PREFIX	DEFAULT_PIPE_PREFIX	TEXT ("Input-")
#define DEFAULT_LOG_CONFIGURATION	NULL
#define DEFAULT_MAX_PIPE_ATTEMPTS	3
#define DEFAULT_OUTPUT_PIPE_PREFIX	DEFAULT_PIPE_PREFIX TEXT ("Output-")
#define DEFAULT_SEND_TIMEOUT		1500	/* 1.5s default */
#ifdef _WIN32
# define DEFAULT_SERVICE_EXECUTABLE	TEXT ("ServiceRunner.exe")
#else
# define DEFAULT_SERVICE_EXECUTABLE	TEXT ("ServiceRunner")
#endif
#define DEFAULT_SERVICE_POLL		250		/* 1/4s default */
#define DEFAULT_START_TIMEOUT		30000	/* 30s default */
#define DEFAULT_STOP_TIMEOUT		2000	/* 2s default */

const TCHAR *CSettings::GetConnectionPipe () const {
	return GetConnectionPipe (ServiceDefaultConnectionPipe ());
}

int CSettings::GetConnectTimeout () const {
	return GetConnectTimeout (DEFAULT_CONNECT_TIMEOUT);
}

bool CSettings::IsDisplayAlerts () const {
	return IsDisplayAlerts (DEFAULT_DISPLAY_ALERTS);
}

int CSettings::GetHeartbeatTimeout () const {
	return GetHeartbeatTimeout (DEFAULT_HEARTBEAT_TIMEOUT);
}

const TCHAR *CSettings::GetInputPipePrefix () const {
	return GetInputPipePrefix (DEFAULT_INPUT_PIPE_PREFIX);
}

const TCHAR *CSettings::GetLogConfiguration () const {
	return GetLogConfiguration (DEFAULT_LOG_CONFIGURATION);
}

int CSettings::GetMaxPipeAttempts () const {
	return GetMaxPipeAttempts (DEFAULT_MAX_PIPE_ATTEMPTS);
}

const TCHAR *CSettings::GetOutputPipePrefix () const {
	return GetOutputPipePrefix (DEFAULT_OUTPUT_PIPE_PREFIX);
}

int CSettings::GetSendTimeout () const {
	return GetSendTimeout (DEFAULT_SEND_TIMEOUT);
}

class CServiceExecutableDefault : public CAbstractSettingProvider {
protected:
	TCHAR *CalculateString () const {
		TCHAR *pszExecutable = NULL;
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
			pszExecutable = _tcsdup (szPath);
		} while (false);
		if (pszExecutable) {
			LOGDEBUG (TEXT ("Default service executable ") << pszExecutable);
		} else {
			pszExecutable = _tcsdup (DEFAULT_SERVICE_EXECUTABLE);
		}
		return pszExecutable;
	}
};

static CServiceExecutableDefault g_oServiceExecutableDefault;

const TCHAR *CSettings::GetServiceExecutable () const {
	return GetServiceExecutable (&g_oServiceExecutableDefault);
}

const TCHAR *CSettings::GetServiceName () const {
	return GetServiceName (ServiceDefaultServiceName ());
}

int CSettings::GetServicePoll () const {
	return GetServicePoll (DEFAULT_SERVICE_POLL);
}

int CSettings::GetStartTimeout () const {
	return GetStartTimeout (DEFAULT_START_TIMEOUT);
}

int CSettings::GetStopTimeout () const {
	return GetStopTimeout (DEFAULT_STOP_TIMEOUT);
}
