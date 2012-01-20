/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#define DLLVERSION_NO_ERRORS
#include "DllVersion.h"
#include "Logging.h"
#include "String.h"

LOGGING(com.opengamma.language.util.DllVersion);

/// Creates a new version query for the DLL, EXE or DSO containing this static code.
CDllVersion::CDllVersion () {
#ifdef _WIN32
	m_pData = NULL;
	Init (GetCurrentModule ());
#endif /* ifdef _WIN32 */
}

#ifdef _WIN32

/// Creates a new version query for the DLL referenced by the handle.
///
/// @param[in] hModule handle of the DLL containing the information
CDllVersion::CDllVersion (HMODULE hModule) {
	Init (hModule);
}

/// Creates a new version query for a named DLL or EXE
///
/// @param[in] pszModule full path to the module
CDllVersion::CDllVersion (PCTSTR pszModule) {
	Init (pszModule);
}

/// Initialises the version query with information from the module handle.
///
/// @param[in] hModule handle of the DLL containing the information
void CDllVersion::Init (HMODULE hModule) {
	assert (hModule);
	m_pData = NULL;
	PTSTR pszPath = new TCHAR[MAX_PATH];
	if (GetModuleFileName (hModule, pszPath, MAX_PATH) != 0) {
		Init (pszPath);
	} else {
		LOGWARN (TEXT ("Couldn't lookup DLL filename, error ") << GetLastError ());
	}
	delete pszPath;
}

/// Initialises the version query with information from the named module.
///
/// @param[in] pszModule full path to the module
void CDllVersion::Init (PCTSTR pszModule) {
	assert (pszModule);
	m_wCharSet = 1252;
	m_wLanguage = 0x0809;
	DWORD cbVersionInfo = GetFileVersionInfoSize (pszModule, NULL);
	if (cbVersionInfo != 0) {
		m_pData = new BYTE[cbVersionInfo];
		if (GetFileVersionInfo (pszModule, 0, cbVersionInfo, m_pData)) {
			LOGDEBUG (TEXT ("Loaded DLL information from ") << pszModule);
			GetLanguageCode ();
			return;
		} else {
			LOGWARN (TEXT ("Couldn't get DLL information, error ") << GetLastError ());
		}
		delete m_pData;
	} else {
		LOGWARN (TEXT ("Couldn't get DLL information from '") << pszModule << TEXT ("', error ") << GetLastError ());
	}
	m_pData = NULL;
}

/// Queries the first language and code page defined in the file.
void CDllVersion::GetLanguageCode () {
	if (m_pData) {
		WORD *pwCodes;
		UINT cbCodes;
		if (VerQueryValue (m_pData, TEXT ("\\VarFileInfo\\Translation"), (LPVOID*)&pwCodes, &cbCodes)) {
			if (cbCodes >= sizeof (WORD) * 2) {
				m_wLanguage = pwCodes[0];
				m_wCharSet = pwCodes[1];
				LOGDEBUG (TEXT ("Found language ") << m_wLanguage << TEXT (", code page ") << m_wCharSet);
			} else {
				LOGWARN (TEXT ("Invalid length, ") << cbCodes);
			}
		} else {
			LOGWARN (TEXT ("Couldn't retrieve translation code pages, error ") << GetLastError ());
		}
	}
}

/// Destroys the version query, releasing any internal resources.
CDllVersion::~CDllVersion () {
	if (m_pData != NULL) {
		delete m_pData;
	}
}

/// Obtain a handle to the current module, i.e. one containing this static code.
///
/// @return current module handle
HMODULE CDllVersion::GetCurrentModule () {
	HMODULE hModule;
	if (GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS | GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT, (PCTSTR)&_logger, &hModule)) {
		return hModule;
	} else {
		LOGWARN (TEXT ("Couldn't lookup host DLL handle, error ") << GetLastError ());
		return NULL;
	}
}

/// Sets the language code.
///
/// @param[in] wLanguage language code
void CDllVersion::SetLanguage (WORD wLanguage) {
	m_wLanguage = wLanguage;
}

/// Sets the character set identifier.
///
/// @param[in] wCharSet character set identifier
void CDllVersion::SetCharSet (WORD wCharSet) {
	m_wCharSet = wCharSet;
}

/// Returns a value from the version information resource.
///
/// @param[in] pszValue name of the value to query
/// @return string value, or the empty string if none is defined
PCTSTR CDllVersion::GetString (PCTSTR pszValue) const {
	if (m_pData) {
		TCHAR szValue[128];
		PCTSTR pszResult;
		UINT cResult;
		StringCbPrintf (szValue, sizeof (szValue), TEXT ("\\StringFileInfo\\%04X%04X\\%s"), m_wLanguage, m_wCharSet, pszValue);
		LOGDEBUG (TEXT ("VerQueryValue ") << szValue);
		if (VerQueryValue (m_pData, szValue, (LPVOID*)&pszResult, &cResult)) {
			LOGDEBUG (pszValue << TEXT (" = ") << pszResult);
			return pszResult;
		} else {
			LOGWARN (TEXT ("Couldn't retrieve ") << pszValue << TEXT (", error ") << GetLastError ());
			return TEXT ("");
		}
	} else {
		LOGWARN (TEXT ("No DLL information - ") << pszValue);
		return TEXT ("");
	}
}

#else /* ifdef _WIN32 */

#define ATTRIBUTE(name) const TCHAR *CDllVersion::s_psz##name = NULL
DLLVERSION_ATTRIBUTES
#undef ATTRIBUTE

#endif /* ifdef _WIN32 */
