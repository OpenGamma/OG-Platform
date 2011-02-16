/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_service_stdafx_h
#define __inc_og_language_service_stdafx_h

#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#include <AclAPI.h>
#include <Sddl.h>
#include <tchar.h>
#include <strsafe.h>
#ifdef __cplusplus
#pragma warning(disable:4995) /* suppress #pragma deprecated warnings from standard C++ headers */
#endif /* ifdef __cplusplus */
#else
#include <stdio.h>
#include <stdlib.h>
#include <malloc.h>
#include <pthread.h>
#include <errno.h>
#include <fcntl.h>
#include <dirent.h>
#endif
#include <assert.h>

#include <jni.h>

#include <Util/AbstractSettings.h>
#include <Util/BufferedInput.h>
#include <Util/File.h>
#include <Util/Fudge.h>
#include <Util/Library.h>
#include <Util/Logging.h>
#include <Util/Mutex.h>
#include <Util/NamedPipe.h>
#include <Util/Process.h>
#include <Util/String.h>
#include <Util/Thread.h>

#endif /* ifndef __inc_og_language_service_stdafx_h */
