/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// I/O functions which support timeouts

#include "Logging.h"
#include "TimeoutIO.h"
#include "Mutex.h"

LOGGING (com.opengamma.language.util.TimeoutIO);

static CMutex g_oClosing;

CTimeoutIO::CTimeoutIO (FILE_REFERENCE file) {
	LOGINFO (TEXT ("File opened"));
	m_file = file;
	m_lLazyTimeout = 0;
	m_bClosed = false;
#ifdef _WIN32
	ZeroMemory (&m_overlapped, sizeof (m_overlapped));
	m_overlapped.hEvent = CreateEvent (NULL, TRUE, FALSE, NULL);
#endif
}

CTimeoutIO::~CTimeoutIO () {
	LOGINFO (TEXT ("File closed"));
#ifdef _WIN32
	CloseHandle (m_file);
	CloseHandle (m_overlapped.hEvent);
#else
	close (m_file);
#endif
}

#ifdef _WIN32
bool CTimeoutIO::WaitOnOverlapped (unsigned long timeout) {
	DWORD dwError = GetLastError ();
	if (dwError != ERROR_IO_PENDING) {
		LOGWARN (TEXT ("I/O is not pending, error ") << dwError);
		SetLastError (dwError);
		return false;
	}
	bool bLazyWait = false;
	if (IsLazyClosing ()) {
		bLazyWait = true;
		timeout = IsLazyClosing ();
	}
waitOnSignal:
	LOGDEBUG (TEXT ("Waiting for I/O for ") << timeout << TEXT ("ms"));
	if (WaitForSingleObject (m_overlapped.hEvent, timeout) == WAIT_OBJECT_0) {
		if (IsClosed ()) {
			LOGDEBUG (TEXT ("File closed"));
			CancelIoEx (m_file, &m_overlapped);
			SetLastError (ERROR_HANDLES_CLOSED);
			return false;
		} else if (!HasOverlappedIoCompleted (&m_overlapped)) {
			if (bLazyWait) {
				LOGWARN (TEXT ("Event signalled without I/O completion during idle wait, failing"));
				CancelIoEx (m_file, &m_overlapped);
				SetLastError (ERROR_HANDLES_CLOSED);
				return false;
			} else {
				timeout = IsLazyClosing ();
				LOGDEBUG (TEXT ("Event signalled without I/O completion, using idle timeout of ") << timeout << TEXT ("ms"));
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
#endif

size_t CTimeoutIO::Read (void *pBuffer, size_t cbBuffer, unsigned long timeout) {
	if (IsClosed ()) {
		LOGWARN (TEXT ("File already closed"));
		SetLastError (ERROR_HANDLES_CLOSED);
		return 0;
	}
#ifdef _WIN32
	DWORD cbBytesRead = 0;
	if (ReadFile (m_file, pBuffer, cbBuffer, &cbBytesRead, GetOverlapped ())) {
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
#else
	TODO (TEXT ("Write to file"));
	return 0;
#endif
}

size_t CTimeoutIO::Write (const void *pBuffer, size_t cbBuffer, unsigned long timeout) {
	if (IsClosed ()) {
		LOGWARN (TEXT ("File already closed"));
		SetLastError (ERROR_HANDLES_CLOSED);
		return 0;
	}
#ifdef _WIN32
	DWORD cbBytesWritten = 0;
	if (WriteFile (m_file, pBuffer, cbBuffer, &cbBytesWritten, &m_overlapped)) {
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
	TODO (TEXT ("Write to file"));
	return 0;
#endif
}

bool CTimeoutIO::Flush () {
#ifdef _WIN32
	return FlushFileBuffers (m_file) ? true : false;
#else
	TODO (TEXT ("Flush file"));
	return false;
#endif
}

bool CTimeoutIO::CancelIO () {
#ifdef _WIN32
	SetEvent (m_overlapped.hEvent);
#else
	TODO (TEXT ("Cancel any pending I/O"));
#endif
	return true;
}

// Cancel any pending I/O and close the underlying resource
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

// The current (or next) I/O operation will timeout after the given time (if shorter than the timeout on the I/O)
bool CTimeoutIO::LazyClose (unsigned long timeout) {
	if (timeout == 0) {
		return Close ();
	} else {
		LOGDEBUG (TEXT ("Lazy closing after ") << timeout << TEXT ("ms"));
		g_oClosing.Enter ();
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

// Cancels the previous call to LazyClose - i.e. current and future I/O operations will be allowed their requested timeout
bool CTimeoutIO::CancelLazyClose () {
	LOGDEBUG (TEXT ("Cancelling lazy close (was ") << m_lLazyTimeout << TEXT ("ms)"));
	m_lLazyTimeout = 0;
	return true;
}
