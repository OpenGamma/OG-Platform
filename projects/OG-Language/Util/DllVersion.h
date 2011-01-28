/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_dllversion_h
#define __inc_og_language_util_dllversion_h

// Fetches version information from the current (or another) DLL

#include "Unicode.h"

#ifndef _WIN32
#include "DllVersionInfo.h"
#endif /* ifndef _Win32 */

class CDllVersion {
private:
#ifdef _WIN32
	PBYTE m_pData;
	void Init (HMODULE hModule);
	void Init (PCTSTR pszModule);
	PCTSTR GetString (PCTSTR pszString);
#endif /* ifdef _WIN32 */
public:
	// Default constructor queries the DLL (or EXE) containing this static code
	CDllVersion ();
#ifdef _WIN32
	// Win32 version will query the version info embedded in the DLL
	CDllVersion (HMODULE hModule);
	CDllVersion (PCTSTR pszModule);
	~CDllVersion ();
#define ACCESSOR(attribute)	const TCHAR * Get##attribute () { return GetString (TEXT (#attribute)); }
#else
	// Non-Win32 version must defined the version constants before including this file
#define ACCESSOR(attribute)	const TCHAR * Get##attribute () { return TEXT (DllVersion_##attribute); }
#endif
	ACCESSOR (Comments)
	ACCESSOR (CompanyName)
	ACCESSOR (FileDescription)
	ACCESSOR (FileVersion)
	ACCESSOR (InternalName)
	ACCESSOR (LegalCopyright)
	ACCESSOR (OriginalFilename)
	ACCESSOR (ProductName)
	ACCESSOR (ProductVersion)
	ACCESSOR (PrivateBuild)
	ACCESSOR (SpecialBuild)
#undef ACCESSOR
};

#endif /* ifndef __inc_og_language_util_dllversion_h */