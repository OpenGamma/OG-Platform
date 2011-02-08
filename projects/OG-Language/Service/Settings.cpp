/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Runtime configuration options

#include "Settings.h"
#ifdef _WIN32
#include <Util/DllVersion.h>
#endif /* ifdef _WIN32 */

LOGGING(com.opengamma.language.service.Settings);

#define DEFAULT_CONNECTION_TIMEOUT	3000	/* 3s default */
#define DEFAULT_IDLE_TIMEOUT		300000	/* 5m default */
#ifdef _WIN32
#define DEFAULT_SDDL				NULL
#endif /* ifdef _WIN32 */
#define DEFAULT_LOG_CONFIGURATION	NULL
#ifdef _WIN32
#define DEFAULT_JVM_LIBRARY			TEXT ("jvm.dll")
#else
#define DEFAULT_JVM_LIBRARY			TEXT ("jvm.so")
#endif

CSettings::CSettings () : CAbstractSettings () {
	m_pszDefaultJvmLibrary = NULL;
	m_pszDefaultJarPath = NULL;
}

CSettings::~CSettings () {
	if (m_pszDefaultJvmLibrary) {
		delete m_pszDefaultJvmLibrary;
		m_pszDefaultJvmLibrary = NULL;
	}
	if (m_pszDefaultJarPath) {
		delete m_pszDefaultJarPath;
		m_pszDefaultJarPath = NULL;
	}
}

#ifdef _WIN32
static BOOL _IsValidLibrary (PCTSTR pszLibrary) {
	DWORD dwAttrib = GetFileAttributes (pszLibrary);
	if (dwAttrib == INVALID_FILE_ATTRIBUTES) return FALSE;
	// Try and load to verify that the library is the same bittage as this process
	HMODULE hModule = LoadLibrary (pszLibrary);
	if (hModule) {
		FreeLibrary (hModule);
		return TRUE;
	} else {
		DWORD dwError = GetLastError ();
		if (dwError == ERROR_BAD_EXE_FORMAT) {
#if defined (_M_IX86)
			LOGERROR (TEXT ("Found ") << pszLibrary << TEXT (" but it is not a 32-bit Windows module - try the 64-bit service"));
#elif defined (_M_X64)
			LOGERROR (TEXT ("Found ") << pszLibrary << TEXT (" but it is not a 64-bit Windows module - try the 32-bit service"));
#else
#error "Need correct error message for processor"
#endif
		} else {
			LOGWARN (TEXT ("Found ") << pszLibrary << TEXT (" but couldn't load, error ") << dwError);
		}
		return FALSE;
	}
}
#endif

#define MAX_ENV_LEN 32767
const TCHAR *CSettings::GetJvmLibrary () {
	if (!m_pszDefaultJvmLibrary) {
#ifdef _WIN32
		TCHAR *pszPath = new TCHAR[MAX_ENV_LEN];
		if (!pszPath) {
			LOGERROR ("Out of memory");
			return NULL;
		}
		DWORD dwPath = GetEnvironmentVariable (TEXT ("JAVA_HOME"), pszPath, MAX_ENV_LEN);
		if (dwPath != 0) {
			size_t cb = dwPath + 20; // \bin\server\jvm.dll +\0
			m_pszDefaultJvmLibrary = new TCHAR[cb];
			if (!m_pszDefaultJvmLibrary) {
				delete pszPath;
				LOGERROR ("Out of memory");
				return NULL;
			}
			StringCchPrintf (m_pszDefaultJvmLibrary, cb, TEXT ("%s\\bin\\server\\jvm.dll"), pszPath);
			if (_IsValidLibrary (m_pszDefaultJvmLibrary)) {
				LOGINFO (TEXT ("Default jvm.dll ") << m_pszDefaultJvmLibrary << TEXT (" found from JAVA_HOME"));
			} else {
				StringCchPrintf (m_pszDefaultJvmLibrary, cb, TEXT ("%s\\bin\\client\\jvm.dll"), pszPath);
				if (_IsValidLibrary (m_pszDefaultJvmLibrary)) {
					LOGINFO (TEXT ("Default jvm.dll ") << m_pszDefaultJvmLibrary << TEXT (" found from JAVA_HOME"));
				} else {
					LOGWARN (TEXT ("JAVA_HOME set but bin\\<server|client>\\jvm.dll doesn't exist"));
					delete m_pszDefaultJvmLibrary;
					m_pszDefaultJvmLibrary = NULL;
				}
			}
		}
		if (m_pszDefaultJvmLibrary == NULL) {
			DWORD dwPath = GetEnvironmentVariable (TEXT ("PATH"), pszPath, MAX_ENV_LEN);
			if (dwPath != 0) {
				TCHAR *nextToken;
				TCHAR *psz = _tcstok_s (pszPath, TEXT (";"), &nextToken);
				while (psz != NULL) {
					size_t cb = dwPath + 27; // MAX( \java.exe +\0 , \server\jvm.dll +\0 , \..\jre\bin\server\jvm.dll + \0)
					m_pszDefaultJvmLibrary = new TCHAR[cb];
					if (!m_pszDefaultJvmLibrary) {
						delete pszPath;
						LOGERROR ("Out of memory");
						return NULL;
					}
					StringCchPrintf (m_pszDefaultJvmLibrary, cb, TEXT ("%s\\java.exe"), psz);
					LOGDEBUG (TEXT ("Looking for ") << m_pszDefaultJvmLibrary);
					if (!_IsValidLibrary (m_pszDefaultJvmLibrary)) {
						delete m_pszDefaultJvmLibrary;
						m_pszDefaultJvmLibrary = NULL;
						psz = _tcstok_s (NULL, TEXT (";"), &nextToken);
						continue;
					}
					LOGDEBUG (TEXT ("Default Java executable ") << m_pszDefaultJvmLibrary << TEXT (" found from PATH"));
					StringCchPrintf (m_pszDefaultJvmLibrary, cb, TEXT ("%s\\server\\jvm.dll"), psz);
					LOGDEBUG (TEXT ("Looking for ") << m_pszDefaultJvmLibrary);
					if (_IsValidLibrary (m_pszDefaultJvmLibrary)) {
						LOGINFO (TEXT ("Default jvm.dll ") << m_pszDefaultJvmLibrary << TEXT (" found from PATH"));
						break;
					}
					StringCchPrintf (m_pszDefaultJvmLibrary, cb, TEXT ("%s\\..\\jre\\bin\\server\\jvm.dll"), psz);
					LOGDEBUG (TEXT ("Looking for ") << m_pszDefaultJvmLibrary);
					if (_IsValidLibrary (m_pszDefaultJvmLibrary)) {
						LOGINFO (TEXT ("Default jvm.dll ") << m_pszDefaultJvmLibrary << TEXT (" found from PATH"));
						break;
					}
					StringCchPrintf (m_pszDefaultJvmLibrary, cb, TEXT ("%s\\client\\jvm.dll"), psz);
					LOGDEBUG (TEXT ("Looking for ") << m_pszDefaultJvmLibrary);
					if (_IsValidLibrary (m_pszDefaultJvmLibrary)) {
						LOGINFO (TEXT ("Default jvm.dll ") << m_pszDefaultJvmLibrary << TEXT (" found from PATH"));
						break;
					}
					StringCchPrintf (m_pszDefaultJvmLibrary, cb, TEXT ("%s\\..\\jre\\bin\\client\\jvm.dll"), psz);
					LOGDEBUG (TEXT ("Looking for ") << m_pszDefaultJvmLibrary);
					if (_IsValidLibrary (m_pszDefaultJvmLibrary)) {
						LOGINFO (TEXT ("Default jvm.dll ") << m_pszDefaultJvmLibrary << TEXT (" found from PATH"));
						break;
					}
					delete m_pszDefaultJvmLibrary;
					m_pszDefaultJvmLibrary = NULL;
					psz = _tcstok_s (NULL, TEXT (";"), &nextToken);
					continue;
				}
			}
		}
		delete pszPath;
#else
		// TODO: Is there anything reasonably generic? The path on my Linux box included a processor (e.g. amd64) reference
#endif
		if (m_pszDefaultJvmLibrary == NULL) {
			LOGDEBUG ("No default JVM libraries found on JAVA_HOME or PATH");
			m_pszDefaultJvmLibrary = _tcsdup (DEFAULT_JVM_LIBRARY);
		}
	}
	return GetJvmLibrary (m_pszDefaultJvmLibrary);
}

