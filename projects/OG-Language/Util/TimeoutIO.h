/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_timeoutio_h
#define __inc_og_language_util_timeoutio_h

#include "Thread.h"

/// I/O functions which support timeouts.
class CTimeoutIO {
public:
#ifdef _WIN32

	/// O/S file reference is HANDLE
	typedef HANDLE FILE_REFERENCE;

private:

	/// Overlapped I/O structure
	OVERLAPPED m_overlapped;

#else /* ifdef _WIN32 */

	/// O/S file reference is int
	typedef int FILE_REFERENCE;

private:

	/// Reference to the thread blocked on I/O
	CAtomicPointer<CThread::INTERRUPTIBLE_HANDLE> m_oBlockedThread;

#endif /* ifdef _WIN32 */
	
	/// O/S file reference
	FILE_REFERENCE m_file;

	/// Close the file after this period of inactivity, in milliseconds.
	volatile unsigned long m_lLazyTimeout;

	/// Indicates if the file has been closed.
	volatile bool m_bClosed;

protected:
#ifdef _WIN32

	/// Returns the OVERLAPPED I/O structure.
	///
	/// @return the OVERLAPPED I/O structure
	OVERLAPPED *GetOverlapped () { return &m_overlapped; }

	bool WaitOnOverlapped (unsigned long timeout);
#else /* ifdef _WIN32 */
	bool BeginOverlapped (unsigned long timeout, bool bRead);
	void EndOverlapped ();
#endif /* ifdef _WIN32 */
	virtual bool CancelIO ();

	/// Returns the underlying O/S file handle
	FILE_REFERENCE GetFile () { return m_file; }

	/// Sets the underlying O/S file handle
	void SetFile (FILE_REFERENCE file) { m_file = file; }

	/// If a lazy close is requested, returns the timeout period in milliseconds.
	///
	/// @return the lazy timeout period, or zero if none
	unsigned long IsLazyClosing () const { return m_lLazyTimeout; }

public:
	CTimeoutIO (FILE_REFERENCE file);
	virtual ~CTimeoutIO ();
	size_t Read (void *pBuffer, size_t cbBuffer, unsigned long timeout);
	size_t Write (const void *pBuffer, size_t cbBuffer, unsigned long timeout);
	bool Flush ();
	bool Close ();
	bool LazyClose (unsigned long timeout);
	bool CancelLazyClose ();

	/// Tests if the I/O instance is closed.
	///
	/// @return true if the instance is closed, false otherwise
	bool IsClosed () const { return m_bClosed; }

};

// This is perhaps a dumb place for this
#ifndef _WIN32
#include <apr-1/apr_time.h>
#define GetTickCount()	((unsigned long)apr_time_as_msec (apr_time_now ()))
#endif

#endif /* ifndef __inc_og_language_util_timeoutio_h */
