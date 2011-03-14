/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_stdafx_h
#define __inc_og_language_util_stdafx_h

// Common include file for all source code in Util

#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#include <Psapi.h>
#ifdef __cplusplus
#pragma warning(disable:4995) /* suppress #pragma deprecated warnings from standard C++ headers */
#endif /* ifdef __cplusplus */
#else
#include <stdio.h>
#include <stdlib.h>
#include <malloc.h>
#include <fcntl.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <arpa/inet.h>
#include <netinet/tcp.h>
#include <signal.h>
#include <unistd.h>
#include <dirent.h>
#include <spawn.h>
#endif
#include <assert.h>

// Include these so that cpptask fails if libraries not present
#include <fudge/fudge.h>
#include <log4cxx/log4cxx.h>

#endif /* ifndef __inc_og_language_util_stdafx_h */
