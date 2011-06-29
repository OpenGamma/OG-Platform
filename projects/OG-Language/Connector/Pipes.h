/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_pipes_h
#define __inc_og_language_connector_pipes_h

#include <Util/BufferedInput.h>
#include <Util/NamedPipe.h>

/// Composes a pair of unidirectional pipes to the Java client stack for bi-directional
/// communication.
class CClientPipes {
private:

	/// Pipe for sending from C++ to the JVM
	CNamedPipe *m_poOutput;

	//// Pipe for reading into C++ from the JVM
	CNamedPipe *m_poInput;

	/// Input buffer for incoming data
	CBufferedInput m_oInputBuffer;

	/// Timestamp of the last call to write on m_poOutput
	long m_lLastWrite;

	/// Connection indicator flag - TRUE if the pipes are valid, FALSE if not connected
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
	const void *PeekInput (size_t cb, unsigned long lTimeout);

	/// Discard data from the input buffer
	///
	/// @param[in] cb number of bytes to discard
	void DiscardInput (size_t cb) { m_oInputBuffer.Discard (cb); }

	/// Clears the connection status.
	void Disconnected () { m_bConnected = false; }

	/// Tests if the connection is valid.
	///
	/// @return TRUE if the pipes are connected, FALSE otherwise
	bool IsConnected () const { return m_bConnected; }

	/// Returns the timestamp of the last write operation sending data to the JVM
	///
	/// @return timestamp of the last write operation
	unsigned long GetLastWrite () const { return m_lLastWrite; }
};

#endif /* ifndef __inc_og_language_connector_pipes_h */
