/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_version_h
#define __inc_og_language_version_h

// Major version number
#define VERSION_MAJOR	0

// Minor version number
#define VERSION_MINOR	5

// Revision
#define REVISION	0

// Build system generated build number
#ifndef BUILD_NUMBER
#define BUILD_NUMBER	1
#endif /* ifndef BUILD_NUMBER */

// Textual suffix
#ifndef VERSION_SUFFIX
#ifdef _DEBUG
#define VERSION_SUFFIX "-Debug"
#else /* ifdef _DEBUG */
#define VERSION_SUFFIX ""
#endif /* ifdef _DEBUG */
#endif /* ifndef VERSION_SUFFIX */

// Generated "Version" string
#include <Util/Quote.h>
#define VERSION		TEXT(QUOTE(VERSION_MAJOR)) TEXT(".") TEXT(QUOTE(VERSION_MINOR)) TEXT(".") TEXT(QUOTE(REVISION)) TEXT(".") TEXT(QUOTE(BUILD_NUMBER)) TEXT(VERSION_SUFFIX)

#endif /* ifndef __inc_og_language_version_h */
