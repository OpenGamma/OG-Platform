/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_concurrent_h
#define __inc_og_language_util_concurrent_h

// Concurrency primitives using Win32 API or pthreads

// Critical section
#ifdef _WIN32
#else
#define CRITICAL_SECTION	pthread_mutex_t
inline bool InitializeCriticalSection(CRITICAL_SECTION *pcrit) {
	int ec = pthread_mutex_init (pcrit, NULL);
	if (ec == 0) return true;
	SetLastError (ec);
	return false;
}
#define DeleteCriticalSection pthread_mutex_destroy
#endif

// Threads
#ifdef _WIN32
#define THREAD_HANDLE		HANDLE
#define THREADPROC_RETURN	DWORD
#define THREADPROC_DECLTYPE	WINAPI
#else
#define THREAD_HANDLE		pthread_t
#define THREADPROC_RETURN	void *
#define THREADPROC_DECLTYPE
#endif

// Dynamic libraries
#ifdef _WIN32
#define LIBRARY_HANDLE	HMODULE
#else
#define LIBRARY_HANDLE	lib_handle
#endif

#endif /* ifndef __inc_og_language_util_concurrent_h */