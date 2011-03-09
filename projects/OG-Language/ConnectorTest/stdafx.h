/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connectortest_stdafx_h
#define __inc_og_language_connectortest_stdafx_h

#if defined (_WIN32)
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#include <tchar.h>
#include <strsafe.h>
#ifdef __cplusplus
#pragma warning(disable:4995) /* suppress #pragma deprecated warnings from standard C++ headers */
#endif /* ifdef __cplusplus */
#else
#include <stdio.h>
#include <stdlib.h>
#endif

#include <assert.h>

#include <Util/AbstractTest.h>
#include <Util/BufferedInput.h>
#include <Util/Fudge.h>
#include <Util/Logging.h>
#include <Util/Mutex.h>
#include <Util/NamedPipe.h>
#include <Util/Process.h>
#include <Util/Semaphore.h>
#include <Util/Thread.h>

#endif /* ifndef __inc_og_language_connectortest_stdafx_h */
