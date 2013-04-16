/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Settings.h"
#include <Util/Atomic.h>
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
#   define DEFAULT_PIPE_FOLDER			TEXT ("/tmp/")
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
#define DEFAULT_REQUEST_TIMEOUT		2		/* 2x send timeout */
#define DEFAULT_SEND_TIMEOUT		1500	/* 1.5s default */
#ifdef _WIN32
# define DEFAULT_SERVICE_EXECUTABLE	TEXT ("ServiceRunner.exe")
#else
# define DEFAULT_SERVICE_EXECUTABLE	TEXT ("ServiceRunner")
#endif
#define DEFAULT_SERVICE_POLL		250		/* 1/4s default */
#define DEFAULT_START_TIMEOUT		30000	/* 30s default */
#define DEFAULT_STOP_TIMEOUT		2000	/* 2s default */

/// Returns the name of the pipe for sending connection messages to the JVM host process (service).
///
/// @return the pipe name
const TCHAR *CSettings::GetConnectionPipe () const {
	return GetConnectionPipe (ServiceDefaultConnectionPipe ());
}

/// Returns the connection timeout in milliseconds.
///
/// @return the timeout in milliseconds
int CSettings::GetConnectTimeout () const {
	return GetConnectTimeout (DEFAULT_CONNECT_TIMEOUT);
}

/// Returns whether "alert"s are enabled. These are attached to a system tray icon in Windows.
///
/// @return TRUE if enabled, FALSE to disable
bool CSettings::IsDisplayAlerts () const {
	return IsDisplayAlerts (DEFAULT_DISPLAY_ALERTS);
}

/// Returns the heartbeat timeout in milliseconds. Each end of the connection should expect there
/// to be no more than this length of time between messages; requiring each to send messages at
/// least that often (using the no-op heartbeat message if there is no genuine traffic).
///
/// @return the timeout in milliseconds
int CSettings::GetHeartbeatTimeout () const {
	return GetHeartbeatTimeout (DEFAULT_HEARTBEAT_TIMEOUT);
}

/// Returns the prefix to use at the start of pipe names for inbound traffic. This should include
/// a path in the case of Posix and named pipes, or the \\\\.\\pipe\\ prefix in the case of Win32.
///
/// @return the pipe prefix
const TCHAR *CSettings::GetInputPipePrefix () const {
	return GetInputPipePrefix (DEFAULT_INPUT_PIPE_PREFIX);
}

/// Returns the full path to the LOG4CXX configuration file.
///
/// @return the path
const TCHAR *CSettings::GetLogConfiguration () const {
	return GetLogConfiguration (DEFAULT_LOG_CONFIGURATION);
}

/// Returns the maximum number of times to try and create the pipes. Pipe creation is retried to
/// allow different names to be generated to avoid naming conflicts with other processes.
///
/// @return the number of attempts
int CSettings::GetMaxPipeAttempts () const {
	return GetMaxPipeAttempts (DEFAULT_MAX_PIPE_ATTEMPTS);
}

/// Returns the prefix to use at the start of pipe names for outgoing traffic. This should include
/// a path in the case of Posix and named pipes, or the \\\\.\\pipe\\ prefix in the case of Win32.
///
/// @return the pipe prefix
const TCHAR *CSettings::GetOutputPipePrefix () const {
	return GetOutputPipePrefix (DEFAULT_OUTPUT_PIPE_PREFIX);
}

/// Returns the timeout for a request to the Java stack in milliseconds.
///
/// @return the timeout in milliseconds
int CSettings::GetRequestTimeout () const {
	return GetRequestTimeout (DEFAULT_REQUEST_TIMEOUT * GetSendTimeout ());
}

/// Returns the timeout for sending messages to the Java stack in milliseconds.
///
/// @return the timeout in milliseconds
int CSettings::GetSendTimeout () const {
	return GetSendTimeout (DEFAULT_SEND_TIMEOUT);
}

/// Locates a path to the ServiceRunner executable.
class CServiceExecutableDefault : public CAbstractSettingProvider {
private:

