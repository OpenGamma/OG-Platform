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
	volatile BOOL m_bClosed;
	volatile DWORD m_dwIdleTimeout;
	HANDLE m_hEvent;
	HANDLE m_hPipe;
	DWORD m_cbBuffer;
	PBYTE m_pbBuffer;
public:
	CConnectionPipe (HANDLE hPipe);
public:
	~CConnectionPipe ();
	static CConnectionPipe *Create (PCTSTR pszSuffix = NULL);
	PJAVACLIENT_CONNECT ReadMessage ();
	void Close ();
	void LazyClose (DWORD dwTimeout = 0);
	void CancelLazyClose ();
	BOOL IsClosed () { return m_bClosed; }
};

#endif /* ifndef __inc_og_language_service_connectionpipe_h */