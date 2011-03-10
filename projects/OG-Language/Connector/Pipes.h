/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_pipes_h
#define __inc_og_language_connector_pipes_h

// Manages the pipes used by the service to talk to the JVM

#include <Util/BufferedInput.h>
#include <Util/NamedPipe.h>

class CClientPipes {
private:
	CNamedPipe *m_poOutput;
	CNamedPipe *m_poInput;
	CBufferedInput m_oInputBuffer;
	long m_lLastWrite;
	bool m_bConnected;
	CClientPipes (CNamedPipe *poOutput, CNamedPipe *poInput);
public:
	static CNamedPipe *CreateInput (const TCHAR *pszPrefix, int nMaxAttempts, int nSuffix);
	static CNamedPipe *CreateOutput (const TCHAR *pszPrefix, int nMaxAttempts, int nSuffix);
	static CNamedPipe *CreateInput (const TCHAR *pszPrefix, int nMaxAttempts);
	static CNamedPipe *CreateOutput (const TCHAR *pszPrefix, int nMaxAttempts);
	static CClientPipes *Create ();
	~CClientPipes ();
	bool Connect (const TCHAR *pszLanguageID, CNamedPipe *poService, unsigned long lTimeout);
	bool Write (void *ptrBuffer, size_t cbBuffer, unsigned long lTimeout);
	void *PeekInput (size_t cb, unsigned long lTimeout);
	void DiscardInput (size_t cb) { m_oInputBuffer.Discard (cb); }
	void Disconnected () { m_bConnected = false; }
	bool IsConnected () { return m_bConnected; }
	unsigned long GetLastWrite () { return m_lLastWrite; }
};

#endif /* ifndef __inc_og_language_connector_pipes_h */
