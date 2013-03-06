/*
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
/// Attempts to open a file named by the concatenation of the three path components.
///
/// @param[in] pszSettingsLocation the name of the file within the config folder, e.g. &lt;company name&lt;/&lt;product&gt;,
///			with no leading slash
/// @param[in] pszBase the location of the config folder, with a leading slash if not the empty string, and no trailing slash
/// @param[in] pszConfig the config folder name with leading and trailing slash
/// @return the opened file handle or NULL if not found
static FILE *_OpenSettings (const TCHAR *pszSettingsLocation, const TCHAR *pszBase, const TCHAR *pszConfig) {
	TCHAR szPath[PATH_MAX];
	StringCbPrintf (szPath, sizeof (szPath), TEXT ("%s%s%s"), pszBase, pszConfig, pszSettingsLocation);
	FILE *f = fopen (szPath, TEXT ("rt"));
	if (!f) {
		LOGDEBUG (TEXT ("Couldn't open ") << szPath << TEXT (", error ") << GetLastError ());
	} else {
		LOGDEBUG (TEXT ("Reading from ") << szPath);
	}
	return f;
}
#endif /* ifndef _WIN32 */

/// Creates a new settings object. Under Windows, the registry keys are opened. Under Posix, the configuration
/// file is found and the contents loaded into the cache.
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
#ifndef DEFAULT_CONFIG_FOLDER
#define DEFAULT_CONFIG_FOLDER	"/etc/"
#endif /* ifndef DEFAULT_CONFIG_FOLDER */
#ifndef DEFAULT_CONFIG_BASE
#define DEFAULT_CONFIG_BASE		"/usr/local"
#endif /* ifndef DEFAULT_CONFIG_BASE */
	const TCHAR *pszConfig = getenv ("OG_LANGUAGE_CONFIG_PATH");
	FILE *f = pszConfig ? _OpenSettings (szSettingsLocation, TEXT (""), pszConfig) : NULL;
	if (!f) {
		f = _OpenSettings (szSettingsLocation, getenv ("HOME"), TEXT (DEFAULT_CONFIG_FOLDER));
		if (!f) {
			f = _OpenSettings (szSettingsLocation, TEXT (DEFAULT_CONFIG_BASE), TEXT (DEFAULT_CONFIG_FOLDER));
			if (!f) {
				f = _OpenSettings (szSettingsLocation, TEXT (""), TEXT (DEFAULT_CONFIG_FOLDER));
				if (!f) {
					LOGWARN (TEXT ("Couldn't open configuration file"));
					return;
				}
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

/// Destroys the object, releasing any memory allocated to the cache. String pointers
/// returned by the querying methods will not be valid after the settings object
/// has been destroyed.
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

/// Removes an entry from the cache
///
/// @param[in] pszKey key to search for, never NULL
void CAbstractSettings::CacheRemove (const TCHAR *pszKey) const {
	struct _setting *pCache = m_pCache;
	struct _setting **ppPrevious = &m_pCache;
	while (pCache) {
		if (!_tcsicmp (pszKey, pCache->pszKey)) {
			*ppPrevious = pCache->pNext;
			free (pCache->pszKey);
			free (pCache->pszValue);
			free (pCache);
			return;
		}
		pCache = pCache->pNext;
		ppPrevious = &pCache->pNext;
	}
}

/// Fetches an entry from the cache
///
/// @param[in] pszKey the key to search for, never NULL
/// @return the value found, or NULL if none
const TCHAR *CAbstractSettings::CacheGet (const TCHAR *pszKey) const {
	struct _setting *pCache = m_pCache;
	while (pCache) {
		if (!_tcsicmp (pszKey, pCache->pszKey)) return pCache->pszValue;
		pCache = pCache->pNext;
	}
	return NULL;
}

/// Puts an entry into the cache. The value is duplicated. This assumes there
/// is no matching value already in the cache.
///
/// @param[in] pszKey the key to store a value against, never NULL
/// @param[in] pszValue the value to store, never NULL
/// @return the cached value - a copy of the original
const TCHAR *CAbstractSettings::CachePut (const TCHAR *pszKey, const TCHAR *pszValue) const {
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

/// Replace an entry in the cache. The value is duplicated.
///
/// @param[in] pszKey the key to store a value against, never NULL
/// @param[in] pszValue the value to store, never NULL
/// @return the cached value - a copy of the original
const TCHAR *CAbstractSettings::CacheReplace (const TCHAR *pszKey, const TCHAR *pszValue) const {
	struct _setting *pCache = m_pCache;
	while (pCache) {
		if (!_tcscmp (pCache->pszKey, pszKey)) {
			free (pCache->pszValue);
			pCache->pszValue = _tcsdup (pszValue);
			return pCache->pszValue;
		}
		pCache = pCache->pNext;
	}
	pCache = new struct _setting;
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

/// Fetches a value from the registry, adding it to the cache if found.
///
/// @param[in] hkey the settings key to look under, never NULL
/// @param[in] pszKey the key value to search for, never NULL
/// @return the value found, or NULL if none
PCTSTR CAbstractSettings::RegistryGet (HKEY hkey, PCTSTR pszKey) const {
	DWORD dwType;
	union {
		TCHAR sz[MAX_PATH];
		DWORD dw;
	} data;
	DWORD dwSize = sizeof (data);
	HRESULT hr;
	if ((hr = RegGetValue (hkey, NULL, pszKey, RRF_RT_REG_DWORD | RRF_RT_REG_SZ, &dwType, &data, &dwSize)) != ERROR_SUCCESS) {
		LOGDEBUG (TEXT ("Couldn't read registry key ") << pszKey << TEXT (", error ") << HRESULT_CODE (hr));
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

/// Writes a value to the registry.
///
/// @param[in] hkey key containing the values
/// @param[in] pszKey the key value to update, never NULL
/// @param[in] pszValue the value to write, or NULL to remove the value
/// @return TRUE if successful, FALSE if there was a problem
BOOL CAbstractSettings::RegistrySet (HKEY hkey, PCTSTR pszKey, PCTSTR pszValue) {
	HRESULT hr;
	if (pszValue) {
		hr = RegSetValueEx (hkey, pszKey, 0, REG_SZ, (const BYTE*)pszValue, (_tcslen (pszValue) + 1) * sizeof (TCHAR));
		CacheReplace (pszKey, pszValue);
	} else {
		hr = RegDeleteValue (hkey, pszKey);
		CacheRemove (pszKey);
	}
	return SUCCEEDED (hr);
}

/// Writes a value to the registry.
///
/// @param[in] pszBase base address under HKLM or HKCU, never NULL
/// @param[in] pszKey the key value to update, never NULL
/// @param[in] pszValue the value to write, or NULL to remove the value
/// @return TRUE if successful, FALSE if there was a problem
BOOL CAbstractSettings::RegistrySet (PCTSTR pszBase, PCTSTR pszKey, PCTSTR pszValue) {
	HKEY hkeyGlobal = NULL, hkeyLocal = NULL;
	HRESULT hr;
	if ((hr = RegOpenKeyEx (HKEY_LOCAL_MACHINE, pszBase, 0, KEY_WRITE, &hkeyGlobal)) != ERROR_SUCCESS) {
		LOGWARN (TEXT ("Couldn't find machine global configuration settings, error ") << hr);
	}
	if ((hr = RegOpenKeyEx (HKEY_CURRENT_USER, pszBase, 0, KEY_WRITE, &hkeyLocal)) != ERROR_SUCCESS) {
		LOGWARN (TEXT ("Couldn't find user local configuration settings, error ") << hr);
	}
	BOOL bResult;
	if (hkeyGlobal) {
		bResult = RegistrySet (hkeyGlobal, pszKey, pszValue);
	} else if (hkeyLocal) {
		bResult = RegistrySet (hkeyLocal, pszKey, pszValue);
	} else {
		LOGERROR (TEXT ("Couldn't open global or local registry keys"));
		bResult = FALSE;
	}
	if (hkeyGlobal) RegCloseKey (hkeyGlobal);
	if (hkeyLocal) RegCloseKey (hkeyLocal);
	return bResult;
}

/// Writes a value to the registry.
///
/// @param[in] pszKey the key value to update, never NULL
/// @param[in] pszValue the value to write, or NULL to remove the value
/// @return TRUE if successful, FALSE if there was a problem
BOOL CAbstractSettings::RegistrySet (PCTSTR pszKey, PCTSTR pszValue) {
	TCHAR szSettingsLocation[256];
	if (!GetSettingsLocation (szSettingsLocation, sizeof (szSettingsLocation))) {
		LOGWARN (TEXT ("Couldn't get settings location, error ") << GetLastError ());
		return FALSE;
	}
	TCHAR szBase[312];
	StringCbPrintf (szBase, sizeof (szBase), TEXT ("SOFTWARE\\%s"), szSettingsLocation);
	BOOL bResult = RegistrySet (szBase, pszKey, pszValue);
#ifdef _WIN64
	StringCbPrintf (szBase, sizeof (szBase), TEXT ("SOFTWARE\\Wow6432Node\\%s"), szSettingsLocation);
	bResult &= RegistrySet (szBase, pszKey, pszValue);
#endif /* ifdef _WIN64 */
	return bResult;
}

/// Enumerates the keys and values in the registry, populating the cache. Note that this
/// is an expensive operation and should only be performed if the enumeration is necessary.
/// Normally the RegistryGet function should be used.
///
/// @param[in] hkey the settings key to look under, not NULL
void CAbstractSettings::RegistryEnumerate (HKEY hkey) const {
	DWORD dwIndex = 0;
	LONG lResult;
	do {
		TCHAR szValueName[256];
		DWORD cchValueName = sizeof (szValueName) / sizeof (TCHAR);
		DWORD dwType;
		union {
			TCHAR sz[MAX_PATH];
			DWORD dw;
		} data;
		DWORD dwSize = sizeof (data);
		lResult = RegEnumValue (hkey, dwIndex, szValueName, &cchValueName, NULL, &dwType, (LPBYTE)&data, &dwSize);
		if (lResult != ERROR_SUCCESS) {
			LOGINFO (TEXT ("Enumerated ") << dwIndex << TEXT (" registry values"));
			break;
		}
		switch (dwType) {
		case REG_DWORD :
			dwSize = data.dw;
			StringCbPrintf (data.sz, sizeof (data.sz), TEXT ("%d"), dwSize);
			/* drop through */
		case REG_SZ :
			CacheReplace (szValueName, data.sz);
			break;
		default :
			LOGWARN (TEXT ("Unexpected type ") << dwType << TEXT (" in registry value ") << szValueName);
			break;
		}
		dwIndex++;
	} while (TRUE);
}

/// Opens a sub-key from the registry.
///
/// @param[in] hkey key to look in
/// @param[in] pszKey name of the key to open, never NULL
/// @return the open handle or NULL if not found
static HKEY _RegistryOpen (HKEY hkey, PCTSTR pszKey) {
	HRESULT hr;
	HKEY hkeyResult;
	if ((hr = RegOpenKeyEx (hkey, pszKey, 0, KEY_READ, &hkeyResult)) != ERROR_SUCCESS) {
		LOGDEBUG (TEXT ("Couldn't open registry key ") << pszKey << TEXT (", error ") << HRESULT_CODE (hr));
		return NULL;
	}
	return hkeyResult;
}

/// Opens a sub-key from the local (user) registry store.
///
/// @param[in] pszKey name of the key, never NULL
/// @return the open key or NULL if not found
HKEY CAbstractSettings::RegistryOpenLocal (PCTSTR pszKey) const {
	return _RegistryOpen (m_hkeyLocal, pszKey);
}

/// Opens a sub-key from the global (machine) registry store.
///
/// @param[in] pszKey name of the key, never NULL
/// @return the open key or NULL if not found
HKEY CAbstractSettings::RegistryOpenGlobal (PCTSTR pszKey) const {
	return _RegistryOpen (m_hkeyGlobal, pszKey);
}

#endif /* ifdef _WIN32 */

/// Fetches a string setting
///
/// @param[in] pszKey the key to search for, never NULL
/// @return the string value, or NULL if none
const TCHAR *CAbstractSettings::Get (const TCHAR *pszKey) const {
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

/// Fetches a string setting, or returns a default if none is defined. Note that this should only
/// be used where the default is a literal value. If the default must be calculated, use a
/// CAbstractSettingProvider instead.
/// 
/// @param[in] pszKey the key to search for, never NULL
/// @param[in] pszDefault the default value to return if the key is not found
/// @return the value found or the default value, may be NULL
const TCHAR *CAbstractSettings::Get (const TCHAR *pszKey, const TCHAR *pszDefault) const {
	const TCHAR *pszValue = Get (pszKey);
	return pszValue ? pszValue : pszDefault;
}

/// Fetches a string setting or returns a default if none is defined. The default value is only
/// created when needed using the CAbstractSettingProvider
///
/// @param[in] pszKey the key to search for, never NULL
/// @param[in] poDefault provider of the default value to return if the key is not found, never NULL
/// @return the value found or the default value, may be NULL
const TCHAR *CAbstractSettings::Get (const TCHAR *pszKey, const CAbstractSettingProvider *poDefault) const {
	const TCHAR *pszValue = Get (pszKey);
	return pszValue ? pszValue : poDefault->GetString (this);
}

/// Fetches a numeric setting or returns a default if none is defined. A string value is searched
/// for and converted to a decimal integer.
//
/// @param[in] pszKey the key to search for, never NULL
/// @param[in] nDefault the default value to return if the key is not found
/// @return the value found or the default value
int CAbstractSettings::Get (const TCHAR *pszKey, int nDefault) const {
	const TCHAR * pszValue = Get (pszKey);
	return pszValue ? _tstoi (pszValue) : nDefault;
}

/// Updates or deletes a setting.
///
/// @param[in] pszKey the key to update, never NULL
/// @param[in] pszValue the value to write, or NULL to delete
/// @return TRUE if successful, or FALSE if there was a problem
bool CAbstractSettings::Set (const TCHAR *pszKey, const TCHAR *pszValue) {
#ifdef _WIN32
	return RegistrySet (pszKey, pszValue) ? true : false;
#else /* ifdef _WIN32 */
	__unused (pszKey)
	__unused (pszValue)
	TODO (TEXT ("Not implemented"));
	return false;
#endif /* ifdef _WIN32 */
}

/// Enumerates setting keys and values that start with a given prefix. The enumerator receives
/// the key values WITHOUT the prefix.
///
/// @param[in] pszPrefix key prefix
/// @param[in] poEnum enumerator to receive the settings
void CAbstractSettings::Enumerate (const TCHAR *pszPrefix, const CEnumerator *poEnum) const {
	size_t cchPrefix = _tcslen (pszPrefix);
#ifdef _WIN32
	RegistryEnumerate (m_hkeyGlobal);
	RegistryEnumerate (m_hkeyLocal);
#endif /* ifdef _WIN32 */
	struct _setting *pSetting = m_pCache;
	while (pSetting) {
		if (!_tcsncmp (pszPrefix, pSetting->pszKey, cchPrefix)) {
			poEnum->Setting (pSetting->pszKey + cchPrefix, pSetting->pszValue);
		}
		pSetting = pSetting->pNext;
	}
}

/// Returns the location of the settings. This is constructed as the company and product names separated
/// by the file separator character. The names are retrieved from the metadata embedded within the
/// calling executable, DLL or DSO.
///
/// @param[in,out] pszBuffer buffer to build the location into as a null terminated string, never NULL
/// @param[in] cbBufferLen size of the buffer in bytes
/// @return true if the buffer was populated, false if the buffer was not big enough to accept the string
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

/// Mutex to serialise access to providers
CMutex CAbstractSettingProvider::s_oMutex;

/// Creates a new provider instance in an uncalculated state
CAbstractSettingProvider::CAbstractSettingProvider () {
	m_bCalculated = false;
	m_pszValue = NULL;
}

/// Destroys a provider instance. If the default value was calculated, the memory is released.
CAbstractSettingProvider::~CAbstractSettingProvider () {
	if (m_pszValue) {
		delete m_pszValue;
	}
}

/// Returns the string provided, calculating it if necessary.
///
/// @param[in] poSettings the owning settings object, not NULL
/// @return the setting value, possibly NULL
const TCHAR *CAbstractSettingProvider::GetString (const CAbstractSettings *poSettings) const {
	s_oMutex.Enter ();
	if (!m_bCalculated) {
		LOGDEBUG (TEXT ("Calculating default setting value"));
		s_oMutex.Leave ();
		TCHAR *pszValue = CalculateString (poSettings);
		s_oMutex.Enter ();
		if (!m_bCalculated) {
			m_pszValue = pszValue;
			m_bCalculated = true;
		} else {
			LOGDEBUG (TEXT ("Another thread calculated the string"));
			if (pszValue) {
				delete pszValue;
			}
		}
	}
	s_oMutex.Leave ();
	return m_pszValue;
}
