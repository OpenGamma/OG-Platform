/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Runtime configuration options

#include "AbstractSettings.h"
#define DLLVERSION_NO_ERRORS
#include "DllVersion.h"
#include "Logging.h"
#include "Error.h"
#include "File.h"
#include "String.h"

LOGGING(com.opengamma.language.util.AbstractSettings);

#ifndef _WIN32
static FILE *_OpenSettings (const TCHAR *pszSettingsLocation, const TCHAR *pszBase, const TCHAR *pszConfig) {
	TCHAR szPath[PATH_MAX];
	StringCbPrintf (szPath, sizeof (szPath), TEXT ("%s%s%s"), pszBase, pszConfig, pszSettingsLocation);
	FILE *f = fopen (szPath, TEXT ("rt"));
	if (!f) {
		LOGWARN (TEXT ("Couldn't open ") << szPath << TEXT (", error ") << GetLastError ());
	} else {
		LOGDEBUG (TEXT ("Reading from ") << szPath);
	}
	return f;
}
#endif /* ifndef _WIN32 */

CAbstractSettings::CAbstractSettings () {
	m_pCache = NULL;
	TCHAR szSettingsLocation[256];
	if (!GetSettingsLocation (szSettingsLocation, sizeof (szSettingsLocation))) {
		LOGWARN (TEXT ("Couldn't get settings location, error ") << GetLastError ());
		return;
	}
#ifdef _WIN32
	m_hkeyGlobal = NULL;
	m_hkeyLocal = NULL;
	HKEY hkey;
	LOGDEBUG ("Opening registry keys");
	HRESULT hr;
	if ((hr = RegOpenKeyEx (HKEY_LOCAL_MACHINE, TEXT ("SOFTWARE"), 0, KEY_READ, &hkey)) == ERROR_SUCCESS) {
		if ((hr = RegOpenKeyEx (hkey, szSettingsLocation, 0, KEY_READ, &m_hkeyGlobal)) != ERROR_SUCCESS) {
			LOGDEBUG ("Couldn't find machine global configuration settings, error " << hr);
		}
		RegCloseKey (hkey);
	} else {
		LOGWARN ("Couldn't open HKEY_LOCAL_MACHINE\\SOFTWARE registry key, error " << hr);
	}
	if ((hr = RegOpenKeyEx (HKEY_CURRENT_USER, TEXT ("SOFTWARE"), 0, KEY_READ, &hkey)) == ERROR_SUCCESS) {
		if ((hr = RegOpenKeyEx (hkey, szSettingsLocation, 0, KEY_READ, &m_hkeyLocal)) != ERROR_SUCCESS) {
			LOGDEBUG ("Couldn't find user local configuration settings, error " << hr);
		}
		RegCloseKey (hkey);
	} else {
		LOGWARN ("Couldn't open HKEY_CURRENT_USER\\Software registry key, error " << hr);
	}
#else /* ifdef _WIN32 */
	// TODO: the default paths should be configurable from the build
	FILE *f = _OpenSettings (szSettingsLocation, getenv ("HOME"), TEXT ("/etc/"));
	if (!f) {
		f = _OpenSettings (szSettingsLocation, TEXT ("/usr/local"), TEXT ("/etc/"));
		if (!f) {
			f = _OpenSettings (szSettingsLocation, TEXT (""), TEXT ("/etc/"));
			if (!f) {
				LOGWARN (TEXT ("Couldn't open configuration file"));
				return;
			}
		}
	}
	int nLine = 0;
	while (fgets (szSettingsLocation, sizeof (szSettingsLocation), f)) {
		nLine++;
		TCHAR *psz = szSettingsLocation;
		while (isspace (*psz)) psz++;
		if (!*psz || (*psz == '#') || !strtok (psz, "\r\n")) continue;
		TCHAR *pszKey = strtok (psz, "=");
		if (!pszKey) {
			// This shouldn't happen
			LOGFATAL (TEXT ("Bad line ") << nLine);
			continue;
		}
		TCHAR *pszValue = strtok (NULL, "");
		if (!pszValue) {
			LOGWARN (TEXT ("Bad line ") << nLine);
			continue;
		}
		LOGDEBUG (TEXT ("Key=") << pszKey << TEXT (", Value=") << pszValue);
		CachePut (pszKey, pszValue);
	}
	fclose (f);
	LOGDEBUG (TEXT ("Configuration file read, ") << nLine << TEXT (" lines"));
#endif /* ifdef _WIN32 */
}

