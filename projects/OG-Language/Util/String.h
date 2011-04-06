/**
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
#endif /* ifdef WCHAR_AVAILABLE */
#ifdef _UNICODE
#define StringCbPrintf StringCbPrintfW
#else /* ifdef _UNICODE */
#define StringCbPrintf StringCbPrintfA
#endif /* ifdef _UNICODE */
#endif /* ifndef _WIN32 */

#ifdef WCHAR_AVAILABLE
static inline wchar_t *AsciiToWideDup (const char *pszIn) {
	int cch = strlen (pszIn);
	wchar_t *pszOut = (wchar_t*)malloc (sizeof (wchar_t) * (cch + 1));
#ifdef _WIN32
	MultiByteToWideChar (CP_ACP, 0, pszIn, cch, pszOut, cch + 1);
#else
	wchar_t *psz = pszOut;
	while (*pszIn) {
		*(psz++) = *(pszIn++);
	}
	*psz = 0;
#endif /* ifdef _WIN32 */
	return pszOut;
}

static inline char *WideToAsciiDup (const wchar_t *pszIn) {
	int cch = wcslen (pszIn);
	char *pszOut = (char*)malloc (sizeof (char) * (cch + 1));
#ifdef _WIN32
	WideCharToMultiByte (CP_ACP, 0, pszIn, cch, pszOut, sizeof (char)  * (cch + 1), NULL, NULL);
#else /* ifdef _WIN32 */
	char *psz = pszOut;
	while (*pszIn) {
		*(psz++) = *(pszIn++);
	}
	*psz = 0;
#endif /* ifdef _WIN32 */
	return pszOut;
}
#endif /* ifdef WCHAR_AVAILABLE */

#undef WCHAR_AVAILABLE

#if defined (_WIN32) && defined (_XHASH_)
class PTSTR_hasher : public stdext::hash_compare<PTSTR> {
public:
	size_t operator () (PTSTR const &psz) const {
		size_t v = 1;
		PCTSTR _psz = psz;
		while (*_psz) {
			v += (v << 4) + *(_psz++);
		}
		return v;
	}
	bool operator () (PTSTR const &psz1, PTSTR const &psz2) const {
		return _tcscmp (psz1, psz2) < 0;
	}
};

class PCTSTR_hasher : public stdext::hash_compare<PCTSTR> {
public:
	size_t operator () (PCTSTR const &psz) const {
		size_t v = 1;
		PCTSTR _psz = psz;
		while (*_psz) {
			v += (v << 4) + *(_psz++);
		}
		return v;
	}
	bool operator () (PCTSTR const &psz1, PCTSTR const &psz2) const {
		return _tcscmp (psz1, psz2) < 0;
	}
};
#endif /* if defined (_WIN32) && defined (_XHASH_) */

typedef struct _rcstring {
	volatile unsigned int nCount;
	TCHAR szString[1];
} RCSTRING, *PRCSTRING;

void StringRelease (PRCSTRING pstr);
PRCSTRING StringCreate (const TCHAR *pszString);
void StringRetain (PRCSTRING pstr);
#define StringPtr(_prc_) ((const TCHAR*)(_prc_)->szString)

#endif /* ifndef __inc_og_language_util_string_h */
