/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Settings.h"
#include <Util/File.h>
#include <Util/Process.h>
#include <Util/Quote.h>

LOGGING(com.opengamma.language.service.Settings);

#ifndef DEFAULT_CONNECTION_TIMEOUT
# define DEFAULT_CONNECTION_TIMEOUT	3000	/* 3s default */
#endif /* ifndef DEFAULT_CONNECTION_TIMEOUT */
#ifndef DEFAULT_BUSY_TIMEOUT
# define DEFAULT_BUSY_TIMEOUT		2000	/* 2s default */
#endif /* ifndef DEFAULT_BUSY_TIMEOUT */
#ifndef DEFAULT_IDLE_TIMEOUT
# define DEFAULT_IDLE_TIMEOUT		300000	/* 5m default */
#endif /* ifndef DEFAULT_IDLE_TIMEOUT */
#ifndef DEFAULT_JVM_LIBRARY
# ifdef _WIN32
#  define DEFAULT_JVM_LIBRARY		TEXT ("jvm.dll")
# else /* ifdef _WIN32 */
#  define DEFAULT_JVM_LIBRARY		TEXT ("jvm.so")
# endif /* ifdef _WIN32 */
#endif /* ifndef DEFAULT_JVM_LIBRARY */
#ifndef DEFAULT_LOG_CONFIGURATION
# define DEFAULT_LOG_CONFIGURATION	NULL
#endif /* ifndef DEFAULT_LOG_CONFIGURATION */
#ifdef _WIN32
# ifndef DEFAULT_SDDL
#  define DEFAULT_SDDL				NULL
# endif /* ifndef DEFAULT_SDDL */
#endif /* ifdef _WIN32 */
#ifndef DEFAULT_SERVICE_NAME
# define DEFAULT_SERVICE_NAME		TEXT ("OpenGammaLanguageAPI")
#endif /* ifndef DEFAULT_SERVICE_NAME */
#ifndef DEFAULT_CONNECTION_PIPE
# ifndef DEFAULT_PIPE_NAME
#  define DEFAULT_PIPE_NAME			TEXT ("Connection")
# endif /* ifndef DEFAULT_PIPE_NAME */
# ifdef _WIN32
#  define DEFAULT_CONNECTION_PIPE	TEXT ("\\\\.\\pipe\\") DEFAULT_SERVICE_NAME TEXT ("-") DEFAULT_PIPE_NAME
# else /* ifdef _WIN32 */
#  ifndef DEFAULT_PIPE_FOLDER
#   define DEFAULT_PIPE_FOLDER		TEXT ("/var/run/OG-Language/")
#  endif /* ifndef DEFAULT_PIPE_FOLDER */
#  define DEFAULT_CONNECTION_PIPE	DEFAULT_PIPE_FOLDER DEFAULT_PIPE_NAME TEXT (".sock")
# endif /* ifdef _WIN32 */
#endif /* ifndef DEFAULT_CONNECTION_PIPE */

/// Returns the default name of the pipe for incoming client connections.
///
/// @return the pipe name
const TCHAR *ServiceDefaultConnectionPipe () {
	return DEFAULT_CONNECTION_PIPE;
}

/// Returns the default name of the service.
///
/// @return the service name
const TCHAR *ServiceDefaultServiceName () {
	return DEFAULT_SERVICE_NAME;
}

/// Locates the JVM library by inspecting the registry or making other educated guesses
class CJvmLibraryDefault : public CAbstractSettingProvider {
private:

#ifdef _WIN32
	/// Checks for a JVM library defined in the registry at HKLM\\SOFTWARE\\JavaSoft\\Java Runtime Environment.
	/// Registry mapping under WOW64 will make sure that a 32-bit process sees a 32-bit JVM and a 64-bit process
	/// sees a 64-bit JVM.
	///
	/// @return the path to the JVM DLL, or NULL if there is none (or an error occurs)
	static TCHAR *SearchRegistry () {
		HKEY hkeyJRE;
		HRESULT hr;
		if ((hr = RegOpenKeyEx (HKEY_LOCAL_MACHINE, TEXT ("SOFTWARE\\JavaSoft\\Java Runtime Environment"), 0, KEY_READ, &hkeyJRE)) != ERROR_SUCCESS) {
			LOGDEBUG (TEXT ("No JRE registry key"));
			return NULL;
		}
		TCHAR szVersion[16];
		DWORD cbVersion = sizeof (szVersion);
		if ((hr = RegGetValue (hkeyJRE, NULL, TEXT ("CurrentVersion"), RRF_RT_REG_SZ, NULL, szVersion, &cbVersion)) != ERROR_SUCCESS) {
			RegCloseKey (hkeyJRE);
			LOGWARN (TEXT ("No CurrentVersion value in JRE key"));
			return NULL;
		}
		LOGDEBUG (TEXT ("Found JRE v") << szVersion);
		TCHAR szPath[MAX_PATH];
		DWORD cbPath = sizeof (szPath);
		if ((hr = RegGetValue (hkeyJRE, szVersion, TEXT ("RuntimeLib"), RRF_RT_REG_SZ, NULL, szPath, &cbPath)) != ERROR_SUCCESS) {
			RegCloseKey (hkeyJRE);
			LOGWARN (TEXT ("No RuntimeLib found for JRE v") << szVersion);
			return NULL;
		}
		LOGINFO (TEXT ("Runtime library ") << szPath << TEXT (" found from registry"));
		RegCloseKey (hkeyJRE);
		return _tcsdup (szPath);
	}
#endif /* ifndef _WIN32 */

