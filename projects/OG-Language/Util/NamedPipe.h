/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
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
	bool m_bServer;
	bool m_bReader;
	CNamedPipe (FILE_REFERENCE pipe, const TCHAR *pszName, bool bServer, bool bReader);
protected:
#ifndef _WIN32
	bool SetTimeout (unsigned long timeout);
#endif
public:
	~CNamedPipe ();
	static CNamedPipe *ClientRead (const TCHAR *pszName);
	static CNamedPipe *ClientWrite (const TCHAR *pszName);
	static CNamedPipe *ServerRead (const TCHAR *pszName, bool bExclusive);
	static CNamedPipe *ServerWrite (const TCHAR *pszName, bool bExclusive);
	const TCHAR *GetName () { return m_pszName; }
	CNamedPipe *Accept (unsigned long timeout);
	bool IsServer () { return m_bServer; }
	bool IsClient () { return !m_bServer; }
	bool IsReader () { return m_bReader; }
	bool IsWriter () { return !m_bReader; }
	static const TCHAR *GetTestPipePrefix ();
};

#endif /* ifndef __inc_og_language_util_namedpipe_h */
