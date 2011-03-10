/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_string_h
#define __inc_og_language_util_string_h

#ifndef _WIN32
#include <stdarg.h>
#endif

#include "Unicode.h"

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

#endif /* ifndef __inc_og_language_util_string_h */
