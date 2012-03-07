/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_version_h
#define __inc_og_language_util_version_h

#include <build/version.h>
#include <Util/Quote.h>
#define VERSION TEXT(QUOTE(VERSION_MAJOR)) TEXT(".") TEXT(QUOTE(VERSION_MINOR)) TEXT(".") TEXT(QUOTE(REVISION)) TEXT(".") TEXT(QUOTE(BUILD_NUMBER)) TEXT(VERSION_SUFFIX)

#endif /* ifndef __inc_og_language_util_version_h */