	/// Checks for a JVM library from the registry (Windows)
	///
	/// @return the path to the JVM DLL, a default best guess, or NULL if there is a problem
	TCHAR *CalculateString () const {
		TCHAR *pszLibrary = NULL;
		do {
#ifdef _WIN32
			if ((pszLibrary = SearchRegistry ()) != NULL) break;
#else /* ifdef _WIN32 */
			// TODO: Is there anything reasonably generic? The path on my Linux box included a processor (e.g. amd64) reference
#endif /* ifdef _WIN32 */
		} while (false);
		if (pszLibrary == NULL) {
			LOGDEBUG ("No default JVM libraries found");
			pszLibrary = _tcsdup (DEFAULT_JVM_LIBRARY);
		}
		return pszLibrary;
	}
};

/// Instance of the provider to retrieve the JVM library path.
static CJvmLibraryDefault g_oJvmLibraryDefault;

/// Returns the path to the JVM library.
///
/// @return the path
const TCHAR *CSettings::GetJvmLibrary () const {
	return GetJvmLibrary (&g_oJvmLibraryDefault);
}

/// Returns the minimum heap size for the JVM.
///
/// @return the minimum heap size in Mb
unsigned long CSettings::GetJvmMinHeap () const {
	return GetJvmMinHeap (256);
}

/// Returns the maximum heap size for the JVM
///
/// @return the maximum heap size in Mb
unsigned long CSettings::GetJvmMaxHeap () const {
	return GetJvmMaxHeap (512);
}

/// Enumerate the system properties to be passed to the JVM.
///
/// @param[in] poEnum enumerator to receive the key/value pairs
void CSettings::GetJvmProperties (const CEnumerator *poEnum) const {
	Enumerate (SETTINGS_JVM_PROPERTY TEXT ("."), poEnum);
}

/// Returns the name of the pipe for incoming client connections
///
/// @return the pipe name
const TCHAR *CSettings::GetConnectionPipe () const {
	return GetConnectionPipe (ServiceDefaultConnectionPipe ());
}

/// Returns the timeout for reading connection messages from incoming client connections
///
/// @return the timeout in milliseconds
unsigned long CSettings::GetConnectionTimeout () const {
	return GetConnectionTimeout (DEFAULT_CONNECTION_TIMEOUT);
}

