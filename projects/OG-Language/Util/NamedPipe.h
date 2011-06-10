/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_namedpipe_h
#define __inc_og_language_util_namedpipe_h

// Named pipes using either Win32 or POSIX

#include "TimeoutIO.h"
#include "Unicode.h"

/// "Named pipes" using either Win32 or Posix. Note that Win32 named pipes and Posix named pipes
/// have different semantics. This implementation is closer to the Win32 definition. Unix Domain
/// Sockets are a closer match than Posix named pipes.
///
/// Pipes are unidirectional, even though the underlying operating system may support bi-directional
/// pipes.
class CNamedPipe : public CTimeoutIO {
private:

	/// Name of the pipe
	TCHAR *m_pszName;

	/// True if this is a server that can accept client connections, false if this is a client created
	/// as a result of a server receiving a connection, or a client that has connected to a server.
	bool m_bServer;

	/// True if this is a "read" end of a pipe, false if it is the "write" end.
	bool m_bReader;

	CNamedPipe (FILE_REFERENCE pipe, const TCHAR *pszName, bool bServer, bool bReader);
public:
	~CNamedPipe ();
	static CNamedPipe *ClientRead (const TCHAR *pszName);
	static CNamedPipe *ClientWrite (const TCHAR *pszName);
	static CNamedPipe *ServerRead (const TCHAR *pszName, bool bExclusive);
	static CNamedPipe *ServerWrite (const TCHAR *pszName, bool bExclusive);
	CNamedPipe *Accept (unsigned long timeout);
	static const TCHAR *GetTestPipePrefix ();

	/// Returns the name of the pipe.
	///
	/// @return pipe name
	const TCHAR *GetName () const { return m_pszName; }

	/// Tests if this is the server end of a pipe.
	///
	/// @return true if this is a server end, false otherwise
	bool IsServer () const { return m_bServer; }

	/// Tests if this is the client end of a pipe.
	///
	/// @return true if this is a client end, false otherwise
	bool IsClient () const { return !m_bServer; }

	/// Tests if this is a readable end of a pipe. For server ends, the pipe
	/// will accept incoming connections from writing clients (i.e. Accept
	/// will return a reading client).
	/// 
	/// @return true if this is a readable end, false otherwise
	bool IsReader () const { return m_bReader; }

	/// Tests if this is a writeable end of a pipe. For server ends, the pipe
	/// will accept incoming connections from reading clients (i.e. Accept
	/// will return a writing client).
	///
	/// @return true if this is a writeable end, false otherwise
	bool IsWriter () const { return !m_bReader; }

};

#endif /* ifndef __inc_og_language_util_namedpipe_h */
