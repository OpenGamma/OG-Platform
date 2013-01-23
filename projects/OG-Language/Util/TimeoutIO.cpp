/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Logging.h"
#include "TimeoutIO.h"
#include "Mutex.h"
#include "Thread.h"

LOGGING (com.opengamma.language.util.TimeoutIO);

/// Critical section for lazy close operations. A mutex could be created
/// for each I/O instance, but the close operation is rare enough that
/// a global section can be shared easily enough.
static CMutex g_oClosing;

/// Creates a new I/O instance.
///
/// @param[in] file underlying O/S handle
CTimeoutIO::CTimeoutIO (FILE_REFERENCE file) {
	LOGINFO (TEXT ("File opened"));
	m_file = file;
	m_lLazyTimeout = 0;
	m_bClosed = false;
#ifdef _WIN32
	ZeroMemory (&m_overlapped, sizeof (m_overlapped));
	m_overlapped.hEvent = CreateEvent (NULL, TRUE, FALSE, NULL);
#endif /* ifdef _WIN32 */
}

/// Destroys the I/O instance.
CTimeoutIO::~CTimeoutIO () {
	LOGINFO (TEXT ("File closed"));
#ifdef _WIN32
	CloseHandle (m_file);
	CloseHandle (m_overlapped.hEvent);
#else /* ifdef _WIN32 */
	if (m_file) {
		close (m_file);
	}
#endif /* ifdef _WIN32 */
}

#ifdef _WIN32

/// Blocks the caller until the I/O completes, the timeout elapses or a lazy timeout elapses.
///
/// @param[in] timeout maximum time to wait in milliseconds
/// @return true if the I/O completed, false otherwise
bool CTimeoutIO::WaitOnOverlapped (unsigned long timeout) {
	DWORD dwError = GetLastError ();
	if (dwError != ERROR_IO_PENDING) {
		LOGWARN (TEXT ("I/O is not pending, error ") << dwError);
		SetLastError (dwError);
		return false;
	}
	bool bLazyWait = false;
	unsigned long lLazy = IsLazyClosing ();
	if (lLazy) {
		bLazyWait = true;
		timeout = lLazy;
	}
waitOnSignal:
	LOGDEBUG (TEXT ("Waiting for I/O for ") << timeout << TEXT ("ms"));
	if (WaitForSingleObject (m_overlapped.hEvent, timeout) == WAIT_OBJECT_0) {
		if (IsClosed ()) {
			LOGDEBUG (TEXT ("File closed"));
			CancelIoEx (m_file, &m_overlapped);
			SetLastError (ECANCELED);
			return false;
		} else if (!HasOverlappedIoCompleted (&m_overlapped)) {
			if (bLazyWait) {
				LOGWARN (TEXT ("Event signalled without I/O completion during idle wait, failing"));
				CancelIoEx (m_file, &m_overlapped);
				SetLastError (ECANCELED);
				return false;
			} else {
				timeout = IsLazyClosing ();
				LOGDEBUG (TEXT ("Event signalled without I/O completion, using idle timeout of ") << timeout << TEXT ("ms"));
				ResetEvent (m_overlapped.hEvent);
				bLazyWait = true;
				goto waitOnSignal;
			}
		}
	} else {
		LOGDEBUG (TEXT ("Timeout elapsed"));
		if (IsLazyClosing ()) {
			if (bLazyWait) {
				LOGINFO (TEXT ("Closing file on idle timeout"));
				Close ();
			}
		}
		if (IsClosed () || !HasOverlappedIoCompleted (&m_overlapped)) {
			CancelIoEx (m_file, &m_overlapped);
			SetLastError (ERROR_TIMEOUT);
			return false;
		}
	}
	return true;
}

#else /* ifdef _WIN32 */

