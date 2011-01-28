/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Runtime configuration options

#include "AbstractSettings.h"
#include "DllVersion.h"
#include "Logging.h"

LOGGING(com.opengamma.language.util.AbstractSettings);

CAbstractSettings::CAbstractSettings () {
	m_hkeyGlobal = NULL;
	m_hkeyLocal = NULL;
	m_pCache = NULL;
	HKEY hkey;
	TCHAR szRegistryConfigurationString[MAX_PATH];
	LOGDEBUG ("Opening registry keys");
	if (GetSettingsLocation (szRegistryConfigurationString, sizeof (szRegistryConfigurationString))) {
		HRESULT hr;
		if ((hr = RegOpenKeyEx (HKEY_LOCAL_MACHINE, TEXT ("SOFTWARE"), 0, KEY_READ, &hkey)) == ERROR_SUCCESS) {
			if ((hr = RegOpenKeyEx (hkey, szRegistryConfigurationString, 0, KEY_READ, &m_hkeyGlobal)) != ERROR_SUCCESS) {
				LOGDEBUG ("Couldn't find machine global configuration settings, error " << hr);
			}
			RegCloseKey (hkey);
		} else {
			LOGWARN ("Couldn't open HKEY_LOCAL_MACHINE\\SOFTWARE registry key, error " << hr);
		}
		if ((hr = RegOpenKeyEx (HKEY_CURRENT_USER, TEXT ("SOFTWARE"), 0, KEY_READ, &hkey)) == ERROR_SUCCESS) {
			if ((hr = RegOpenKeyEx (hkey, szRegistryConfigurationString, 0, KEY_READ, &m_hkeyLocal)) != ERROR_SUCCESS) {
				LOGDEBUG ("Couldn't find user local configuration settings, error " << hr);
			}
			RegCloseKey (hkey);
		} else {
			LOGWARN ("Couldn't open HKEY_CURRENT_USER\\Software registry key, error " << hr);
		}
	} else {
		LOGWARN ("Couldn't find local registry configuration string, error " << GetLastError ());
	}
}

CAbstractSettings::~CAbstractSettings () {
	LOGDEBUG ("Closing registry keys");
	if (m_hkeyGlobal != NULL) {
		RegCloseKey (m_hkeyGlobal);
		m_hkeyGlobal = NULL;
	}
	if (m_hkeyLocal != NULL) {
		RegCloseKey (m_hkeyLocal);
		m_hkeyLocal = NULL;
	}
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
	if (!pszValue) {
#ifdef _WIN32
		pszValue = RegistryGet (m_hkeyLocal, pszKey);
		if (!pszValue) {
			pszValue = RegistryGet (m_hkeyGlobal, pszKey);
		}
#endif
	}
	return pszValue;
}

const TCHAR *CAbstractSettings::Get (const TCHAR *pszKey, PCTSTR pszDefault) {
	const TCHAR *pszValue = Get (pszKey);
	return pszValue ? pszValue : pszDefault;
}

int CAbstractSettings::Get (const TCHAR *pszKey, int nDefault) {
	const TCHAR * pszValue = Get (pszKey);
	return pszValue ? _tstoi (pszValue) : nDefault;
}

bool CAbstractSettings::GetSettingsLocation (TCHAR * pszBuffer, size_t cbBufferLen) {
	CDllVersion version;
	PCTSTR pszCompanyName = version.GetCompanyName ();
	PCTSTR pszProductName = version.GetProductName ();
#ifndef _WIN32
	// TODO: pull the path prefix from a macro to allow user to change during ./configure & build
	if (cbBufferLen > sizeof (TCHAR) * 5) {
		*(pszBuffer++) = '/';
		*(pszBuffer++) = 'e';
		*(pszBuffer++) = 't';
		*(pszBuffer++) = 'c';
		*(pszBuffer++) = '/';
		cbBufferLen -= sizeof (TCHAR) * 5;
	}
#endif /* ifndef _WIN32 */
	while ((cbBufferLen > sizeof (TCHAR)) && *pszCompanyName) {
		TCHAR c = *(pszCompanyName++);
		if (((c >= 'a') && (c <= 'z'))
		 || ((c >= 'A') && (c <= 'Z'))) {
			 *(pszBuffer++) = c;
			 cbBufferLen -= sizeof (TCHAR);
		}
	}
	if (cbBufferLen > sizeof (TCHAR)) {
#ifdef _WIN32
		*(pszBuffer++) = '\\';
#else
		*(pszBuffer++) = '/';
#endif
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
