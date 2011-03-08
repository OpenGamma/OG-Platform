/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_service_connectionpipe_h
#define __inc_og_language_service_connectionpipe_h

// Implementation of the IPC connection for incoming requests

#include "Public.h"

class CConnectionPipe {
private:
	unsigned long m_dwReadTimeout;
	CNamedPipe *m_poPipe;
	CConnectionPipe (CNamedPipe *poPipe, unsigned long dwReadTimeout);
public:
	~CConnectionPipe ();
	static CConnectionPipe *Create (const TCHAR *pszSuffix = NULL);
	ClientConnect *ReadMessage ();
	const TCHAR *GetName () { return m_poPipe->GetName (); }
	bool Close () { return m_poPipe->Close (); }
	bool LazyClose (unsigned long dwTimeout = 0);
	bool CancelLazyClose () { return m_poPipe->CancelLazyClose (); }
	bool IsClosed () { return m_poPipe->IsClosed (); }
};

#endif /* ifndef __inc_og_language_service_connectionpipe_h */