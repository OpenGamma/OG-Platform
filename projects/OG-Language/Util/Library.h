/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_library_h
#define __inc_og_language_util_library_h

#ifndef _WIN32
#include <apr-1/apr_dso.h>
#include "MemoryPool.h"
#include "Error.h"
#endif /* ifndef _WIN32 */

#include "Unicode.h"

/// Abstraction of a dynamically loaded library, e.g. a DLL on Windows or DSO on Posix style system.
class CLibrary {
private:

#ifdef _WIN32

	/// Module handle of the library.
	HMODULE m_hModule;

#else /* ifdef _WIN32 */

	/// Memory pool to allocate DSO resources from
	CMemoryPool m_oPool;

	/// Library handle.
	apr_dso_handle_t *m_pDSO;

#endif /* ifdef _WIN32 */

	/// Creates a new library handle instance.
	CLibrary () { }

public:

	/// Destroys the library handle instance, releasing the underlying library.
	~CLibrary () {
#ifdef _WIN32
		FreeLibrary (m_hModule);
#else /* ifdef _WIN32 */
		apr_dso_unload (m_pDSO);
#endif /* ifdef _WIN32 */
	}

	/// Creates a new library reference, if the library exists.
	///
	/// @param[in] pszPath full path to the library file, or file name if pszSearchPath is not NULL
#ifdef _WIN32
	/// @param[in] pszSearchPath path to search for the library, or NULL if pszPath is a full filename
#endif /* ifdef _WIN32 */
	/// @return the library instance, or NULL if the library could not be found
	static CLibrary *Create (const TCHAR *pszPath
#ifdef _WIN32
			, const TCHAR *pszSearchPath = NULL
#endif /* ifdef _WIN32 */
			) {
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

	/// Returns the address of a symbol within the library.
	///
	/// @param[in] pszLabel symol to load, not NULL
	/// @return symbol address or NULL if not found
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
/// Reference-counted lock on a library. The underlying library cannot be unloaded while the lock is
/// held. Use this to maintain the validity of an address returned from a CLibrary instance beyond
/// the possible lifetime of the original CLibrary instance.
class CLibraryLock {
private:

	/// Module handle.
	HMODULE m_hDll;

	/// Creates a new module handle lock.
	CLibraryLock (HMODULE hDll) {
		m_hDll = hDll;
	}

	/// Destroys the lock, must be called after the lock is released, or ownership taken for later
	/// release.
	~CLibraryLock () {
		assert (!m_hDll);
	}

	/// Takes ownership of the module handle from a lock and deletes the lock object.
	///
	/// @param[in] poLock lock to take ownership from and delete
	/// @return the module handle acquired
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

	/// Creates a library lock from an address within the library, typically one returned by
	/// CLibrary::GetAddress.
	///
	/// @param[in] pAddressInLibrary address in the library to lock
	/// @return the library lock object
	static CLibraryLock *CreateFromAddress (const void *pAddressInLibrary) {
		HMODULE hDll = NULL;
		if (GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS, (PCTSTR)pAddressInLibrary, &hDll)) {
			return new CLibraryLock (hDll);
		} else {
			return NULL;
		}
	}

	/// Unlocks a library and deletes the lock object.
	///
	/// @param[in] poLock library lock object
	static void UnlockAndDelete (CLibraryLock *poLock) {
		HMODULE hDll = GetModuleHandleAndDelete (poLock);
		if (hDll) {
			FreeLibrary (hDll);
		}
	}

	/// Unlocks a library, deletes the lock object and exits the calling thread.
	///
	/// @param[in] poLock library lock object
	/// @param[in] dwExitCode thread exit code
	/// @return the thread exit code if the library wasn't locked, otherwise does not return
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