	/// Locates the executable based on the service name.
	///
	/// @param[in] pszServiceName name of the service
	/// @return the path to the executable, or NULL if none is found (or an error occurs)
	static TCHAR *FindServiceExecutable (const TCHAR *pszServiceName) {
		if (!pszServiceName) return NULL;
		TCHAR *pszExecutable = NULL;
#ifdef _WIN32
		HRESULT hr;
		HKEY hkey;
		if ((hr = RegOpenKeyEx (HKEY_LOCAL_MACHINE, TEXT ("SYSTEM\\CurrentControlSet\\services"), 0, KEY_READ, &hkey)) == ERROR_SUCCESS) {
			TCHAR szImage[MAX_PATH];
			DWORD cbImage = sizeof (szImage);
			if ((hr = RegGetValue (hkey, pszServiceName, TEXT ("ImagePath"), RRF_RT_REG_SZ, NULL, szImage, &cbImage)) == ERROR_SUCCESS) {
				if (szImage[0] == '\"') {
					int cchImage = (cbImage / sizeof (TCHAR)) - 1;
					assert (cchImage >= 2);
					assert (!szImage[cchImage]);
					assert (szImage[cchImage - 1] == '\"');
					szImage[cchImage - 1] = 0;
					pszExecutable = _tcsdup (szImage + 1);
				} else {
					pszExecutable = _tcsdup (szImage);
				}
			}
			RegCloseKey (hkey);
		} else {
			LOGWARN (TEXT ("Couldn't open HKLM\\SYSTEM\\CurrentControlSet\\services registry key, error ") << hr);
		}
#else /* ifdef _WIN32 */
		// TODO: Can the daemon process be found from the Sys V style service wrappers?
#endif /* ifdef _WIN32 */
		return pszExecutable;
	}

	/// Locates the executable based on searching the folder of the current path.
	///
	/// @return the path to the executable, or NULL if none is found (or an error occurs)
	static TCHAR *FindLocalExecutable () {
		TCHAR szPath[260];
		if (!CProcess::GetCurrentModule (szPath, (sizeof (szPath) / sizeof (TCHAR)) - _tcslen (DEFAULT_SERVICE_EXECUTABLE))) {
			LOGWARN (TEXT ("Couldn't get current module filename, error ") << GetLastError ());
			return NULL;
		}
		TCHAR *pszFolder = _tcsrchr (szPath, PATH_CHAR);
		if (pszFolder) {
			pszFolder++;
		} else {
			pszFolder = szPath;
		}
		// The memcpy is safe because we have a short buffer length to GetCurrentModule to guarantee room
		memcpy (pszFolder, DEFAULT_SERVICE_EXECUTABLE, (_tcslen (DEFAULT_SERVICE_EXECUTABLE) + 1) * sizeof (TCHAR));
		LOGDEBUG (TEXT ("Executable ") << szPath);
#ifdef _WIN32
		HANDLE hFile = CreateFile (szPath, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, 0, NULL);
		if (!hFile) {
			LOGWARN (TEXT ("Couldn't open executable file ") << szPath << TEXT (", error ") << GetLastError ());
			return NULL;
		}
		if (!GetFinalPathNameByHandle (hFile, szPath, MAX_PATH, FILE_NAME_NORMALIZED)) {
			LOGWARN (TEXT ("Couldn't get normalized executable name, error ") << GetLastError ());
			CloseHandle (hFile);
			return NULL;
		}
		CloseHandle (hFile);
		if (!_tcsncmp (szPath, TEXT ("\\\\?\\UNC\\"), 8)) {
			LOGDEBUG (TEXT ("Removing \\\\?\\UNC\\ prefix"));
			memmove (szPath + 2, szPath + 8, (_tcslen (szPath + 8) + 1) * sizeof (TCHAR));
		} else if (!_tcsncmp (szPath, TEXT ("\\\\?\\"), 4)) {
			LOGDEBUG (TEXT ("Removing \\\\?\\ prefix"));
			memmove (szPath, szPath + 4, (_tcslen (szPath + 4) + 1) * sizeof (TCHAR));
		}
#else /* ifdef _WIN32 */
		int file = open (szPath, O_RDONLY);
		if (file <= 0) {
			LOGWARN (TEXT ("Couldn't open executable file ") << szPath << TEXT (", error ") << GetLastError ());
			return NULL;
		}
		close (file);
#endif /* ifdef _WIN32 */
		return _tcsdup (szPath);
	}

protected:

