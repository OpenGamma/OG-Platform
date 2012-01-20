/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_dllversion_h
#define __inc_og_language_util_dllversion_h

#include "Unicode.h"

#ifndef _WIN32
#ifdef DLLVERSION_NO_ERRORS
#define DllVersion_FileDescription	TEXT ("")
#define DllVersion_OriginalFilename	TEXT ("")
#endif /* ifdef DLLVERSION_NO_ERRORS */
#include "DllVersionInfo.h"
#endif /* ifndef _WIN32 */

#define DLLVERSION_ATTRIBUTES \
	ATTRIBUTE (Comments); \
	ATTRIBUTE (CompanyName); \
	ATTRIBUTE (FileDescription); \
	ATTRIBUTE (FileVersion); \
	ATTRIBUTE (InternalName); \
	ATTRIBUTE (LegalCopyright); \
	ATTRIBUTE (OriginalFilename); \
	ATTRIBUTE (ProductName); \
	ATTRIBUTE (ProductVersion); \
	ATTRIBUTE (PrivateBuild); \
	ATTRIBUTE (SpecialBuild);

/// Fetches version information from the current (or another) DLL. The Win32 version will query
/// the embedded version information resource. The Posix version must define the version constants
/// before including this file so that they are embedded statically.
class CDllVersion {
private:
#ifdef _WIN32

	/// Version information data buffer
	PBYTE m_pData;

	/// Language code
	WORD m_wLanguage;

	/// Character set code
	WORD m_wCharSet;

	void Init (HMODULE hModule);
	void Init (PCTSTR pszModule);
	void GetLanguageCode ();
	PCTSTR GetString (PCTSTR pszString) const;
#else /* ifdef _WIN32 */
#define ATTRIBUTE(name) \
	static const TCHAR *s_psz##name
	DLLVERSION_ATTRIBUTES
#undef ATTRIBUTE
#endif /* ifdef _WIN32 */
public:
	CDllVersion ();
#ifdef _WIN32
	CDllVersion (HMODULE hModule);
	CDllVersion (PCTSTR pszModule);
	~CDllVersion ();
	static HMODULE GetCurrentModule ();
	void SetLanguage (WORD wLanguage);
	void SetCharSet (WORD wCharSet);
#define ATTRIBUTE(name) \
	const TCHAR *Get##name () const { return GetString (TEXT (#name)); }
#else /* ifdef _WIN32 */
	/// Populates the static data buffer with version constants defined at compile time.
	static void Initialise () {
#define ATTRIBUTE(name) s_psz##name = TEXT (DllVersion_##name)
		DLLVERSION_ATTRIBUTES
#undef ATTRIBUTE
	}
#define ATTRIBUTE(name) \
	const TCHAR *Get##name () const { return s_psz##name ? s_psz##name : TEXT (DllVersion_##name); }
#endif /* ifdef _WIN32 */
	DLLVERSION_ATTRIBUTES
#undef ATTRIBUTE
};

#endif /* ifndef __inc_og_language_util_dllversion_h */
