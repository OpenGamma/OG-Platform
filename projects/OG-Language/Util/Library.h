/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_library_h
#define __inc_og_language_util_library_h

// Library handling using Win32 or APR

#ifndef _WIN32
#include <apr-1/apr_dso.h>
#include "MemoryPool.h"
#include "Error.h"
#endif /* ifndef _WIN32 */

#include "Unicode.h"

class CLibrary {
private:
#ifdef _WIN32
	HMODULE m_hModule;
#else
	CMemoryPool m_oPool;
	apr_dso_handle_t *m_pDSO;
#endif
	CLibrary () { }
public:
	~CLibrary () {
#ifdef _WIN32
		FreeLibrary (m_hModule);
#else
		apr_dso_unload (m_pDSO);
#endif
	}
	static CLibrary *Create (const TCHAR *pszPath, const TCHAR *pszSearchPath = NULL) {
		CLibrary *po = new CLibrary ();
#ifdef _WIN32
		if (pszSearchPath) {
			SetDllDirectory (pszSearchPath);
			po->m_hModule = LoadLibraryEx (pszPath, NULL, LOAD_WITH_ALTERED_SEARCH_PATH);
			SetDllDirectory (NULL);
		} else {
			po->m_hModule = LoadLibrary (pszPath);
		}
		if (!po->m_hModule) {
			delete po;
			return NULL;
		}
#else
#ifdef _UNICODE
#error "Unicode not available"
#else
		if (!PosixLastError (apr_dso_load (&po->m_pDSO, pszPath, po->m_oPool))) {
			delete po;
			return NULL;
		}
#endif
#endif
		return po;
	}
	void *GetAddress (const char *pszLabel) const {
#ifdef _WIN32
		return GetProcAddress (m_hModule, pszLabel);
#else
		void *pAddress;
		if (!PosixLastError (apr_dso_sym (&pAddress, m_pDSO, pszLabel))) return NULL;
		return pAddress;
#endif
	}
};

#ifdef _WIN32
class CLibraryLock {
private:
	HMODULE m_hDll;
	CLibraryLock (HMODULE hDll) {
		m_hDll = hDll;
	}
	~CLibraryLock () {
		assert (!m_hDll);
	}
	static HMODULE GetModuleHandleAndDelete (CLibraryLock *poLock) {
		if (poLock) {
			HMODULE hDll = poLock->m_hDll;
			poLock->m_hDll = NULL;
			delete poLock;
			return hDll;
		} else {
			return NULL;
		}
	}
public:
	static CLibraryLock *CreateFromAddress (const void *pAddressInLibrary) {
		HMODULE hDll = NULL;
		if (GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS, (PCTSTR)pAddressInLibrary, &hDll)) {
			return new CLibraryLock (hDll);
		} else {
			return NULL;
		}
	}
	static void UnlockAndDelete (CLibraryLock *poLock) {
		HMODULE hDll = GetModuleHandleAndDelete (poLock);
		if (hDll) {
			FreeLibrary (hDll);
		}
	}
	static DWORD UnlockDeleteAndExitThread (CLibraryLock *poLock, DWORD dwExitCode) {
		HMODULE hDll = GetModuleHandleAndDelete (poLock);
		if (hDll) {
			FreeLibraryAndExitThread (hDll, dwExitCode);
		}
		return dwExitCode;
	}
};
#endif /* ifdef _WIN32 */

#endif /* ifndef __inc_og_language_util_library_h */
