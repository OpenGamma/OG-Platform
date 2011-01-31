/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Runtime configuration options

#include "Settings.h"

LOGGING(com.opengamma.language.service.Settings);

#define DEFAULT_IDLE_TIMEOUT		300000	/* 5m default */
#ifdef _WIN32
#define DEFAULT_SDDL				NULL
#endif /* ifdef _WIN32 */
#define DEFAULT_LOG_CONFIGURATION	NULL

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

#define MAX_ENV_LEN 32767
const TCHAR *CSettings::GetJvmLibrary () {
	if (!m_pszDefaultJvmLibrary) {
		TCHAR *pszPath = new TCHAR[MAX_ENV_LEN];
		if (!pszPath) {
			LOGERROR ("Out of memory");
			return NULL;
		}
#ifdef _WIN32
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
			DWORD dwAttrib = GetFileAttributes (m_pszDefaultJvmLibrary);
			if (dwAttrib != INVALID_FILE_ATTRIBUTES) {
				LOGINFO (TEXT ("Default jvm.dll ") << m_pszDefaultJvmLibrary << TEXT (" found from JAVA_HOME"));
			} else {
				StringCchPrintf (m_pszDefaultJvmLibrary, cb, TEXT ("%s\\bin\\client\\jvm.dll"), pszPath);
				dwAttrib = GetFileAttributes (m_pszDefaultJvmLibrary);
				if (dwAttrib != INVALID_FILE_ATTRIBUTES) {
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
					DWORD dwAttrib = GetFileAttributes (m_pszDefaultJvmLibrary);
					if (dwAttrib == INVALID_FILE_ATTRIBUTES) {
						delete m_pszDefaultJvmLibrary;
						m_pszDefaultJvmLibrary = NULL;
						psz = _tcstok_s (NULL, TEXT (";"), &nextToken);
						continue;
					}
					LOGDEBUG (TEXT ("Default Java executable ") << m_pszDefaultJvmLibrary << TEXT (" found from PATH"));
					StringCchPrintf (m_pszDefaultJvmLibrary, cb, TEXT ("%s\\server\\jvm.dll"), psz);
					LOGDEBUG (TEXT ("Looking for ") << m_pszDefaultJvmLibrary);
					dwAttrib = GetFileAttributes (m_pszDefaultJvmLibrary);
					if (dwAttrib != INVALID_FILE_ATTRIBUTES) {
						LOGINFO (TEXT ("Default jvm.dll ") << m_pszDefaultJvmLibrary << TEXT (" found from PATH"));
						break;
					}
					StringCchPrintf (m_pszDefaultJvmLibrary, cb, TEXT ("%s\\..\\jre\\bin\\server\\jvm.dll"), psz);
					LOGDEBUG (TEXT ("Looking for ") << m_pszDefaultJvmLibrary);
					dwAttrib = GetFileAttributes (m_pszDefaultJvmLibrary);
					if (dwAttrib != INVALID_FILE_ATTRIBUTES) {
						LOGINFO (TEXT ("Default jvm.dll ") << m_pszDefaultJvmLibrary << TEXT (" found from PATH"));
						break;
					}
					StringCchPrintf (m_pszDefaultJvmLibrary, cb, TEXT ("%s\\client\\jvm.dll"), psz);
					LOGDEBUG (TEXT ("Looking for ") << m_pszDefaultJvmLibrary);
					dwAttrib = GetFileAttributes (m_pszDefaultJvmLibrary);
					if (dwAttrib != INVALID_FILE_ATTRIBUTES) {
						LOGINFO (TEXT ("Default jvm.dll ") << m_pszDefaultJvmLibrary << TEXT (" found from PATH"));
						break;
					}
					StringCchPrintf (m_pszDefaultJvmLibrary, cb, TEXT ("%s\\..\\jre\\bin\\client\\jvm.dll"), psz);
					LOGDEBUG (TEXT ("Looking for ") << m_pszDefaultJvmLibrary);
					dwAttrib = GetFileAttributes (m_pszDefaultJvmLibrary);
					if (dwAttrib != INVALID_FILE_ATTRIBUTES) {
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
#else
		TODO (TEXT ("POSIX version of the environment scanning above"));
#endif
		delete pszPath;
		if (m_pszDefaultJvmLibrary == NULL) {
			LOGDEBUG ("No default JVM libraries found on JAVA_HOME or PATH");
			m_pszDefaultJvmLibrary = _tcsdup (TEXT ("jvm.dll"));
		}
	}
	return GetJvmLibrary (m_pszDefaultJvmLibrary);
}

const TCHAR *CSettings::GetConnectionPipe () {
	return GetConnectionPipe (SERVICE_DEFAULT_CONNECTION_PIPE);
}

const TCHAR *CSettings::GetJarPath () {
	if (!m_pszDefaultJarPath) {
#ifdef _WIN32
		TCHAR szModuleFilename[MAX_PATH];
		if (GetModuleFileName (NULL, szModuleFilename, MAX_PATH)) {
			int n = _tcslen (szModuleFilename);
			while ((n > 0) && (szModuleFilename[n] != '\\')) {
				n--;
			}
			if (n > 0) {
				szModuleFilename[n] = 0;
				LOGINFO (TEXT ("Default folder ") << szModuleFilename << TEXT (" found from executable"));
				m_pszDefaultJarPath = _tcsdup (szModuleFilename);
			} else {
				LOGINFO (TEXT ("No default folder found"));
				m_pszDefaultJarPath = _tcsdup (TEXT ("."));
			}
		} else {
			LOGWARN (TEXT ("Couldn't get executable filename and path, error ") << GetLastError ());
			m_pszDefaultJarPath = _tcsdup (TEXT ("."));
		}
#else
		TODO (TEXT ("POSIX version of the module above"));
#endif
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