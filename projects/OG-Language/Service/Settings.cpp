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
#ifndef _WIN32
# ifndef JVM_LIBRARY_SEARCH_PATH
#  define JVM_LIBRARY_SEARCH_PATH	TEXT ("/usr/lib/jvm")
# endif /* ifndef JVM_LIBRARY_SEARCH_PATH */
# ifndef JVM_LIBRARY_FILE_NAME
#  define JVM_LIBRARY_FILE_NAME		TEXT ("libjvm.so")
# endif /* ifndef JVM_LIBRARY_FILE_NAME */
#endif /* ifndef _WIN32 */
#ifndef DEFAULT_JVM_LIBRARY
# ifdef _WIN32
#  define DEFAULT_JVM_LIBRARY       TEXT ("jvm.dll")
# else /* ifdef _WIN32 */
#  define DEFAULT_JVM_LIBRARY       JVM_LIBRARY_FILE_NAME
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
# ifdef _WIN32
#  define DEFAULT_SERVICE_NAME		TEXT ("OpenGammaLanguageAPI")
# else /* ifdef _WIN32 */
#  define DEFAULT_SERVICE_NAME		TEXT ("og-language")
# endif /* ifdef _WIN32 */
#endif /* ifndef DEFAULT_SERVICE_NAME */
#ifndef _WIN32
# ifndef SERVICE_CTRL_CHK_PREFIX
#  define SERVICE_CTRL_CHK_PREFIX	TEXT ("/etc/init.d/")
# endif /* ifndef SERVICE_CTRL_CHK_PREFIX */
# ifndef SERVICE_CTRL_CHK_SUFFIX
#  define SERVICE_CTRL_CHK_SUFFIX	TEXT ("")
# endif /* ifndef SERVICE_CTRL_CHK_SUFFIX */
# ifndef SERVICE_CTRL_CMD_PREFIX
#  define SERVICE_CTRL_CMD_PREFIX	TEXT ("/sbin/service ")
# endif /* ifndef SERVICE_CTRL_CMD_PREFIX */
# ifndef SERVICE_CTRL_CMD_SUFFIX
#  define SERVICE_CTRL_CMD_SUFFIX	TEXT (" %s > /dev/null")
# endif /* ifdef SERVICE_CTRL_CMD_SUFFIX */
# ifndef SERVICE_CTRL_QUERY_PARAM
#  define SERVICE_CTRL_QUERY_PARAM	TEXT ("status")
# endif /* ifndef SERVICE_CTRL_QUERY_PARAM */
# ifndef SERVICE_CTRL_START_PARAM
#  define SERVICE_CTRL_START_PARAM	TEXT ("start")
# endif /* ifdef SERVICE_CTRL_START_PARAM */
# ifndef SERVICE_CTRL_STOP_PARAM
#  define SERVICE_CTRL_STOP_PARAM	TEXT ("stop")
# endif /* ifdef SERVICE_CTRL_STOP_PARAM */
#endif /* ifndef _WIN32 */
#ifndef DEFAULT_CONNECTION_PIPE
# ifdef _WIN32
#  define DEFAULT_CONNECTION_PIPE	TEXT ("\\\\.\\pipe\\") DEFAULT_SERVICE_NAME TEXT ("-Connection")
# else /* ifdef _WIN32 */
#  ifndef DEFAULT_PIPE_FOLDER
#   define DEFAULT_PIPE_FOLDER		TEXT ("/var/run/opengamma/")
#  endif /* ifndef DEFAULT_PIPE_FOLDER */
#  define DEFAULT_CONNECTION_PIPE	DEFAULT_PIPE_FOLDER DEFAULT_SERVICE_NAME TEXT (".sock")
# endif /* ifdef _WIN32 */
#endif /* ifndef DEFAULT_CONNECTION_PIPE */
#ifndef DEFAULT_JVM_MIN_HEAP
# define DEFAULT_JVM_MIN_HEAP		256
#endif /* ifndef DEFAULT_JVM_MIN_HEAP */
#ifndef DEFAULT_JVM_MAX_HEAP
# define DEFAULT_JVM_MAX_HEAP		512
#endif /* ifndef DEFAULT_JVM_MAX_HEAP */
#ifndef DEFAULT_PID_FILE
# define DEFAULT_PID_FILE			DEFAULT_PIPE_FOLDER TEXT ("og-language.pid")
#endif /* ifndef DEFAULT_PID_FILE */

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

