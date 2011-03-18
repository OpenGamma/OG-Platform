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
inline int StringCbPrintfA (char *pszBuffer, size_t cbBuffer, const char *pszFormat, ...) {
	va_list args;
	va_start (args, pszFormat);
	int result;
	result = vsnprintf (pszBuffer, cbBuffer, pszFormat, args);
	if (result < 0) return -1;
	if ((size_t)result > cbBuffer) return -1;
	return 0;
}
#ifdef WCHAR_AVAILABLE
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
#endif /* ifdef WCHAR_AVAILABLE */
#ifdef _UNICODE
#define StringCbPrintf StringCbPrintfW
#else /* ifdef _UNICODE */
#define StringCbPrintf StringCbPrintfA
#endif /* ifdef _UNICODE */
#endif /* ifndef _WIN32 */

#ifdef WCHAR_AVAILABLE
inline wchar_t *AsciiToWideDup (const char *pszIn) {
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

inline char *WideToAsciiDup (const wchar_t *pszIn) {
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

#endif /* ifndef __inc_og_language_util_string_h */