const TCHAR *CSettings::GetConnectionPipe () {
	return GetConnectionPipe (SERVICE_DEFAULT_CONNECTION_PIPE);
}

unsigned long CSettings::GetConnectionTimeout () {
	return GetConnectionTimeout (DEFAULT_CONNECTION_TIMEOUT);
}

static bool _GetExecutableName (TCHAR *pszBuffer, size_t cchBuffer) {
#ifdef _WIN32
	HMODULE hModule = CDllVersion::GetCurrentModule ();
	if (!hModule) {
		return false;
	}
	if (!GetModuleFileName (hModule, pszBuffer, cchBuffer)) {
		LOGWARN (TEXT ("Couldn't get module filename, error ") << GetLastError ());
		return false;
	}
#else
	if (readlink ("/proc/self/exe", pszBuffer, cchBuffer / sizeof (TCHAR)) <= 0) {
		LOGWARN (TEXT ("Couldn't get module filename, error ") << GetLastError ());
		return false;
	}
#endif
	return true;
}

#define CLIENT_JAR_NAME		TEXT ("client.jar")
#define CLIENT_JAR_LEN		10
const TCHAR *CSettings::GetJarPath () {
	if (!m_pszDefaultJarPath) {
		// Scan backwards from the module to find a path which has Client.jar in. This works if all of the
		// JARs and DLLs are in the same folder, but also in the case of a build system where we have sub-folders
		// for the different configurations/platforms.
		TCHAR szPath[256 + CLIENT_JAR_LEN]; // Guarantee room for Client.jar at the end
		if (_GetExecutableName (szPath, 256)) {
			LOGDEBUG (TEXT ("Executable = ") << szPath);
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
					m_pszDefaultJarPath = _tcsdup (szPath);
					break;
				}
				pszEnd = _tcsrchr (szPath, PATH_CHAR);
			}
			if (!m_pszDefaultJarPath) {
				LOGWARN (TEXT ("Couldn't find client library Jar on module path"));
				m_pszDefaultJarPath = _tcsdup (TEXT ("."));
			}
		} else {
			m_pszDefaultJarPath = _tcsdup (TEXT ("."));
		}
	}
	return GetJarPath (m_pszDefaultJarPath);
}

const TCHAR *CSettings::GetAnnotationCache () {
	return GetAnnotationCache (GetJarPath ());
}

const TCHAR *CSettings::GetLogConfiguration () {
	return GetLogConfiguration (DEFAULT_LOG_CONFIGURATION);
}

unsigned long CSettings::GetIdleTimeout () {
	return GetIdleTimeout (DEFAULT_IDLE_TIMEOUT);
}

const TCHAR *CSettings::GetServiceName () {
	return GetServiceName (SERVICE_DEFAULT_SERVICE_NAME);
}

#ifdef _WIN32
const TCHAR *CSettings::GetServiceSDDL () {
	return GetServiceSDDL (DEFAULT_SDDL);
}
#endif /* ifdef _WIN32 */