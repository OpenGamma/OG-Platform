/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_pipes_h
#define __inc_og_language_connector_pipes_h

// Manages the pipes used by the service to talk to the JVM

class CClientPipes {
private:
	CNamedPipe *m_poOutput;
	CNamedPipe *m_poInput;
	CClientPipes (CNamedPipe *poOutput, CNamedPipe *poInput);
public:
	static CNamedPipe *CreateInput (const TCHAR *pszPrefix, int nMaxAttempts, int nSuffix);
	static CNamedPipe *CreateOutput (const TCHAR *pszPrefix, int nMaxAttempts, int nSuffix);
	static CNamedPipe *CreateInput (const TCHAR *pszPrefix, int nMaxAttempts);
	static CNamedPipe *CreateOutput (const TCHAR *pszPrefix, int nMaxAttempts);
	static CClientPipes *Create ();
	~CClientPipes ();
	bool Connect (CNamedPipe *poService, unsigned long lTimeout);
};

#endif /* ifndef __inc_og_language_connector_pipes_h */