/// Locates the path containing all of the Java stack resources by working backwards from the folder
/// containing the service executable until client.jar is found.
class CJarPathDefault : public CAbstractSettingProvider {
protected:
#define CLIENT_JAR_NAME		TEXT ("client.jar")
#define CLIENT_JAR_LEN		10
	/// Scans backwards from the service executable's folder until it finds one containing client.jar
	///
	/// @return the path, or a default best guess if none is found
	TCHAR *CalculateString () const {
		TCHAR *pszJarPath = NULL;
		// Scan backwards from the module to find a path which has Client.jar in. This works if all of the
		// JARs and DLLs are in the same folder, but also in the case of a build system where we have sub-folders
		// for the different configurations/platforms.
		TCHAR szPath[256 + CLIENT_JAR_LEN]; // Guarantee room for Client.jar at the end
		if (CProcess::GetCurrentModule (szPath, 256)) {
			LOGDEBUG (TEXT ("Module = ") << szPath);
			TCHAR *pszEnd = _tcsrchr (szPath, PATH_CHAR);
			while (pszEnd) {
#ifdef _DEBUG
				*pszEnd = 0;
				LOGDEBUG (TEXT ("Testing path ") << szPath);
				*pszEnd = PATH_CHAR;
#endif
				memcpy (pszEnd + 1, CLIENT_JAR_NAME, (CLIENT_JAR_LEN + 1) * sizeof (TCHAR));
#ifdef _WIN32
				HANDLE hFile = CreateFile (szPath, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, 0, NULL);
				if (hFile == INVALID_HANDLE_VALUE) {
#else
				int nFile = open (szPath, O_RDONLY);
				if (nFile <= 0) {
#endif
					int ec = GetLastError ();
					if (ec != ENOENT) {
						LOGWARN (TEXT ("Couldn't scan for ") << szPath << TEXT (", error ") << ec);
						break;
					}
					*pszEnd = 0;
				} else {
#ifdef _WIN32
					CloseHandle (hFile);
#else
					close (nFile);
#endif
					*pszEnd = 0;
					LOGINFO (TEXT ("Found path ") << szPath << TEXT (" containing ") << CLIENT_JAR_NAME);
					pszJarPath = _tcsdup (szPath);
					break;
				}
				pszEnd = _tcsrchr (szPath, PATH_CHAR);
			}
			if (!pszJarPath) {
				LOGWARN (TEXT ("Couldn't find client library Jar on module path"));
				pszJarPath = _tcsdup (TEXT ("."));
			}
		} else {
			pszJarPath = _tcsdup (TEXT ("."));
		}
		return pszJarPath;
	}
};

/// Instance of the provider to get the default path for the Java resources.
static CJarPathDefault g_oJarPathDefault;

/// Returns the path containing the Java stack resources.
///
/// @return the path
const TCHAR *CSettings::GetJarPath () const {
	return GetJarPath (&g_oJarPathDefault);
}

/// Locates the ext folder by searching for the client.jar
class CExtPathDefault : public CAbstractSettingProvider {
protected:
	TCHAR *CalculateString () const {
		const TCHAR *pszJarPath = g_oJarPathDefault.GetString ();
		if (!pszJarPath) {
			LOGERROR (TEXT ("No JAR path to base EXT from"));
			return NULL;
		}
		size_t cchJarPath = _tcslen (pszJarPath);
		TCHAR *pszExtPath = new TCHAR[cchJarPath + 5];
		if (!pszExtPath) {
			LOGFATAL (TEXT ("Out of memory"));
			return NULL;
		}
		memcpy (pszExtPath, pszJarPath, cchJarPath * sizeof (TCHAR));
		memcpy (pszExtPath + cchJarPath, TEXT (PATH_CHAR_STR) TEXT ("ext"), 5 * sizeof (TCHAR));
		LOGINFO (TEXT ("Found EXT path ") << pszExtPath);
		return pszExtPath;
	}
};

/// Instance of the provider to get the default path for the EXT resources.
static CExtPathDefault g_oExtPathDefault;

/// Returns the path containing the language binding resources.
///
/// @return the path
const TCHAR *CSettings::GetExtPath () const {
	return GetExtPath (&g_oExtPathDefault);
}

/// Returns the path where Fudge annotation cache files should be written. The default is the JAR path, but this
/// may not be writable in some installations.
///
/// @return the annotation cache path
const TCHAR *CSettings::GetAnnotationCache () const {
	return GetAnnotationCache (GetJarPath ());
}

/// Returns the timeout for polling the JVM's busy status; i.e. how often feedback from the service will get reported
/// during service start/stop handling.
///
/// @return the timeout in milliseconds
unsigned long CSettings::GetBusyTimeout () const {
	return GetBusyTimeout (DEFAULT_BUSY_TIMEOUT);
}

/// Returns the path to the log configuration file.
///
/// @return the log configuration file path
const TCHAR *CSettings::GetLogConfiguration () const {
	return GetLogConfiguration (DEFAULT_LOG_CONFIGURATION);
}

/// Returns the idle timeout - if no connections are received for this time the service will shutdown.
///
/// @return the timeout in milliseconds
unsigned long CSettings::GetIdleTimeout () const {
	return GetIdleTimeout (DEFAULT_IDLE_TIMEOUT);
}

/// Returns the service name.
///
/// @return the service name
const TCHAR *CSettings::GetServiceName () const {
	return GetServiceName (ServiceDefaultServiceName ());
}

#ifdef _WIN32
/// Returns the SDDL that should be applied to the process; this is to relax security rights to allow clients to
/// restart a rogue process (if required).
///
/// @return the SDDL string, or NULL to use the system defaults
const TCHAR *CSettings::GetServiceSDDL () const {
	return GetServiceSDDL (DEFAULT_SDDL);
}
#endif /* ifdef _WIN32 */
