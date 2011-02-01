/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// I/O functions which support timeouts

#include "Logging.h"
#include "TimeoutIO.h"

LOGGING (com.opengamma.language.util.TimeoutIO);

CTimeoutIO::CTimeoutIO (FILE_REFERENCE pipe) {
	m_pipe = pipe;
#ifdef _WIN32
	ZeroMemory (&m_overlapped, sizeof (m_overlapped));
	m_overlapped.hEvent = CreateEvent (NULL, TRUE, FALSE, NULL);
#endif
}

CTimeoutIO::~CTimeoutIO () {
	LOGDEBUG (TEXT ("Closing handle"));
#ifdef _WIN32
	if (CancelIoEx (m_pipe, &m_overlapped)) {
		// This shouldn't have happened as it means another thread is using the pipe still
		LOGFATAL (TEXT ("Operations pending on pipe"));
	} else {
		DWORD dw = GetLastError ();
		if (dw == ERROR_NOT_FOUND) {
			LOGDEBUG (TEXT ("No pending I/O to cancel"));
		} else {
			// This shouldn't have happened as it means another thread is using the pipe still
			LOGFATAL (TEXT ("Operations pending on pipe, error ") << dw);
		}
	}
	CloseHandle (m_pipe);
	CloseHandle (m_overlapped.hEvent);
#else
	close (m_pipe);
#endif
}

size_t CTimeoutIO::Read (void *pBuffer, size_t cbBuffer, unsigned long timeout) {
	TODO (__FUNCTION__);
	return 0;
}

size_t CTimeoutIO::Write (const void *pBuffer, size_t cbBuffer, unsigned long timeout) {
	TODO (__FUNCTION__);
	return 0;
}

bool CTimeoutIO::Flush () {
	TODO (__FUNCTION__);
	return false;
}

bool CTimeoutIO::Close () {
	TODO (__FUNCTION__);
	return false;
}

bool CTimeoutIO::LazyClose (unsigned long timeout) {
	TODO (__FUNCTION__);
	return false;
}

bool CTimeoutIO::CancelLazyClose () {
	TODO (__FUNCTION__);
	return false;
}

bool CTimeoutIO::IsClosed () {
	TODO (__FUNCTION__);
	return false;
}