/// Begin an overlapped I/O operation, blocking until the I/O is able to complete, the timeout
/// elapses or a lazy timeout occurs.
///
/// @param[in] timeout maximum time to wait in milliseconds
/// @param[in] bRead true for a read operation, false for a write
/// @return true if an I/O operation is available, false otherwise
bool CTimeoutIO::BeginOverlapped (unsigned long timeout, bool bRead) {
	if (m_oBlockedThread.GetAndSet (CThread::GetInterruptible ()) == (CThread::INTERRUPTIBLE_HANDLE)-1) {
		unsigned long lLazy = m_lLazyTimeout;
		if (lLazy && (lLazy < timeout)) {
			timeout = lLazy;
		}
	}
	if (IsClosed ()) {
		m_oBlockedThread.Set (0);
		LOGDEBUG (TEXT ("Already closed; no I/O operations available"));
		SetLastError (ECANCELED);
		return false;
	}
	fd_set fds;
	FD_ZERO (&fds);
	FD_SET (m_file, &fds);
	timeval tv;
	tv.tv_sec = timeout / 1000;
	tv.tv_usec = (timeout % 1000) * 1000;
	int n = select (m_file + 1, bRead ? &fds : NULL, bRead ? NULL : &fds, NULL, &tv);
	if (n == 1) {
		assert (FD_ISSET (m_file, &fds));
		// I/O operation should complete without blocking
		return true;
	} else {
		m_oBlockedThread.Set (0);
		if (n == 0) {
			// I/O operation shouldn't complete without blocking
			SetLastError (ETIMEDOUT);
		}
		return false;
	}
}

/// Complete an overlapped I/O operation.
void CTimeoutIO::EndOverlapped () {
	m_oBlockedThread.Set (0);
}

#endif /* ifdef _WIN32 */

/// Read from the underlying, returning when at least one byte has been read, the timeout elapses
/// or a lazy timeout occurs.
///
/// @param[out] pBuffer buffer to read into
/// @param[in] cbBuffer size of buffer in bytes
/// @param[in] timeout maximum time to wait in milliseconds
/// @return the number of bytes read, if any
size_t CTimeoutIO::Read (void *pBuffer, size_t cbBuffer, unsigned long timeout) {
	if (IsClosed ()) {
		LOGWARN (TEXT ("File already closed"));
		SetLastError (ECANCELED);
		return 0;
	}
#ifdef _WIN32
	DWORD cbBytesRead = 0;
#ifdef _WIN64
	if (cbBuffer > MAXDWORD) cbBuffer = MAXDWORD;
#endif /* ifdef _WIN64 */
	if (ReadFile (m_file, pBuffer, (DWORD)cbBuffer, &cbBytesRead, GetOverlapped ())) {
		LOGDEBUG (TEXT ("Read ") << cbBytesRead << TEXT (" immediate data"));
		return cbBytesRead;
	}
	if (!WaitOnOverlapped (timeout)) {
		DWORD dwError = GetLastError ();
		LOGDEBUG (TEXT ("Overlapped result not available, error ") << dwError);
		SetLastError (dwError);
		return 0;
	}
	if (!GetOverlappedResult (m_file, &m_overlapped, &cbBytesRead, FALSE)) {
		// This shouldn't happen as the object was signalled, or HasOverlappedIoCompleted returned TRUE
		DWORD dwError = GetLastError ();
		LOGERROR (TEXT ("Couldn't complete overlapped read, error ") << dwError);
		CancelIoEx (m_file, &m_overlapped);
		SetLastError (dwError);
		return 0;
	}
	return cbBytesRead;
#else /* ifdef _WIN32 */
	bool bLazyWait = false;
	unsigned long lLazy = IsLazyClosing ();
	if (lLazy) {
		bLazyWait = true;
		timeout = lLazy;
	}
timeoutOperation:
	ssize_t cbBytesRead;
	if (BeginOverlapped (timeout, true)) {
		cbBytesRead = read (m_file, pBuffer, cbBuffer);
		EndOverlapped ();
	} else {
		cbBytesRead = -1;
	}
	if (cbBytesRead < 0) {
		int ec = GetLastError ();
		if (ec == EINTR) {
			LOGDEBUG (TEXT ("Read interrupted"));
			lLazy = IsLazyClosing ();
			if (lLazy) {
				if (bLazyWait) {
					LOGINFO (TEXT ("Closing file on idle timeout"));
					Close ();
				}
			}
			if (!IsClosed () && !bLazyWait) {
				bLazyWait = true;
				timeout = lLazy;
				LOGDEBUG (TEXT ("Resuming operation with idle timeout of ") << timeout << TEXT ("ms"));
				goto timeoutOperation;
			}
		}
		LOGWARN (TEXT ("Couldn't read from file, error ") << ec);
		SetLastError (ec);
		return 0;
	} else if (cbBytesRead == 0) {
		LOGWARN (TEXT ("End of stream detected"));
		SetLastError (ECONNRESET);
		return 0;
	}
	return cbBytesRead;
#endif /* ifdef _WIN32 */
}

