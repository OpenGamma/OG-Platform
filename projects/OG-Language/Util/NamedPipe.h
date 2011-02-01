/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_namedpipe_h
#define __inc_og_language_util_namedpipe_h

// Named pipes using either Win32 or POSIX

#include "TimeoutIO.h"
#include "Unicode.h"

class CNamedPipe : public CTimeoutIO {
private:
	TCHAR *m_pszName;
	CNamedPipe (FILE_REFERENCE pipe, const TCHAR *pszName);
public:
	~CNamedPipe ();
	static CNamedPipe *ClientRead (const TCHAR *pszName);
	static CNamedPipe *ClientWrite (const TCHAR *pszName);
	static CNamedPipe *ServerRead (const TCHAR *pszName);
	static CNamedPipe *ServerWrite (const TCHAR *pszName);
	const TCHAR *GetName () { return m_pszName; }
	CNamedPipe *Accept (unsigned long timeout);
};

#endif /* ifndef __inc_og_language_util_namedpipe_h */