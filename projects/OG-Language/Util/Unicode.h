/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_unicode_h
#define __inc_og_language_util_unicode_h

// Macros and functions for working with Unicode in Win32 style

#if !defined (_WIN32) && !defined (_TCHAR_DEFINED)
// Define equivalent macros that we use
#ifdef _UNICODE
#include <wchar.h>
#define TCHAR		wchar_t
#define TEXT(str)	L##str
#define _tcscmp		wcscmp
#define _tcsdup		wcsdup
#define _tcsicmp	wcscasecmp
#define _tcslen		wcslen
#define _tstoi		?
#else
#include <string.h>
#define TCHAR		char
#define TEXT(str)	str
#define _tcscmp		strcmp
#define _tcsdup		strdup
#define _tcsicmp	strcasecmp
#define _tcslen		strlen
#define _tstoi		atoi
#endif /* ifdef _UNICODE */
#define _TCHAR_DEFINED
#endif

#endif /* ifndef __inc_og_language_util_unicode_h */
