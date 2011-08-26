/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Pipes.h"
#include "Settings.h"
#include <Util/String.h>
#include <Util/Error.h>

LOGGING (com.opengamma.language.connector.Pipes);

/// Creates a new pipe pair.
///
/// @param[in] poOutput C++ to Java pipe, never NULL
/// @param[in] poInput Java to C++ pipe, never NULL
CClientPipes::CClientPipes (CNamedPipe *poOutput, CNamedPipe *poInput) {
	m_poOutput = poOutput;
	m_poInput = poInput;
	m_bConnected = false;
	m_lLastWrite = GetTickCount ();
}

/// Destroys the pipe pair.
CClientPipes::~CClientPipes () {
	delete m_poOutput;
	delete m_poInput;
}

/// Creates an input pipe, ready to receive a connection.
///
/// @param[in] pszPrefix prefix for the pipe name
/// @param[in] nMaxAttempts number of times to retry the creation operation with different suffixes
/// @param[in] nSuffix numeric suffix, incremented on each retry
/// @return the pipe instance or NULL if there is a problem
CNamedPipe *CClientPipes::CreateInput (const TCHAR *pszPrefix, int nMaxAttempts, int nSuffix) {
	TCHAR szPipeName[256];
	int error = EINVAL;
	while (nMaxAttempts-- > 0) {
		StringCbPrintf (szPipeName, sizeof (szPipeName), TEXT ("%s%08X"), pszPrefix, nSuffix++);
		LOGDEBUG (TEXT ("Creating pipe ") << szPipeName);
		CNamedPipe *poPipe = CNamedPipe::ServerRead (szPipeName, true);
		if (poPipe) return poPipe;
		error = GetLastError ();
		LOGWARN (TEXT ("Error creating pipe ") << szPipeName << TEXT (", error ") << error);
	}
	SetLastError (error);
	return NULL;
}

/// Creates an ouptut pipe, ready to receive a connection.
///
/// @param[in] pszPrefix prefix for the pipe name
/// @param[in] nMaxAttempts number of times to retry the creation operation with different suffixes
/// @param[in] nSuffix numeric suffix, incremented on each retry
/// @return the pipe instance or NULL if there is a problem
CNamedPipe *CClientPipes::CreateOutput (const TCHAR *pszPrefix, int nMaxAttempts, int nSuffix) {
	TCHAR szPipeName[256];
	int error = 0;
	while (nMaxAttempts-- > 0) {
		StringCbPrintf (szPipeName, sizeof (szPipeName), TEXT ("%s%08X"), pszPrefix, nSuffix++);
		LOGDEBUG (TEXT ("Creating pipe ") << szPipeName);
		CNamedPipe *poPipe = CNamedPipe::ServerWrite (szPipeName, true);
		if (poPipe) return poPipe;
		error = GetLastError ();
		LOGWARN (TEXT ("Error creating pipe ") << szPipeName << TEXT (", error ") << error);
	}
	SetLastError (error);
	return NULL;
}

/// Construct a numeric pipe suffix from the system clock. Low order bits from the clock become
/// high order bits in the suffix so that slight increments are likely to give unique values.
///
/// @return a numeric suffix
static long _ClockSuffix () {
	unsigned long n = GetTickCount ();
	return ((n & 0xFF) << 24) | ((n & 0xFF00) << 8) | ((n & 0xFF0000) >> 8) | (n >> 24);
}

/// Creates an input pipe for receiving data from the JVM ready to accept a connection.
///
/// @param[in] pszPrefix pipe name prefix
/// @param[in] nMaxAttempts maximum number of retries to create the pipe
/// @return the pipe instance or NULL if there is a problem
CNamedPipe *CClientPipes::CreateInput (const TCHAR *pszPrefix, int nMaxAttempts) {
	return CreateInput (pszPrefix, nMaxAttempts, _ClockSuffix ());
}

/// Creates an output pipe for writing data to the JVM ready to accept a connection.
///
/// @param[in] pszPrefix pipe name prefix
/// @param[in] nMaxAttempts maximum number of retries to create the pipe
/// @return the pipe instance or NULL if there is a problem
CNamedPipe *CClientPipes::CreateOutput (const TCHAR *pszPrefix, int nMaxAttempts) {
	return CreateOutput (pszPrefix, nMaxAttempts, _ClockSuffix ());
}

/// Creates a pair of pipes ready to accept a connection from the JVM.
///
/// @return the pipe pair instance or NULL if there is a problem
CClientPipes *CClientPipes::Create () {
	CSettings oSettings;
	LOGDEBUG (TEXT ("Creating input pipe"));
	CNamedPipe *poInput = CreateInput (oSettings.GetInputPipePrefix (), oSettings.GetMaxPipeAttempts ());
	if (poInput) {
		LOGDEBUG (TEXT ("Creating output pipe"));
		CNamedPipe *poOutput = CreateOutput (oSettings.GetOutputPipePrefix (), oSettings.GetMaxPipeAttempts ());
		if (poOutput) {
			return new CClientPipes (poOutput, poInput);
		} else {
			LOGERROR (TEXT ("Couldn't create output pipe, error ") << GetLastError ());
			delete poInput;
		}
	} else {
		LOGERROR (TEXT ("Couldn't create input pipe, error ") << GetLastError ());
	}
	return NULL;
}