/// Write to the underlying, returning when at least one byte has been written, the timeout elapses
/// or a lazy timeout occurs.
///
/// @param[in] pBuffer buffer to write
/// @param[in] cbBuffer number of bytes in the buffer
/// @param[in] timeout maximum time to wait in milliseconds
/// @return number of bytes written, if any
size_t CTimeoutIO::Write (const void *pBuffer, size_t cbBuffer, unsigned long timeout) {
	if (IsClosed ()) {
		LOGWARN (TEXT ("File already closed"));
		SetLastError (ECANCELED);
		return 0;
	}
#ifdef _WIN32
	DWORD cbBytesWritten = 0;
#ifdef _WIN64
	if (cbBuffer > MAXDWORD) cbBuffer = MAXDWORD;
#endif /* ifdef _WIN64 */
	if (WriteFile (m_file, pBuffer, (DWORD)cbBuffer, &cbBytesWritten, &m_overlapped)) {
		LOGDEBUG (TEXT ("Write ") << cbBytesWritten << TEXT (" immediate data"));
		return cbBytesWritten;
	}
	if (!WaitOnOverlapped (timeout)) {
		DWORD dwError = GetLastError ();
		LOGDEBUG (TEXT ("Overlapped result not available, error ") << dwError);
		SetLastError (dwError);
		return 0;
	}
	if (!GetOverlappedResult (m_file, &m_overlapped, &cbBytesWritten, FALSE)) {
		// This shouldn't happen as the object was signalled, or HasOverlappedIoCompleted returned TRUE
		DWORD dwError = GetLastError ();
		LOGERROR (TEXT ("Couldn't complete overlapped write, error ") << dwError);
		CancelIoEx (m_file, &m_overlapped);
		SetLastError (dwError);
		return 0;
	}
	return cbBytesWritten;
#else
	bool bLazyWait = false;
	unsigned long lLazy = IsLazyClosing ();
	if (lLazy) {
		bLazyWait = true;
		timeout = lLazy;
	}
timeoutOperation:
	ssize_t cbWritten;
	if (BeginOverlapped (timeout, false)) {
		cbWritten = write (m_file, pBuffer, cbBuffer);
		EndOverlapped ();
	} else {
		cbWritten = -1;
	}
	if (cbWritten < 0) {
		int ec = GetLastError ();
		if (ec == EINTR) {
			LOGDEBUG (TEXT ("Write interrupted"));
			lLazy = IsLazyClosing ();
			if (lLazy) {
				if (bLazyWait) {
					LOGINFO (TEXT ("Closing file on idle timeout"));
					Close ();
				}
			}
			if (!IsClosed () && !bLazyWait) {
				bLazyWait = true;
				timeout = lLazy;
				LOGDEBUG (TEXT ("Resuming operation with idle timeout of ") << timeout << TEXT ("ms"));
				goto timeoutOperation;
			}
		}
		LOGWARN (TEXT ("Couldn't write to file, error ") << ec);
		SetLastError (ec);
		return 0;
	} else if (cbWritten == 0) {
		LOGWARN (TEXT ("No bytes written"));
		SetLastError (ECONNRESET);
		return 0;
	}
	return cbWritten;
#endif
}