	/// Locates the directory of the current module, and looks alongside it for the service executable.
	///
	/// @param[in] poSettings the owning settings object, an instance of CSettings
	/// @return the path to the executable, or a default best guess if none was found
	TCHAR *CalculateString (const CAbstractSettings *poSettings) const {
		TCHAR *pszExecutable = NULL;
		do {
			if ((pszExecutable = FindServiceExecutable (((const CSettings*)poSettings)->GetServiceName ())) != NULL) break;
			if ((pszExecutable = FindLocalExecutable ()) != NULL) break;
		} while (false);
		if (pszExecutable) {
			LOGDEBUG (TEXT ("Default service executable ") << pszExecutable);
		} else {
			pszExecutable = _tcsdup (DEFAULT_SERVICE_EXECUTABLE);
		}
		return pszExecutable;
	}
};

/// Instance of the provider to retrieve the default service executable path.
static CServiceExecutableDefault g_oServiceExecutableDefault;

/// Returns the path to the ServiceRunner executable.
///
/// @return the path
const TCHAR *CSettings::GetServiceExecutable () const {
	return GetServiceExecutable (&g_oServiceExecutableDefault);
}

/// Returns the name of the service the JVM host is installed as. The exact meaning of a service
/// is O/S dependent.
///
/// @return the service name
const TCHAR *CSettings::GetServiceName () const {
	return GetServiceName (ServiceDefaultServiceName ());
}

/// Returns the polling period in milliseconds for querying the state of a service (i.e. running
/// or stopped).
///
/// @return the period in milliseconds
int CSettings::GetServicePoll () const {
	return GetServicePoll (DEFAULT_SERVICE_POLL);
}

#ifndef _WIN32

/// Locates the service query command.
class CServiceQueryDefault : public CAbstractSettingProvider {
protected:
	/// Constructs the service control command by adding "status" to the default command.
	///
	/// @param[in] poSettings the owning settings object, an instance of CSettings
	/// @return the service control query string
	TCHAR *CalculateString (const CAbstractSettings *poSettings) const {
		return ServiceCreateQueryCmd (((const CSettings*)poSettings)->GetServiceName ());
	}
};

/// Instance of the provider to retrieve the default service query command.
static CServiceQueryDefault g_oServiceQueryDefault;

/// Returns the command that should be used to query the status of the service in the Posix
/// environment. For example "service og-language status".
///
/// @return the command to use, or NULL if there is none
const TCHAR *CSettings::GetServiceQueryCmd () const {
	return GetServiceQueryCmd (&g_oServiceQueryDefault);
}

/// Locates the service start command.
class CServiceStartDefault : public CAbstractSettingProvider {
protected:
	/// Constructs the service control command by adding "start" to the default command.
	//
	/// @param[in] poSettings the owning settings object, an instance of CSettings
	/// @return the service control start string
	TCHAR *CalculateString (const CAbstractSettings *poSettings) const {
		return ServiceCreateStartCmd (((const CSettings*)poSettings)->GetServiceName ());
	}
};

/// Instance of the provider to retrieve the default service start command.
static CServiceStartDefault g_oServiceStartDefault;

/// Returns the command that should be used to start the service in the Posix environment. For example
/// "service og-language start"
///
/// @return the command to use, or NULL if there is none
const TCHAR *CSettings::GetServiceStartCmd () const {
	return GetServiceStartCmd (&g_oServiceStartDefault);
}

/// Locates the service stop command.
class CServiceStopDefault : public CAbstractSettingProvider {
protected:
	/// Constructs the service control command by adding "stop" to the default command.
	///
	/// @param[in] poSettings the owning settings object, an instance of CSettings
	/// @return the service control stop string
	TCHAR *CalculateString (const CAbstractSettings *poSettings) const {
		return ServiceCreateStopCmd (((const CSettings*)poSettings)->GetServiceName ());
	}
};

/// Instance of the provider to retrieve the default service stop command.
static CServiceStopDefault g_oServiceStopDefault;

/// Returns the command that should be used to stop the service in the Posix environment. For example
/// "service og-language stop"
///
/// @return the command to use, or NULL if there is none
const TCHAR *CSettings::GetServiceStopCmd () const {
	return GetServiceStopCmd (&g_oServiceStopDefault);
}

#endif /* ifndef _WIN32 */

/// Returns the time to wait for a service or executable to startup in milliseconds.
///
/// @return the timeout in milliseconds
int CSettings::GetStartTimeout () const {
	return GetStartTimeout (DEFAULT_START_TIMEOUT);
}

/// Returns the time to wait for a service or executable to stop on being sent a terminate.
///
/// @return the timeout in milliseconds.
int CSettings::GetStopTimeout () const {
	return GetStopTimeout (DEFAULT_STOP_TIMEOUT);
}
