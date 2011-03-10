/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_dllversion_h
#define __inc_og_language_util_dllversion_h

// Fetches version information from the current (or another) DLL

#include "Unicode.h"

#ifndef _WIN32
#ifdef DLLVERSION_NO_ERRORS
#define DllVersion_FileDescription	TEXT ("")
#define DllVersion_OriginalFilename	TEXT ("")
#endif /* ifdef DLLVERSION_NO_ERRORS */
#include "DllVersionInfo.h"
#endif /* ifndef _WIN32 */

#define DLLVERSION_ATTRIBUTES \
	ATTRIBUTE (Comments) \
	ATTRIBUTE (CompanyName) \
	ATTRIBUTE (FileDescription) \
	ATTRIBUTE (FileVersion) \
	ATTRIBUTE (InternalName) \
	ATTRIBUTE (LegalCopyright) \
	ATTRIBUTE (OriginalFilename) \
	ATTRIBUTE (ProductName) \
	ATTRIBUTE (ProductVersion) \
	ATTRIBUTE (PrivateBuild) \
	ATTRIBUTE (SpecialBuild)

class CDllVersion {
private:
#ifdef _WIN32
	PBYTE m_pData;
	void Init (HMODULE hModule);
	void Init (PCTSTR pszModule);
	PCTSTR GetString (PCTSTR pszString);
#else /* ifdef _WIN32 */
#define ATTRIBUTE(name) static const TCHAR *s_psz##name;
	DLLVERSION_ATTRIBUTES
#undef ATTRIBUTE
#endif /* ifdef _WIN32 */
public:
	// Default constructor queries the DLL (or EXE) containing this static code
	CDllVersion ();
#ifdef _WIN32
	// Win32 version will query the version info embedded in the DLL
	CDllVersion (HMODULE hModule);
	CDllVersion (PCTSTR pszModule);
	~CDllVersion ();
	static HMODULE GetCurrentModule ();
#define ATTRIBUTE(name) const TCHAR *Get##name () { return GetString (TEXT (#name)); }
#else
	// Non-Win32 version must defined the version constants before including this file
	static void Initialise () {
#define ATTRIBUTE(name) s_psz##name = TEXT (DllVersion_##name);
		DLLVERSION_ATTRIBUTES
#undef ATTRIBUTE
	}
#define ATTRIBUTE(name) const TCHAR *Get##name () { return s_psz##name ? s_psz##name : TEXT (DllVersion_##name); }
#endif
	DLLVERSION_ATTRIBUTES
#undef ATTRIBUTE
};

#endif /* ifndef __inc_og_language_util_dllversion_h */
