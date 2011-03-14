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
#else /* ifdef _WIN32 */
#include <stdarg.h>
#endif /* ifdef _WIN32 */

#ifndef _WIN32
inline int StringCbPrintfA (char *pszBuffer, size_t cbBuffer, const char *pszFormat, ...) {
	va_list args;
	va_start (args, pszFormat);
	int result;
	result = vsnprintf (pszBuffer, cbBuffer, pszFormat, args);
	if (result < 0) return -1;
	if ((size_t)result > cbBuffer) return -1;
	return 0;
}
#ifdef _WCHAR_H
inline int StringCbPrintfW (wchar_t *pszBuffer, size_t cbBuffer, const wchar_t *pszFormat, ...) {
	va_list args;
	va_start (args, pszFormat);
	int result;
	cbBuffer /= sizeof (wchar_t);
	result = vswprintf (pszBuffer, cbBuffer, pszFormat, args);
	if (result < 0) return -1;
	if ((size_t)result > cbBuffer) return -1;
	return 0;
}
#endif /* ifdef _WCHAR_H */
#ifdef _UNICODE
#define StringCbPrintf StringCbPrintfW
#else /* ifdef _UNICODE */
#define StringCbPrintf StringCbPrintfA
#endif /* ifdef _UNICODE */
#endif /* ifndef _WIN32 */

#ifdef _WCHAR_H
inline wchar_t *AsciiToWideDup (char *pszIn) {
	int cch = strlen (pszIn);
	wchar_t *pszOut = new wchar_t[cch + 1];
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

inline char *WideToAsciiDup (wchar_t *pszIn) {
	int cch = wcslen (pszIn);
	char *pszOut = new char[cch + 1];
#ifdef _WIN32
	WideCharToMultiByte (CP_ACP, 0, pszIn, cchIn, pszOut, sizeof (char)  * (cch + 1), NULL, NULL);
#else /* ifdef _WIN32 */
	char *psz = pszOut;
	while (*pszIn) {
		*(psz++) = *(pszIn++);
	}
	*psz = 0;
#endif /* ifdef _WIN32 */
	return pszOut;
}
#endif /* ifdef _WCHAR_H */

#endif /* ifndef __inc_og_language_util_string_h */
