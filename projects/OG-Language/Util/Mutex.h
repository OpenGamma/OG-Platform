/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_mutex_h
#define __inc_og_language_util_mutex_h

// Mutex using either Win32, pthreads, or APR

#ifdef _WIN32
#elif defined (HAVE_PTHREAD)
#include <pthread.h>
#else
#include <apr-1/apr_thread_mutex.h>
#include "MemoryPool.h"
#endif

class CMutex {
private:
#ifdef _WIN32
	CRITICAL_SECTION m_cs;
#elif defined (HAVE_PTHREAD)
	pthread_mutex_t m_mutex;
#else
	apr_thread_mutex_t *m_pMutex;
	CMemoryPool m_oPool;
#endif
public:
	// Note that mutex construction failure will go unnoticed. If you can't create
	// a mutex, the system is fairly broken and will probably crash soon anyway.
	CMutex () {
#ifdef _WIN32
		InitializeCriticalSection (&m_cs);
#elif defined (HAVE_PTHREAD)
		pthread_mutex_init (&m_mutex, NULL);
#else
		m_pMutex = NULL;
		apr_thread_mutex_create (&m_pMutex, APR_THREAD_MUTEX_DEFAULT, m_oPool);
#endif
	}
	~CMutex () {
#ifdef _WIN32
		DeleteCriticalSection (&m_cs);
#elif defined (HAVE_PTHREAD)
		pthread_mutex_destroy (&m_mutex);
#else
		apr_thread_mutex_destroy (m_pMutex);
#endif
	}
	void Enter () {
#ifdef _WIN32
		EnterCriticalSection (&m_cs);
#elif defined (HAVE_PTHREAD)
		pthread_mutex_lock (&m_mutex);
#else
		apr_thread_mutex_lock (m_pMutex);
#endif
	}
	void Leave () {
#ifdef _WIN32
		LeaveCriticalSection (&m_cs);
#elif defined (HAVE_PTHREAD)
		pthread_mutex_unlock (&m_mutex);
#else
		apr_thread_mutex_unlock (m_pMutex);
#endif
	}
};

#endif /* ifndef __inc_og_language_util_mutex_h */
