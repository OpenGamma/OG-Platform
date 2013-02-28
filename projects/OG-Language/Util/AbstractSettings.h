/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_abstractsettings_h
#define __inc_og_language_util_abstractsettings_h

// Runtime configuration options

#include "Mutex.h"
#include "Unicode.h"

/// Cached setting - a key/value pair. Entries form a singly linked list.
///
/// Note the cache is not about performance, but more to track the memory we've allocated. It is
/// unlikely that keys will be accessed at random, only once anyway, so speedy search lookups
/// from a hash implementation aren't that helpful.
struct _setting {
	/// Key, never NULL
	TCHAR * pszKey;
	/// Value, never NULL
	TCHAR * pszValue;
	/// Next node in the list or NULL if this is the last
	struct _setting *pNext;
};

class CAbstractSettings;

/// Base class for lazy generation of default values. Default values that are computationally
/// expensive to generate should be produced by a provider to avoid the overhead if the user
/// has explicitly provided that setting.
///
/// A static instance of a subclass should be created which can be referenced by setting
/// query methods when needed. Internal caching will mean the default is only calculated at
/// most once.
class CAbstractSettingProvider {
private:
	static CMutex s_oMutex;

	/// Whether the value has been calculated yet. If true, m_pszValue can be used, otherwise
	/// CalculateString must be called and the result stored in m_pszValue.
	mutable bool m_bCalculated;

	/// The string to provide, cached. Only valid if m_bCalculated is true.
	mutable TCHAR *m_pszValue;

protected:

	/// Calculates the default value. The default value may be NULL. If a string is returned
	/// it must be allocated on the heap - the memory will be freed by the destructor.
	///
	/// @param pOwner the calling CAbstractSettings instance, not NULL
	/// @return the default value, possibly NULL
	virtual TCHAR *CalculateString (const CAbstractSettings *pOwner) const = 0;

public:
	CAbstractSettingProvider ();
	~CAbstractSettingProvider ();
	const TCHAR *GetString (const CAbstractSettings *pOwner) const;
};

/// Base class for querying application settings. The Windows implementation uses the registry for storage
/// while the Posix implementation will use a flat file on disk. The GetSettingsLocation method identifies
/// the exact settings to use (e.g. registry key names or filenames).
///
/// Under Windows, the settings are held under HKLM or HKCU \\Software\\&lt;company name&gt;\\&lt;product&gt;.
/// The settings in HKCU take precedence over settings in HKLM allowing a per user override.
///
/// Under Posix, the settings are held in a file at $HOME/etc/&lt;company name&gt;/&lt;product&gt;,
/// /usr/local/etc/&lt;company name&gt;/&lt;product&gt;, or /etc/&lt;company name&gt;/&lt;product&gt;.
/// Entries in the file are of the form &lt;key&gt;=&lt;value&gt; with lines starting # ignored.
///
/// The company and product names used to locate the settings are retrieved from the metadata embedded
/// within the calling executable, DLL, or DSO.
class CAbstractSettings {
private:
#ifdef _WIN32

	/// Settings key under HKEY_CURRENT_USER
	HKEY m_hkeyLocal;

	/// Settings key under HKEY_LOCAL_MACHINE
	HKEY m_hkeyGlobal;

#endif

	/// Head of the cached settings linked-list
	mutable struct _setting * m_pCache;

protected:
	void CacheRemove (const TCHAR *pszKey) const;
	const TCHAR *CacheGet (const TCHAR *pszKey) const;
	const TCHAR *CachePut (const TCHAR *pszKey, const TCHAR *pszValue) const;
	const TCHAR *CacheReplace (const TCHAR *pszKey, const TCHAR *pszValue) const;
#ifdef _WIN32
	PCTSTR RegistryGet (HKEY hKey, PCTSTR pszKey) const;
	BOOL RegistrySet (HKEY hkey, PCTSTR pszKey, PCTSTR pszValue);
	BOOL RegistrySet (PCTSTR pszBase, PCTSTR pszKey, PCTSTR pszValue);
	BOOL RegistrySet (PCTSTR pszKey, PCTSTR pszValue);
	HKEY RegistryOpenLocal (PCTSTR pszKey) const;
	HKEY RegistryOpenGlobal (PCTSTR pszKey) const;
	void RegistryEnumerate (HKEY hkey) const;
#endif
	const TCHAR *Get (const TCHAR *pszKey) const;
	const TCHAR *Get (const TCHAR *pszKey, const TCHAR *pszDefault) const;
	const TCHAR *Get (const TCHAR *pszKey, const CAbstractSettingProvider *poDefault) const;
	int Get (const TCHAR *pszKey, int nDefault) const;
	bool Set (const TCHAR *pszKey, const TCHAR *pszValue);
public:

	/// Enumeration of setting key/value pairs.
	class CEnumerator {
	public:

		/// Handles an enumerated setting.
		///
		/// @param[in] pszKey setting key
		/// @param[in] pszValue setting value
		virtual void Setting (const TCHAR *pszKey, const TCHAR *pszValue) const = 0;

	};

protected:
	void Enumerate (const TCHAR *pszPrefix, const CEnumerator *poEnum) const;
public:
	CAbstractSettings ();
	~CAbstractSettings ();
	static bool GetSettingsLocation (TCHAR *pszBuffer, size_t cbBufferLen);
	virtual const TCHAR *GetLogConfiguration () const = 0;
};

#endif /* ifndef __inc_og_language_util_abstractsettings_h */
