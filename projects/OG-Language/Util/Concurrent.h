/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_concurrent_h
#define __inc_og_language_util_concurrent_h

// Concurrency primitives using Win32 API or pthreads

#include "Error.h"

// Critical section
#ifdef _WIN32
#else
#define CRITICAL_SECTION					pthread_mutex_t
#define InitializeCriticalSection(pcrit)	PosixLastError(pthread_mutex_init(pcrit, NULL))
#define EnterCriticalSection(pcrit)			PosixLastError(pthread_mutex_lock(pcrit))
#define LeaveCriticalSection(pcrit)			PosixLastError(pthread_mutex_unlock(pcrit))
#define DeleteCriticalSection(pcrit)		PosixLastError(pthread_mutex_destroy(pcrit))
#endif

// Threads
#ifdef _WIN32
#define THREAD_HANDLE			HANDLE
#define THREADPROC_RETURN		DWORD
#define THREADPROC_DECLTYPE		WINAPI
#define DetachThread(handle)	CloseHandle(handle)
#else
#define THREAD_HANDLE			pthread_t
#define THREADPROC_RETURN		void *
#define THREADPROC_DECLTYPE
#define DetachThread(handle)	PosixLastError(pthread_detach(handle))
#endif

// Dynamic libraries
#ifdef _WIN32
#define LIBRARY_HANDLE					HMODULE
#else
#define LIBRARY_HANDLE					void *
#define FreeLibrary(handle)				PosixLastError(dlclose(handle))
#define GetProcAddress(handle,label)	dlsym(handle, label)
#endif

#endif /* ifndef __inc_og_language_util_concurrent_h */