/// Flushes all open file buffers, for O/S that support buffering.
///
/// @return true if the buffers were flushed (or the O/S does not support it), false otherwise
bool CTimeoutIO::Flush () {
#ifdef _WIN32
	return FlushFileBuffers (m_file) ? true : false;
#else
	//return fsync (m_file) == 0; // This is not supported for sockets or pipes
	return true;
#endif
}

/// Attempts to cancel the current overlapped I/O operation. Code blocked in Read or Write will then resume and
/// either resume the blocking operation, or abort it fully.
///
/// @return true if the operation could be cancelled, false otherwise
bool CTimeoutIO::CancelIO () {
#ifdef _WIN32
	SetEvent (m_overlapped.hEvent);
#else
	CThread::INTERRUPTIBLE_HANDLE hBlockedThread = m_oBlockedThread.GetAndSet ((CThread::INTERRUPTIBLE_HANDLE)-1);
	if (hBlockedThread) {
		LOGDEBUG (TEXT ("Interrupting thread blocked on I/O"));
		CThread::Interrupt (hBlockedThread);
	} else {
		LOGDEBUG (TEXT ("No pending I/O to cancel"));
	}
#endif
	return true;
}

/// Cancels any pending I/O and closes the underlying resources.
///
/// @return true if the resources could be closed (or were closed already), false if there was a problem
bool CTimeoutIO::Close () {
	if (m_bClosed) {
		LOGWARN (TEXT ("Already closed"));
		return true;
	}
	LOGDEBUG (TEXT ("Closing file"));
	g_oClosing.Enter ();
	m_bClosed = true;
	if (!CancelIO ()) {
		m_bClosed = false;
		g_oClosing.Leave ();
		LOGWARN (TEXT ("Couldn't cancel I/O for close notification, error ") << GetLastError ());
		return false;
	}
	g_oClosing.Leave ();
	return true;
}

/// Sets the timeout period for the current (or next) I/O operation. This may be shorter than the timeout on the
/// I/O operation itself allowing a long running operation to be interrupted and abandoned
///
/// @param[in] timeout maximum time to wait between underlying I/O operations
/// @return true if the lazy closing threshold could be set, false otherwise
bool CTimeoutIO::LazyClose (unsigned long timeout) {
	if (timeout == 0) {
		return Close ();
	} else {
		LOGDEBUG (TEXT ("Lazy closing after ") << timeout << TEXT ("ms"));
		g_oClosing.Enter ();
		if (m_bClosed) {
			g_oClosing.Leave ();
			LOGWARN (TEXT ("Already closed, can't set lazy notification"));
			return false;
		}
		m_lLazyTimeout = timeout;
		if (!CancelIO ()) {
			m_lLazyTimeout = 0;
			g_oClosing.Leave ();
			LOGWARN (TEXT ("Couldn't cancel I/O for lazy close notification, error ") << GetLastError ());
			return false;
		}
		g_oClosing.Leave ();
		return true;
	}
}

/// Cancels the previous call to LazyClose - i.e. current and future I/O operations will be allowed their requested timeout
///
/// @return true if the lazy closing threshold could be reset, false otherwise
bool CTimeoutIO::CancelLazyClose () {
	LOGDEBUG (TEXT ("Cancelling lazy close (was ") << m_lLazyTimeout << TEXT ("ms)"));
	m_lLazyTimeout = 0;
#ifndef _WIN32
	m_oBlockedThread.CompareAndSet (0, (CThread::INTERRUPTIBLE_HANDLE)-1);
#endif /* ifndef _WIN32 */
	return true;
}
