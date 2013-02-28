/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef STDAFX_H
#define STDAFX_H

#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#include <AclAPI.h>
#include <Sddl.h>
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
#include <unistd.h>
#include <sys/stat.h>
#include <sys/wait.h>
#endif
#include <assert.h>

#include <jni.h>

#include <Util/Fudge.h>
#include <Util/Logging.h>
#include <Util/String.h>
#include <Util/Error.h>

#endif /* ifndef STDAFX_H */
