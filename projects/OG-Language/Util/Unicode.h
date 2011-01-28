/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_unicode_h
#define __inc_og_language_util_unicode_h

// Macros and functions for working with Unicode in Win32 style

#ifndef _WIN32
// Define equivalent macros that we use
#ifdef _UNICODE
#include <wchar.h>
#define TCHAR wchar_t
#define TEXT(str) L##str
#else
#define TCHAR char
#define TEXT(str) str
#endif /* ifdef _UNICODE */
#endif

#endif /* ifndef __inc_og_language_util_unicode_h */