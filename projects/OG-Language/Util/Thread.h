/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_thread_h
#define __inc_og_language_util_thread_h

#ifndef _WIN32
#include <apr-1/apr_portable.h>
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

/// Base class of an object that can be executed.
class IRunnable {
public:
	IRunnable () { }
	virtual ~IRunnable () { }
	virtual void Run () = 0;
};

/// Base class of a thread abstraction.
///
/// This is a reference counted object using the Retain and Release methods.
class CThread : public IRunnable {
private:

	/// Reference count.
	mutable CAtomicInt m_oRefCount;

	/// Thread identifier.
	int m_nThreadId;

#ifdef _WIN32

	/// Lock on the module containing the thread's code.
	CLibraryLock *m_poModuleLock;

	/// System thread handle.
	HANDLE m_hThread;

	static DWORD WINAPI StartProc (void *pObject);
#else /* ifdef _WIN32 */

	/// Source of synthetic thread identifiers to uniquely identify them.
	static CAtomicInt s_oNextThreadId;

	/// Underlying thread handle.
	apr_thread_t *m_pThread;
	
	/// Termination semaphore for synchronising on thread exit.
	mutable CSemaphore m_oTerminate;

	/// APR memory pool for thread resources
	CMemoryPool m_oPool;

	static void *APR_THREAD_FUNC StartProc (apr_thread_t *handle, void *pObject);
#endif /* ifdef _WIN32 */
protected:

	/// Destroys the thread instance.
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

	/// Creates a new thread instance.
	CThread () : IRunnable (), m_oRefCount (1) {
		m_nThreadId = 0;
#ifdef _WIN32
		m_poModuleLock = NULL;
		m_hThread = NULL;
#else
		m_pThread = NULL;
#endif
	}

	/// Increments the reference count
	void Retain () const {
		m_oRefCount.IncrementAndGet ();
	}

	/// Decrements the reference count, deleting the thread when it reaches zero.
	///
	/// @param[in] poThread thread to release, never NULL
	static void Release (const CThread *poThread) {
		if (poThread->m_oRefCount.DecrementAndGet () == 0) {
			delete poThread;
		}
	}

	bool Start ();

	/// Returns the thread identifier.
	///
	/// @return thread identifier
	int GetThreadId () const {
		return m_nThreadId;
	}

	/// Waits for thread completion. This is not re-entrant.
	///
	/// @param[in] timeout maximum time to wait for the thread to exit in milliseconds
	/// @return true if the thread has completed, false otherwise
	bool Wait (unsigned long timeout = 0xFFFFFFFF) const {
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

	/// Waits for thread completion, and releases the thread. This is not re-entrant.
	///
	/// @param[in] pThread thread to wait for and release
	/// @param[in] timeout maximum time to wait in milliseconds
	/// @return true if the thread completed, false otherwise. The thread is always released regardless
	///         of the return value.
	static bool WaitAndRelease (const CThread *pThread, unsigned long timeout = 0xFFFFFFFF) {
		bool result = pThread->Wait (timeout);
		Release (pThread);
		return result;
	}

	/// Waits for a given period.
	///
	/// @param[in] millis milliseconds to wait for
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
	/// Yields execution to another runnable thread.
	static void Yield () {
#ifdef _WIN32
		SwitchToThread ();
#elif defined (HAVE_PTHREAD)
		pthread_yield ();
#else
		apr_thread_yield ();
#endif
	}

#ifdef _WIN32
	typedef DWORD THREAD_REF;
#else
	typedef apr_os_thread_t THREAD_REF;
#endif

	/// Returns a reference that describes the calling thread for debugging/testing purposes.
	///
	/// @return a thread reference
	static THREAD_REF GetThreadRef () {
#ifdef _WIN32
		return GetCurrentThreadId ();
#else
		return apr_os_thread_current ();
#endif
	}

#ifdef HAVE_PTHREAD
	typedef pthread_t INTERRUPTIBLE_HANDLE;
#else
#define NONINTERRUPTIBLE_THREADS
#endif

#ifndef NONINTERRUPTIBLE_THREADS

	/// Gets an interruptible reference for the calling thread.
	///
	/// @return the interruptible reference
	static INTERRUPTIBLE_HANDLE GetInterruptible () {
#ifdef HAVE_PTHREAD
		return pthread_self ();
#else
#error
#endif
	}

	/// Interrupts a thread, e.g. sending it SIGALRM on posix, to release any blocking operations.
	///
	/// @param[in] handle interruptible handle returned by GetInterruptible
	static void Interrupt (INTERRUPTIBLE_HANDLE handle) {
#ifdef HAVE_PTHREAD
		pthread_kill (handle, SIGALRM);
#else
#error
#endif
	}

#else /* ifndef NONINTERRUPTIBLE_THREADS */
#undef NONINTERRUPTIBLE_THREADS
#endif /* ifndef NONINTERRUPTIBLE_THREADS */

#ifdef _WIN32
	/// Gets the underlying Win32 handle for the thread allowing normal O/S calls.
	HANDLE GetWin32Handle () {
		return m_hThread;
	}
#endif /* ifdef _WIN32 */

};

#endif /* ifndef __inc_og_language_util_thread_h */
