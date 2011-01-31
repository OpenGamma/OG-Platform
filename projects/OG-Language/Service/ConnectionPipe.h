/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_service_connectionpipe_h
#define __inc_og_language_service_connectionpipe_h

// Implementation of the IPC connection for incoming requests

#include "Public.h"

class CConnectionPipe {
private:
	volatile bool m_bClosed;
	volatile unsigned long m_dwIdleTimeout;
#ifdef _WIN32
	HANDLE m_hEvent;
	HANDLE m_hPipe;
#endif
	size_t m_cbBuffer;
	void *m_pBuffer;
	void Init ();
public:
#ifdef _WIN32
	CConnectionPipe (HANDLE hPipe);
#else
	CConnectionPipe ();
#endif
public:
	~CConnectionPipe ();
	static CConnectionPipe *Create (const TCHAR *pszSuffix = NULL);
	PJAVACLIENT_CONNECT ReadMessage ();
	void Close ();
	void LazyClose (unsigned long dwTimeout = 0);
	void CancelLazyClose ();
	bool IsClosed () { return m_bClosed; }
};

#endif /* ifndef __inc_og_language_service_connectionpipe_h */