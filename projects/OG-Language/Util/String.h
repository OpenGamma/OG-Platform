/*
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_string_h
#define __inc_og_language_util_string_h

#include "Unicode.h"

#ifdef _WIN32
#include <strsafe.h>
#ifdef _INC_WCHAR
#define WCHAR_AVAILABLE
#endif /* ifdef _INC_WCHAR */
#else /* ifdef _WIN32 */
#include <stdarg.h>
#ifdef _WCHAR_H
#define WCHAR_AVAILABLE
#endif /* ifdef _WCHAR_H */
#endif /* ifdef _WIN32 */

#ifndef _WIN32
static inline int StringCbPrintfA (char *pszBuffer, size_t cbBuffer, const char *pszFormat, ...) {
	va_list args;
	va_start (args, pszFormat);
	int result;
	result = vsnprintf (pszBuffer, cbBuffer, pszFormat, args);
	if (result < 0) return -1;
	if ((size_t)result > cbBuffer) return -1;
	return 0;
}
#ifdef WCHAR_AVAILABLE
static inline int StringCbPrintfW (wchar_t *pszBuffer, size_t cbBuffer, const wchar_t *pszFormat, ...) {
	va_list args;
	va_start (args, pszFormat);
	int result;
	cbBuffer /= sizeof (wchar_t);
	result = vswprintf (pszBuffer, cbBuffer, pszFormat, args);
	if (result < 0) return -1;
	if ((size_t)result > cbBuffer) return -1;
	return 0;
}

static inline int StringCchPrintfW (wchar_t *pszBuffer, size_t cchBuffer, const wchar_t *pszFormat, ...) {
	va_list args;
	va_start (args, pszFormat);
	int result;
	result = vswprintf (pszBuffer, cchBuffer, pszFormat, args);
	if (result < 0) return -1;
	if ((size_t)result > cchBuffer) return -1;
	return 0;
}
#endif /* ifdef WCHAR_AVAILABLE */
#ifdef _UNICODE
#define StringCbPrintf StringCbPrintfW
#define StringCchPrintf StringCchPrintfW
#else /* ifdef _UNICODE */
#define StringCbPrintf StringCbPrintfA
#define StringCchPrintf StringCbPrintfA
#endif /* ifdef _UNICODE */
#endif /* ifndef _WIN32 */

#ifdef WCHAR_AVAILABLE

/// Duplicates a string, converting from Ascii to Wide characters.
///
/// @param[in] pszIn string to copy
/// @return the new string
static inline wchar_t *AsciiToWideDup (const char *pszIn) {
	size_t cch = strlen (pszIn) + 1;
#ifdef _WIN64
	if (cch > MAXDWORD) return NULL;
#endif /* ifdef _WIN64 */
	wchar_t *pszOut = (wchar_t*)malloc (sizeof (wchar_t) * cch);
	if (pszOut) {
#ifdef _WIN32
		MultiByteToWideChar (CP_ACP, 0, pszIn, (int)cch, pszOut, (int)cch);
#else /* ifdef _WIN32 */
		wchar_t *psz = pszOut;
		while (*pszIn) {
			*(psz++) = *(pszIn++);
		}
		*psz = 0;
#endif /* ifdef _WIN32 */
	}
	return pszOut;
}

/// Duplicates a string, converting from Wide to Ascii characters.
///
/// @param[in] pszIn string to copy
/// @return the new string
static inline char *WideToAsciiDup (const wchar_t *pszIn) {
	size_t cch = wcslen (pszIn) + 1;
#ifdef _WIN64
	if (cch > MAXINT32) return NULL;
#endif /* ifdef _WIN64 */
	char *pszOut = (char*)malloc (sizeof (char) * cch);
	if (pszOut) {
#ifdef _WIN32
		WideCharToMultiByte (CP_ACP, 0, pszIn, (int)cch, pszOut, sizeof (char)  * (int)cch, NULL, NULL);
#else /* ifdef _WIN32 */
		char *psz = pszOut;
		while (*pszIn) {
			*(psz++) = *(pszIn++);
		}
		*psz = 0;
#endif /* ifdef _WIN32 */
	}
	return pszOut;
}

#endif /* ifdef WCHAR_AVAILABLE */

#undef WCHAR_AVAILABLE

#if defined (_WIN32) && defined (_XHASH_)

/// Hashes a string.
class PTSTR_hasher : public stdext::hash_compare<PTSTR> {
public:

	/// Returns a hash code for a string.
	/// 
	/// @param[in] psz string to hash
	/// @return the hashed value
	size_t operator () (PTSTR const &psz) const {
		size_t v = 1;
		PCTSTR _psz = psz;
		while (*_psz) {
			v += (v << 4) + *(_psz++);
		}
		return v;
	}

	/// Orders two strings.
	///
	/// @param[in] psz1 first string
	/// @param[in] psz2 second string
	/// @return true if the first string is strictly less than the second
	bool operator () (PTSTR const &psz1, PTSTR const &psz2) const {
		return _tcscmp (psz1, psz2) < 0;
	}
};

/// Hashes a string.
class PCTSTR_hasher : public stdext::hash_compare<PCTSTR> {
public:

	/// Returns a hash code for a string.
	///
	/// @param[in] psz string to hash
	/// @return the hashed value
	size_t operator () (PCTSTR const &psz) const {
		size_t v = 1;
		PCTSTR _psz = psz;
		while (*_psz) {
			v += (v << 4) + *(_psz++);
		}
		return v;
	}

	/// Orders two strings.
	///
	/// @param[in] psz1 first string
	/// @param[in] psz2 second string
	/// @return true if the first string is strictly less than the second
	bool operator () (PCTSTR const &psz1, PCTSTR const &psz2) const {
		return _tcscmp (psz1, psz2) < 0;
	}

};

#endif /* if defined (_WIN32) && defined (_XHASH_) */

// TODO: change this to an object

/// Reference counted string.
typedef struct _rcstring {
	/// Reference count
	volatile unsigned int nCount;
	/// String data
	TCHAR szString[1];
} RCSTRING, *PRCSTRING;

void StringRelease (PRCSTRING pstr);
PRCSTRING StringCreate (const TCHAR *pszString);
void StringRetain (PRCSTRING pstr);

// Returns the string from a reference counted string object
#define StringPtr(_prc_) ((const TCHAR*)(_prc_)->szString)

#endif /* ifndef __inc_og_language_util_string_h */
