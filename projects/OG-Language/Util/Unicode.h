/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_unicode_h
#define __inc_og_language_util_unicode_h

// Macros and functions for working with Unicode in Win32 style

#ifdef _WIN32
# include <tchar.h>
#else /* ifdef _WIN32 */
# ifndef _TCHAR_DEFINED
// Define equivalent macros that we use
#  ifdef _UNICODE
#   include <wchar.h>
#   define TCHAR		wchar_t
#   define TEXT(str)	L##str
#   define _tcscmp		wcscmp
#   define _tcsncmp		wcsncmp
#   define _tcsdup		wcsdup
#   define _tcsicmp		wcscasecmp
#   define _tcslen		wcslen
#   define _tcsrchr		wcsrchr
#   define _tcstok_s(a,b,c)	wcstok(a,b)
#   define _tstoi		?
#   define _tstof		?
#  else /* ifdef _UNICODE */
#   include <string.h>
#   define TCHAR		char
#   define TEXT(str)	str
#   define _tcscmp		strcmp
#   define _tcsncmp		strncmp
#   define _tcsdup		strdup
#   define _tcsicmp		strcasecmp
#   define _tcslen		strlen
#   define _tcsrchr		strrchr
#   define _tcstok_s(a,b,c)	strtok(a,b)
#   define _tstoi		atoi
#   define _tstof		atof
#  endif /* ifdef _UNICODE */
#  define _TCHAR_DEFINED
# endif /* ifmdef _TCHAR_DEFINED */
#endif /* ifdef _WIN32 */

#ifdef _UNICODE
# define _tcsAsciiDup	WideToAsciiDup
# define Ascii_tcsDup	AsciiToWideDup
# define _tcsWideDup	wcsdup
# define Wide_tcsDup	wcsdup
#else /* ifdef _UNICODE */
# define _tcsAsciiDup	strdup
# define Ascii_tcsDup	strdup
# define _tcsWideDup	AsciiToWideDup
# define Wide_tcsDup	WideToAsciiDup
#endif /* ifdef _UNICODE */

#endif /* ifndef __inc_og_language_util_unicode_h */
