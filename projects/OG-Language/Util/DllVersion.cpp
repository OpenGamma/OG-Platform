/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Fetches version information from the current (or another) DLL

#define DLLVERSION_NO_ERRORS
#include "DllVersion.h"
#include "Logging.h"

LOGGING(com.opengamma.language.util.DllVersion);

CDllVersion::CDllVersion () {
#ifdef _WIN32
	m_pData = NULL;
	Init (GetCurrentModule ());
#endif /* ifdef _WIN32 */
}

#ifdef _WIN32

CDllVersion::CDllVersion (HMODULE hModule) {
	Init (hModule);
}

CDllVersion::CDllVersion (PCTSTR pszModule) {
	Init (pszModule);
}

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

void CDllVersion::Init (PCTSTR pszModule) {
	assert (pszModule);
	DWORD cbVersionInfo = GetFileVersionInfoSize (pszModule, NULL);
	if (cbVersionInfo != 0) {
		m_pData = new BYTE[cbVersionInfo];
		if (GetFileVersionInfo (pszModule, 0, cbVersionInfo, m_pData)) {
			LOGDEBUG (TEXT ("Loaded DLL information from ") << pszModule);
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

CDllVersion::~CDllVersion () {
	if (m_pData != NULL) {
		delete m_pData;
	}
}

HMODULE CDllVersion::GetCurrentModule () {
	HMODULE hModule;
	if (GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS | GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT, (PCTSTR)&_logger, &hModule)) {
		return hModule;
	} else {
		LOGWARN (TEXT ("Couldn't lookup host DLL handle, error ") << GetLastError ());
		return NULL;
	}
}

PCTSTR CDllVersion::GetString (PCTSTR pszValue) {
	if (m_pData) {
		TCHAR szValue[128];
		PCTSTR pszResult;
		UINT cResult;
		StringCbPrintf (szValue, sizeof (szValue), TEXT ("\\StringFileInfo\\%04X%04X\\%s"), 0x0809, 1252, pszValue);
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

#endif /* ifdef _WIN32 */