#ifndef _WIN32

static size_t _maxstrlen (const TCHAR *pszA, const TCHAR *pszB) {
	size_t cchA = _tcslen (pszA);
	size_t cchB = _tcslen (pszB);
	if (cchA < cchB) {
		return cchB;
	} else {
		return cchA;
	}
}

/// Creates a service control string by checking for a file which is based on the service name and
/// then composing the service name with the operation. For example it might search for
/// "/etc/init.d/<service>" and then execute "service <service> <command>"
///
/// This will allocate memory for the returned string if successful. The caller must release this
/// when done.
///
/// @param[in] pszServiceName the service name, not NULL
/// @param[in] pszOperation the operation string, not NULL
/// @return the service control command, or NULL if there is a problem
static TCHAR *_ServiceCreateControl (const TCHAR *pszServiceName, const TCHAR *pszOperation) {
	TCHAR *pszTemp1 = NULL;
	TCHAR *pszResult = NULL;
	do {
		size_t cb = (_maxstrlen (SERVICE_CTRL_CHK_PREFIX, SERVICE_CTRL_CMD_PREFIX) + _tcslen (pszServiceName) + _maxstrlen (SERVICE_CTRL_CHK_SUFFIX, SERVICE_CTRL_CMD_SUFFIX) + 1) * sizeof (TCHAR);
		pszTemp1 = (TCHAR*)malloc (cb);
		if (!pszTemp1) {
			LOGFATAL (TEXT ("Out of memory"));
			break;
		}
		StringCbPrintf (pszTemp1, cb, TEXT ("%s%s%s"), SERVICE_CTRL_CHK_PREFIX, pszServiceName, SERVICE_CTRL_CHK_SUFFIX);
		LOGDEBUG (TEXT ("Testing for ") << pszTemp1);
		struct stat statBuf;
		if (stat (pszTemp1, &statBuf)) {
			LOGWARN (TEXT ("Couldn't stat ") << pszTemp1);
			break;
		}
		LOGDEBUG (TEXT ("Found ") << pszTemp1 << TEXT (" with mode ") << statBuf.st_mode);
		if (!(statBuf.st_mode & (S_IXUSR | S_IXGRP | S_IXOTH))) {
			LOGWARN (TEXT ("Service script ") << pszTemp1 << TEXT (" doesn't have execute bit set"));
			break;
		}
		StringCbPrintf (pszTemp1, cb, TEXT ("%s%s%s"), SERVICE_CTRL_CMD_PREFIX, pszServiceName, SERVICE_CTRL_CMD_SUFFIX);
		LOGDEBUG (TEXT ("Forming service operation \"") << pszTemp1 << TEXT ("\" with ") << pszOperation);
		cb = (_tcslen (pszTemp1) + _tcslen (pszOperation) - 1) * sizeof (TCHAR);
		pszResult = (TCHAR*)malloc (cb);
		if (!pszResult) {
			LOGFATAL (TEXT ("Out of memory"));
			break;
		}
		StringCbPrintf (pszResult, cb, pszTemp1, pszOperation);
		LOGINFO (TEXT ("Found service control command: ") << pszResult);
	} while (false);
	if (pszTemp1) free (pszTemp1);
	return pszResult;
}

TCHAR *ServiceCreateQueryCmd (const TCHAR *pszServiceName) {
	return _ServiceCreateControl (pszServiceName, SERVICE_CTRL_QUERY_PARAM);
}

TCHAR *ServiceCreateStartCmd (const TCHAR *pszServiceName) {
	return _ServiceCreateControl (pszServiceName, SERVICE_CTRL_START_PARAM);
}

TCHAR *ServiceCreateStopCmd (const TCHAR *pszServiceName) {
	return _ServiceCreateControl (pszServiceName, SERVICE_CTRL_STOP_PARAM);
}

#endif /* ifndef _WIN32 */