/// Sends the names of the local pipe pair to the service host so that it can establish a session
/// that will connect back to them.
///
/// @param[in] pszLanguageID language identifier to be passed to the Java stack, never NULL
/// @param[in] poService service channel for communicating with the JVM host process, never NULL
/// @param[in] lTimeout maximum time to wait for writing the connection message in milliseconds
/// @return TRUE if the connection message was written, FALSE if there was a problem
bool CClientPipes::Connect (const TCHAR *pszLanguageID, CNamedPipe *poService, unsigned long lTimeout) {
	assert (!m_bConnected);
	// TODO: the user stuff should be moved into util so it can be used on pipe suffixes in the tests
#ifdef _WIN32
	TCHAR szUserName[256];
	DWORD dwSize = 256;
	if (!GetUserName (szUserName, &dwSize)) {
		LOGWARN (TEXT ("Couldn't lookup current user name, error ") << GetLastError ());
		return false;
	}
	const TCHAR *pszUserName = szUserName;
#else /* ifdef _WIN32 */
	const TCHAR *pszUserName = getenv (TEXT ("USER"));
	if (!pszUserName) {
		LOGWARN (TEXT ("Couldn't lookup current user name"));
		return false;
	}
#endif /* ifdef _WIN32 */
	ClientConnect cc;
	memset (&cc, 0, sizeof (cc));
#ifdef _DEBUG
	cc._debug = FUDGE_TRUE;
#endif /* ifdef _DEBUG */
	cc._userName = pszUserName;
	cc._CPPToJavaPipe = m_poOutput->GetName ();
	cc._JavaToCPPPipe = m_poInput->GetName ();
	cc._languageID = pszLanguageID;
	LOGDEBUG (TEXT ("Writing connection message"));
	FudgeMsg msg;
	if (ClientConnect_toFudgeMsg (&cc, &msg) != FUDGE_OK) {
		LOGWARN (TEXT ("Couldn't create Fudge message"));
		return false;
	}
	FudgeMsgEnvelope env;
	if (FudgeMsgEnvelope_create (&env, 0, 0, 0, msg) != FUDGE_OK) {
		LOGWARN (TEXT ("Couldn't create Fudge message envelope"));
		FudgeMsg_release (msg);
		return false;
	}
	fudge_byte *ptrBuffer;
	fudge_i32 cbBuffer;
	FudgeStatus status = FudgeCodec_encodeMsg (env, &ptrBuffer, &cbBuffer);
	FudgeMsgEnvelope_release (env);
	FudgeMsg_release (msg);
	if (status != FUDGE_OK) {
		LOGWARN (TEXT ("Couldn't encode Fudge message"));
		FudgeMsgEnvelope_release (env);
		return false;
	}
	m_bConnected = poService->Write (ptrBuffer, cbBuffer, lTimeout) == (size_t)cbBuffer;
	free (ptrBuffer);
	if (m_bConnected) {
		LOGINFO (TEXT ("Connected to JVM"));
	} else {
		LOGWARN (TEXT ("Couldn't write connection message, error ") << GetLastError ());
	}
	return m_bConnected;
}

/// Writes data to the JVM. The Java client stack expects the pipe to deliver a sequence
/// of Fudge encoded messages.
///
/// @param[in] ptrBuffer buffer containing data to write, never NULL
/// @param[in] cbBuffer number of bytes to write
/// @param[in] lTimeout maximum time to try the write for in milliseconds
/// @return TRUE if all of the data was written, FALSE if there was a problem
bool CClientPipes::Write (void *ptrBuffer, size_t cbBuffer, unsigned long lTimeout) {
	do {
		LOGDEBUG (TEXT ("Writing ") << cbBuffer << TEXT (" bytes"));
		size_t cbWritten = m_poOutput->Write (ptrBuffer, cbBuffer, lTimeout);
		if (cbWritten > 0) {
			if (cbWritten < cbBuffer) {
				cbBuffer -= cbWritten;
				ptrBuffer = (char*)ptrBuffer + cbWritten;
			} else {
				if (!m_poOutput->Flush ()) {
					LOGERROR (TEXT ("Couldn't flush output buffer, error ") << GetLastError ());
				}
				m_lLastWrite = GetTickCount ();
				return true;
			}
		} else {
			int ec = GetLastError ();
			LOGWARN (TEXT ("Couldn't write ") << cbBuffer << TEXT (" bytes, error ") << ec);
			SetLastError (ec);
			return false;
		}
	} while (true);
}

/// Reads data into the buffer if necessary and returns it. After the data has been handled
/// it should be discared with DiscardInput; the caller must not release or modify the
/// buffer.
///
/// @param[in] cb number of bytes to read; the returned buffer will contain at least this many bytes
/// @param[in] lTimeout maximum time to wait for data if a read is needed
/// @return the buffer or NULL if there is a problem
const void *CClientPipes::PeekInput (size_t cb, unsigned long lTimeout) {
	if (m_oInputBuffer.Read (m_poInput, cb, lTimeout)) {
		return m_oInputBuffer.GetData ();
	} else {
		int ec = GetLastError ();
		if (ec == ETIMEDOUT) {
			LOGDEBUG (TEXT ("Timeout reading input"));
		} else {
			LOGWARN (TEXT ("Error reading input, error ") << ec);
		}
		SetLastError (ec);
		return NULL;
	}
}
