/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_timeoutio_h
#define __inc_og_language_util_timeoutio_h

// I/O functions which support timeouts

class CTimeoutIO {
private:
#ifdef _WIN32
	OVERLAPPED m_overlapped;
#define FILE_REFERENCE	HANDLE
#else
#define FILE_REFERENCE	int
#endif
	FILE_REFERENCE m_pipe;
public:
	CTimeoutIO (FILE_REFERENCE pipe);
	virtual ~CTimeoutIO ();
	size_t Read (void *pBuffer, size_t cbBuffer, unsigned long timeout);
	size_t Write (const void *pBuffer, size_t cbBuffer, unsigned long timeout);
	bool Flush ();
	bool Close ();
	bool LazyClose (unsigned long timeout);
	bool CancelLazyClose ();
	bool IsClosed ();
};

#endif /* ifndef __inc_og_language_util_timeoutio_h */