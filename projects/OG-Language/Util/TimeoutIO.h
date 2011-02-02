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
	FILE_REFERENCE m_file;
	volatile unsigned long m_lLazyTimeout;
	volatile bool m_bClosed;
protected:
#ifdef _WIN32
	OVERLAPPED *GetOverlapped () { return &m_overlapped; }
	bool WaitOnOverlapped (unsigned long timeout);
#endif
	virtual bool CancelIO ();
	FILE_REFERENCE GetFile () { return m_file; }
	void SetFile (FILE_REFERENCE file) { m_file = file; }
	unsigned long IsLazyClosing () { return m_lLazyTimeout; }
public:
	CTimeoutIO (FILE_REFERENCE file);
	virtual ~CTimeoutIO ();
	size_t Read (void *pBuffer, size_t cbBuffer, unsigned long timeout);
	size_t Write (const void *pBuffer, size_t cbBuffer, unsigned long timeout);
	bool Flush ();
	bool Close ();
	bool LazyClose (unsigned long timeout);
	bool CancelLazyClose ();
	bool IsClosed () { return m_bClosed; }
};

#endif /* ifndef __inc_og_language_util_timeoutio_h */