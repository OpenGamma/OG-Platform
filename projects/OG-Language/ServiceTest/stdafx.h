/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_servicetest_stdafx_h
#define __inc_og_language_servicetest_stdafx_h

#if defined (_WIN32)
#include <Windows.h>
#ifdef __cplusplus
#pragma warning(disable:4995) /* suppress #pragma deprecated warnings from standard C++ headers */
#endif /* ifdef __cplusplus */
#else
#include <stdio.h>
#include <stdlib.h>
#endif
#include <assert.h>

#include <jni.h>

#include <Util/AbstractTest.h>

#endif /* ifndef __inc_og_language_servicetest_stdafx_h */