/// Extracts the numeric version fragment from a string.
///
/// @param[in] pszVersion the version string, e.g. "1.7"
/// @param[in] nFragment the version fragment, e.g. 0 for the major version number, 1 for the minor
/// @return the version number
int JavaVersionFragment (const TCHAR *pszVersion, int nFragment) {
	int nValue;
	size_t cch = _tcslen (pszVersion), i = 0;
	do {
		nValue = 0;
		while ((i < cch) && (pszVersion[i] != '.')) {
			if ((pszVersion[i] >= '0') && (pszVersion[i] <= '9')) {
				nValue = (nValue * 10) + (pszVersion[i] - '0');
			}
			i++;
		}
		if (i < cch) i++;
	} while (nFragment-- > 0);
	return nValue;
}

/// Locates the JVM library by inspecting the registry or making other educated guesses
class CJvmLibraryDefault : public CAbstractSettingProvider {
private:

#ifdef _WIN32

	/// Checks for a valid JVM library indexed with a given version code. The default Java installation will
	/// write the correct version code to the CurrentVersion value which will be used. In the future we might
	/// want to load a specific JVM (e.g. if a newer one raises compatability issues)
	///
	/// @param[in] hkeyPublisher the JVM publisher key, not NULL
	/// @param[in] pszVersion the version string, not NULL
	/// @return the path to the JVM DLL if found, or NULL if there is none
	static TCHAR *SearchJavaVersion (HKEY hkeyPublisher, const TCHAR *pszVersion) {
		TCHAR szPath[MAX_PATH];
		DWORD cbPath = sizeof (szPath);
		LOGDEBUG (TEXT ("Trying JRE ") << pszVersion);
		if (RegGetValue (hkeyPublisher, pszVersion, TEXT ("RuntimeLib"), RRF_RT_REG_SZ, NULL, szPath, &cbPath) != ERROR_SUCCESS) {
			return NULL;
		}
		HANDLE hFile = CreateFile (szPath, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
		// JRE 1.7 32-bit doesn't seem to have this bug but 64-bit version does
		if (hFile == INVALID_HANDLE_VALUE) {
			// JDK1.7 64-bit puts the wrong path into the registry. It ends \client\jvm.dll but should be \server\jvm.dll
			int cchPath = _tcslen (szPath);
			if ((cchPath > 15) && !_tcscmp (szPath + cchPath - 15, TEXT ("\\client\\jvm.dll"))) {
				LOGDEBUG (TEXT ("Applying hack for broken JDK installer"));
				memcpy (szPath + cchPath - 14, TEXT ("server"), sizeof (TCHAR) * 6);
				hFile = CreateFile (szPath, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
				if (hFile == INVALID_HANDLE_VALUE) {
					LOGWARN (TEXT ("JVM runtime ") << szPath << TEXT (" can't be opened (modified value used), error ") << GetLastError ());
				} else {
					CloseHandle (hFile);
				}
			} else {
				LOGWARN (TEXT ("JVM runtime ") << szPath << TEXT (" can't be opened, error ") << GetLastError ());
			}
		} else {
			CloseHandle (hFile);
		}
		return _tcsdup (szPath);
	}

	/// Checks for a JVM library defined in the registry at HKLM\\SOFTWARE\\<publisher>\\Java Runtime Environment.
	/// Registry mapping under WOW64 will make sure that a 32-bit process sees a 32-bit JVM and a 64-bit process
	/// sees a 64-bit JVM.
	///
	/// @param[in] pszPublisher publisher code to search under, never NULL
	/// @return the path to the JVM DLL, or NULL if there is none (or an error occurs)
	static TCHAR *SearchRegistry (const TCHAR *pszPublisher) {
		HKEY hkeyJRE;
		TCHAR sz[256];
		StringCbPrintf (sz, sizeof (sz), TEXT ("SOFTWARE\\%s\\Java Runtime Environment"), pszPublisher);
		if (RegOpenKeyEx (HKEY_LOCAL_MACHINE, sz, 0, KEY_READ, &hkeyJRE) != ERROR_SUCCESS) {
			LOGDEBUG (TEXT ("No JRE registry key for ") << pszPublisher);
			return NULL;
		}
		DWORD cbVersion;
		TCHAR *pszPath = NULL;
		do {
			// Try the published default
			cbVersion = sizeof (sz);
			if (RegGetValue (hkeyJRE, NULL, TEXT ("CurrentVersion"), RRF_RT_REG_SZ, NULL, sz, &cbVersion) == ERROR_SUCCESS) {
				LOGDEBUG (TEXT ("Found JRE v") << sz << TEXT (" from ") << pszPublisher);
				int nMajorVersion = JavaVersionFragment (sz, 0);
				if ((nMajorVersion > 1) || ((nMajorVersion == 1) && (JavaVersionFragment (sz, 1) >= 7))) {
					pszPath = SearchJavaVersion (hkeyJRE, sz);
					if (pszPath) {
						LOGINFO (TEXT ("Found default JVM ") << pszPath << TEXT (" from ") << pszPublisher << TEXT (" in registry"));
						break;
					} else {
						LOGWARN (TEXT ("No RuntimeLib found for JRE v") << sz << TEXT (" from ") << pszPublisher);
					}
				} else {
					LOGWARN (TEXT ("Version ") << sz << TEXT (" from ") << pszPublisher << TEXT (" not supported"));
				}
			} else {
				LOGDEBUG (TEXT ("No default JVM installed from ") << pszPublisher);
			}
			// Try the Java7 version
			cbVersion = sizeof (sz);
			if (RegGetValue (hkeyJRE, NULL, TEXT ("Java7FamilyVersion"), RRF_RT_REG_SZ, NULL, sz, &cbVersion) == ERROR_SUCCESS) {
				LOGDEBUG (TEXT ("Found JRE v") << sz);
				pszPath = SearchJavaVersion (hkeyJRE, sz);
				if (pszPath) {
					LOGINFO (TEXT ("Found default Java7 JVM ") << pszPath << TEXT (" from ") << pszPublisher << TEXT (" in registry"));
					break;
				} else {
					LOGWARN (TEXT ("No RuntimeLib found for JRE v") << sz << TEXT (" from ") << pszPublisher);
				}
			} else {
				LOGDEBUG (TEXT ("No default Java7 JVM installed from ") << pszPublisher);
			}
			// Try v1.7 regardless
			pszPath = SearchJavaVersion (hkeyJRE, TEXT ("1.7"));
			if (pszPath) {
				LOGINFO (TEXT ("Found non-default JRE v1.7 from ") << pszPublisher);
				break;
			}
		} while (false);
		RegCloseKey (hkeyJRE);
		return pszPath;
	}

	/// Checks for a standard JVM library or one bundled with an OpenGamma installation.
	///
	/// @return the path to the JVM DLL, or NULL if there is none (or an error occurs)
	static TCHAR *SearchRegistry () {
		TCHAR *pszPath;
		do {
			if ((pszPath = SearchRegistry (TEXT ("OpenGammaLtd"))) != NULL) break;
			if ((pszPath = SearchRegistry (TEXT ("JavaSoft"))) != NULL) break;
			LOGWARN (TEXT ("No default JVM or OpenGamma bundled JVM found in the registry"));
		} while (false);
		return pszPath;
	}

#else /* ifndef _WIN32 */

	/// Checks for a JVM library under part of the filesystem.
	///
	/// @param[in] pszPath the folder to start searching from, never NULL
	/// @param[in] nDepth folder depth count - the scan won't go deeper than 16 (in case symlinks create an infinite loop)
	/// @return the path to the JVM library, or NULL if none is found
	static TCHAR *SearchFileSystem (const TCHAR *pszPath, int nDepth = 0) {
		if (nDepth > 16) {
			LOGWARN (TEXT ("Recursion depth limit hit at ") << pszPath);
			return NULL;
		}
		LOGDEBUG (TEXT ("Scanning folder ") << pszPath);
		DIR *dir = opendir (pszPath);
		if (!dir) {
			if (nDepth == 0) {
				LOGWARN (TEXT ("Can't read folder ") << pszPath << TEXT (", error ") << GetLastError ());
			} else {
				LOGDEBUG (TEXT ("Can't read folder ") << pszPath << TEXT (", error ") << GetLastError ());
			}
			return NULL;
		}
		TCHAR *pszLibrary = NULL;
		struct dirent *dp;
		while ((dp = readdir (dir)) != NULL) {
			if (dp->d_name[0] == '.') {
				continue;
			}
			if ((dp->d_type == DT_DIR) || (dp->d_type == DT_LNK)) {
				LOGDEBUG (TEXT ("Recursing into folder ") << dp->d_name);
				size_t cchNewPath = _tcslen (pszPath) + _tcslen (dp->d_name) + 2;
				TCHAR *pszNewPath = new TCHAR[cchNewPath];
				if (!pszNewPath) {
					LOGFATAL (TEXT ("Out of memory"));
					break;
				}
				StringCbPrintf (pszNewPath, cchNewPath * sizeof (TCHAR), TEXT ("%s/%s"), pszPath, dp->d_name);
				pszLibrary = SearchFileSystem (pszNewPath, nDepth + 1);
				delete pszNewPath;
				if (pszLibrary) {
					break;
				}
			} else if (!_tcscmp (dp->d_name, JVM_LIBRARY_FILE_NAME)) {
				LOGINFO (TEXT ("Found ") << JVM_LIBRARY_FILE_NAME << TEXT (" in ") << pszPath);
				size_t cchLibrary = _tcslen (pszPath) + _tcslen (dp->d_name) + 2;
				pszLibrary = new TCHAR[cchLibrary];
				if (!pszLibrary) {
					LOGFATAL (TEXT ("Out of memory"));
					break;
				}
				StringCbPrintf (pszLibrary, cchLibrary * sizeof (TCHAR), TEXT ("%s/%s"), pszPath, dp->d_name);
				// TODO: /proc/self/exe probably isn't portable, but seems to work on Fedora
				CProcess *poCheck = CProcess::Start (TEXT ("/proc/self/exe"), TEXT ("jvm"), pszLibrary);
				if (poCheck) {
					int status = 1;
					LOGDEBUG (TEXT ("Waiting on JVM check"));
					wait (&status);
					poCheck->Terminate (); // Terminate if still running
					delete poCheck;
					LOGDEBUG (TEXT ("JVM check returned ") << status);
					if (status == 0) {
						// Confirmed the JVM version
						break;
					}
				}
				LOGWARN (TEXT ("Couldn't verify JVM version"));
				delete pszLibrary;
				pszLibrary = NULL;
			}
		}
		closedir (dir);
		return pszLibrary;
	}

#endif /* ifndef _WIN32 */

	/// Checks for a JVM library from the registry (Windows), or by scanning the file system (Posix)
	///
	/// @param[in] poSettings ignored
	/// @return the path to the JVM DLL, a default best guess, or NULL if there is a problem
	TCHAR *CalculateString (const CAbstractSettings *poSettings) const {
		TCHAR *pszLibrary = NULL;
		do {
#ifdef _WIN32
			if ((pszLibrary = SearchRegistry ()) != NULL) break;
#else /* ifdef _WIN32 */
			if ((pszLibrary = SearchFileSystem (JVM_LIBRARY_SEARCH_PATH)) != NULL) break;
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
	return GetJvmMinHeap (DEFAULT_JVM_MIN_HEAP);
}

/// Returns the maximum heap size for the JVM
///
/// @return the maximum heap size in Mb
unsigned long CSettings::GetJvmMaxHeap () const {
	return GetJvmMaxHeap (DEFAULT_JVM_MAX_HEAP);
}

/// Enumerate the system properties to be passed to the JVM.
///
/// @param[in] poEnum enumerator to receive the key/value pairs
void CSettings::GetJvmProperties (const CEnumerator *poEnum) const {
	Enumerate (SETTINGS_JVM_PROPERTY TEXT ("."), poEnum);
}

/// Updates a system property that would be passed to the JVM
///
/// @param[in] pszProperty property name, never NULL
/// @param[in] pszValue value to set, or NULL to delete
void CSettings::SetJvmProperty (const TCHAR *pszProperty, const TCHAR *pszValue) {
	TCHAR szProperty[256];
	StringCbPrintf (szProperty, sizeof (szProperty), TEXT ("%s.%s"), SETTINGS_JVM_PROPERTY, pszProperty);
	Set (szProperty, pszValue);
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
	/// @param[in] poSettings ignored
	/// @return the path, or a default best guess if none is found
	TCHAR *CalculateString (const CAbstractSettings *poSettings) const {
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
	TCHAR *CalculateString (const CAbstractSettings *poSettings) const {
		const TCHAR *pszJarPath = g_oJarPathDefault.GetString (poSettings);
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

#ifndef _WIN32
/// Returns the path to the PID file to write when starting as a daemon process
///
/// @return the path to the PID file
const TCHAR *CSettings::GetPidFile () const {
	return GetPidFile (DEFAULT_PID_FILE);
}
#endif /* ifndef _WIN32 */

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
