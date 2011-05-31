/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_thread_h
#define __inc_og_language_util_thread_h

// Threads using either Win32 or APR

#ifndef _WIN32
#include <apr-1/apr_thread_proc.h>
#include <unistd.h>
#ifdef HAVE_PTHREAD
#include <pthread.h>
#endif
#include "MemoryPool.h"
#include "Semaphore.h"
#else
#include "Library.h"
#endif

#include "Atomic.h"

class IRunnable {
public:
	IRunnable () { }
	virtual ~IRunnable () { }
	virtual void Run () = 0;
};

class CThread : public IRunnable {
private:
	mutable CAtomicInt m_oRefCount;
	int m_nThreadId;
#ifdef _WIN32
	CLibraryLock *m_poModuleLock;
	HANDLE m_hThread;
	static DWORD WINAPI StartProc (void *pObject);
#else
	static CAtomicInt s_oNextThreadId;
	apr_thread_t *m_pThread;
	mutable CSemaphore m_oTerminate;
	CMemoryPool m_oPool;
	static void *APR_THREAD_FUNC StartProc (apr_thread_t *handle, void *pObject);
#endif
protected:
	~CThread () {
		assert (m_oRefCount.Get () == 0);
#ifdef _WIN32
		assert (!m_poModuleLock);
		if (m_hThread) CloseHandle (m_hThread);
#else
		if (m_pThread) apr_thread_detach (m_pThread);
#endif
	}
public:
	CThread () : IRunnable (), m_oRefCount (1) {
		m_nThreadId = 0;
#ifdef _WIN32
		m_poModuleLock = NULL;
		m_hThread = NULL;
#else
		m_pThread = NULL;
#endif
	}
	void Retain () const {
		m_oRefCount.IncrementAndGet ();
	}
	static void Release (const CThread *poThread) {
		if (poThread->m_oRefCount.DecrementAndGet () == 0) {
			delete poThread;
		}
	}
	bool Start (); // NOT RE-ENTRANT
	int GetThreadId () const {
		return m_nThreadId;
	}
	bool Wait (unsigned long timeout = 0xFFFFFFFF) const { // NOT RE-ENTRANT
#ifdef _WIN32
		switch (WaitForSingleObject (m_hThread, timeout)) {
		case WAIT_ABANDONED :
			SetLastError (ERROR_ABANDONED_WAIT_0);
			return false;
		case WAIT_OBJECT_0 :
			return true;
		case WAIT_TIMEOUT :
			SetLastError (ERROR_TIMEOUT);
			return false;
		default :
			return false;
		}
#else
		if (m_pThread) {
			if (m_oTerminate.Wait (timeout)) {
				// Leave the semaphore signalled
				m_oTerminate.Signal ();
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
#endif
	}
	static bool WaitAndRelease (const CThread *pThread, unsigned long timeout = 0xFFFFFFFF) { // NOT RE-ENTRANT
		bool result = pThread->Wait (timeout);
		Release (pThread);
		return result;
	}
	static void Sleep (unsigned long millis) {
#ifdef _WIN32
		::Sleep (millis);
#else
		usleep (millis * 1000);
#endif
	}
#ifdef Yield
#undef Yield
#endif /* ifdef Yield */
	static void Yield () {
#ifdef _WIN32
		SwitchToThread ();
#elif defined (HAVE_PTHREAD)
		pthread_yield ();
#else
		apr_thread_yield ();
#endif
	}
	static void *CurrentRef () {
#ifdef _WIN32
		return (void*)GetCurrentThreadId ();
#elif defined (HAVE_PTHREAD)
		return (void*)pthread_self ();
#else
		// TODO
		return NULL;
#endif
	}
	static void Interrupt (void *pThreadRef) {
#ifdef HAVE_PTHREAD
		pthread_kill ((pthread_t)pThreadRef, SIGALRM);
#endif
	}
};

#endif /* ifndef __inc_og_language_util_thread_h */
