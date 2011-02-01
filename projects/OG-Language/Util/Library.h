/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_library_h
#define __inc_og_language_util_library_h

// Library handling using Win32 or APR

#ifndef _WIN32
#include <apr-1/apr_dso.h>
#include "MemoryPool.h"
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
		if (!PosixLastError (apr_dso_load (&po->m_pDSO, pszPath, m_oPool))) {
			delete po;
			return NULL;
		}
#endif
#endif
		return po;
	}
	void *GetAddress (const char *pszLabel) {
#ifdef _WIN32
		return GetProcAddress (m_hModule, pszLabel);
#else
		apr_dso_handle_sym_t *pSym;
		if (!PosixLastError (apr_dso_sym (&pSym, m_pDSO, pszLabel))) return NULL;
		return pSym;
#endif
	}
};

#endif /* ifndef __inc_og_language_util_library_h */