CAbstractSettings::~CAbstractSettings () {
#ifdef _WIN32
	LOGDEBUG ("Closing registry keys");
	if (m_hkeyGlobal != NULL) {
		RegCloseKey (m_hkeyGlobal);
		m_hkeyGlobal = NULL;
	}
	if (m_hkeyLocal != NULL) {
		RegCloseKey (m_hkeyLocal);
		m_hkeyLocal = NULL;
	}
#endif /* ifdef _WIN32 */
	LOGDEBUG ("Deleting cache");
	while (m_pCache != NULL) {
		delete m_pCache->pszKey;
		delete m_pCache->pszValue;
		struct _setting *pNext = m_pCache->pNext;
		delete m_pCache;
		m_pCache = pNext;
	}
}

// Note the cache is not about performance, but more to track the memory we've allocated. It is
// unlikely that keys will be accessed at random, only once anyway, so speedy search lookups
// from a hash implementation aren't that helpful.

const TCHAR *CAbstractSettings::CacheGet (const TCHAR *pszKey) {
	struct _setting *pCache = m_pCache;
	while (pCache) {
		if (!_tcsicmp (pszKey, pCache->pszKey)) return pCache->pszValue;
		pCache = pCache->pNext;
	}
	return NULL;
}

const TCHAR *CAbstractSettings::CachePut (const TCHAR *pszKey, const TCHAR *pszValue) {
	struct _setting *pCache = new struct _setting;
	if (!pCache) {
		LOGFATAL (TEXT ("Out of memory"));
		return NULL;
	} else {
		pCache->pszKey = _tcsdup (pszKey);
		pCache->pszValue = _tcsdup (pszValue);
		pCache->pNext = m_pCache;
		m_pCache = pCache;
		return pCache->pszValue;
	}
}

#ifdef _WIN32
PCTSTR CAbstractSettings::RegistryGet (HKEY hkey, PCTSTR pszKey) {
	DWORD dwType;
	union {
		TCHAR sz[MAX_PATH];
		DWORD dw;
	} data;
	DWORD dwSize = sizeof (data);
	HRESULT hr;
	if ((hr = RegGetValue (hkey, NULL, pszKey, RRF_RT_REG_DWORD | RRF_RT_REG_SZ, &dwType, &data, &dwSize)) != ERROR_SUCCESS) {
		LOGDEBUG (TEXT ("Couldn't read registry key ") << pszKey << TEXT (", error ") << hr);
	} else {
		switch (dwType) {
		case REG_DWORD :
			dwSize = data.dw;
			StringCbPrintf (data.sz, sizeof (data.sz), TEXT ("%d"), dwSize);
			/* drop through */
		case REG_SZ :
			return CachePut (pszKey, data.sz);
		default :
			LOGWARN (TEXT ("Unexpected type ") << dwType << TEXT (" in registry value ") << pszKey);
			break;
		}
	}
	return NULL;
}
#endif /* ifdef _WIN32 */

const TCHAR *CAbstractSettings::Get (const TCHAR *pszKey) {
	const TCHAR *pszValue = CacheGet (pszKey);
#ifdef _WIN32
	if (!pszValue) {
		pszValue = RegistryGet (m_hkeyLocal, pszKey);
		if (!pszValue) {
			pszValue = RegistryGet (m_hkeyGlobal, pszKey);
		}
	}
#endif
	return pszValue;
}

const TCHAR *CAbstractSettings::Get (const TCHAR *pszKey, const TCHAR *pszDefault) {
	const TCHAR *pszValue = Get (pszKey);
	return pszValue ? pszValue : pszDefault;
}

int CAbstractSettings::Get (const TCHAR *pszKey, int nDefault) {
	const TCHAR * pszValue = Get (pszKey);
	return pszValue ? _tstoi (pszValue) : nDefault;
}

bool CAbstractSettings::GetSettingsLocation (TCHAR * pszBuffer, size_t cbBufferLen) {
	CDllVersion version;
	const TCHAR *pszCompanyName = version.GetCompanyName ();
	const TCHAR *pszProductName = version.GetProductName ();
	while ((cbBufferLen > sizeof (TCHAR)) && *pszCompanyName) {
		TCHAR c = *(pszCompanyName++);
		if (((c >= 'a') && (c <= 'z'))
		 || ((c >= 'A') && (c <= 'Z'))) {
			 *(pszBuffer++) = c;
			 cbBufferLen -= sizeof (TCHAR);
		}
	}
	if (cbBufferLen > sizeof (TCHAR)) {
		*(pszBuffer++) = PATH_CHAR;
		cbBufferLen -= sizeof (TCHAR);
	}
	while ((cbBufferLen > sizeof (TCHAR)) && *pszProductName) {
		TCHAR c = *(pszProductName++);
		if (((c >= 'a') && (c <= 'z'))
		 || ((c >= 'A') && (c <= 'Z'))) {
			 *(pszBuffer++) = c;
			 cbBufferLen -= sizeof (TCHAR);
		}
	}
	if (cbBufferLen >= sizeof (TCHAR)) {
		*pszBuffer = 0;
		return !*pszProductName && !*pszCompanyName;
	} else {
		return false;
	}
}
