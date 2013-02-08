/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

#define _INTERNAL
#include "ConnectionPipe.h"
#include "Settings.h"
#include <Util/BufferedInput.h>

LOGGING (com.opengamma.language.service.ConnectionPipe);

/// Creates a new IPC connection using the given underlying pipe.
///
/// @param[in] poPipe underlying pipe, never NULL
/// @param[in] dwReadTimeout read timeout on the pipe in milliseconds
CConnectionPipe::CConnectionPipe (CNamedPipe *poPipe, unsigned long dwReadTimeout) {
	LOGDEBUG (TEXT ("Connection pipe created"));
	m_poPipe = poPipe;
	m_dwReadTimeout = dwReadTimeout;
}

/// Destroys the IPC connection.
CConnectionPipe::~CConnectionPipe () {
	LOGDEBUG (TEXT ("Destroying connection pipe"));
	delete m_poPipe;
}

/// Creates a new IPC for incoming connection requests. The pipe name is retrieved from the settings and the
/// given suffix added if supplied.
///
/// @param[in] pszSuffix optional suffix, use NULL to not add a suffix
/// @return the new IPC or NULL if there was a problem
CConnectionPipe *CConnectionPipe::Create (const TCHAR *pszSuffix) {
	TCHAR szPipeName[256];
	CSettings settings;
	const TCHAR *pszPipeName = settings.GetConnectionPipe ();
	if (pszSuffix) {
		StringCbPrintf (szPipeName, sizeof (szPipeName), TEXT ("%s%s"), pszPipeName, pszSuffix);
		pszPipeName = szPipeName;
	}
	LOGDEBUG (TEXT ("Creating connection pipe ") << pszPipeName);
	CNamedPipe *poPipe = CNamedPipe::ServerRead (pszPipeName, false);
	if (!poPipe) {
		LOGWARN (TEXT ("Couldn't create pipe ") << pszPipeName << TEXT (", error ") << GetLastError ());
		return NULL;
	}
	LOGINFO (TEXT ("Created connection pipe ") << pszPipeName);
	return new CConnectionPipe (poPipe, settings.GetConnectionTimeout ());
}

/// Reads the next incoming connection message from the underlying pipe. This method will block until the
/// pipe is closed. When a connection is received, the read timeout applies to receive the connection
/// message. If a message is not received in this time, that connection is closed and the next one is
/// waited for.
///
/// The blocking call will terminate if another thread calls the Close method, or a LazyClose was requested
/// before the call to ReadMessage and the lazy timeout has elapsed.
///
/// @return the new connection or NULL if there was a problem or the pipe was closed
ClientConnect *CConnectionPipe::ReadMessage () {
	do {
		LOGDEBUG (TEXT ("Waiting for client connection"));
		CNamedPipe *poClient = m_poPipe->Accept (0xFFFFFFFF);
		if (!poClient) {
			LOGDEBUG (TEXT ("No client connection"));
			if (m_poPipe->IsClosed ()) {
				LOGINFO (TEXT ("Pipe closed"));
			} else {
				LOGERROR (TEXT ("Couldn't receive client connection, error ") << GetLastError ());
			}
			return NULL;
		}
		LOGDEBUG (TEXT ("Connection accepted - reading from pipe"));
		CBufferedInput oBuffer;
		if (!oBuffer.Read (poClient, 8, m_dwReadTimeout)) { // Fudge headers are 8-bytes long
			int ec = GetLastError ();
			delete poClient;
			if (ec == ETIMEDOUT) {
				LOGWARN (TEXT ("Client connection timed out on read"));
				continue;
			} else {
				LOGERROR (TEXT ("Couldn't read from client connection, error ") << ec);
				return NULL;
			}
		}
		LOGDEBUG (TEXT ("Read ") << oBuffer.GetAvailable () << TEXT (" bytes"));
		FudgeStatus status;
		FudgeMsgHeader header;
		fudge_byte *ptr = (fudge_byte*)oBuffer.GetData ();
		if ((status = FudgeHeader_decodeMsgHeader (&header, ptr, 8)) != FUDGE_OK) {
			delete poClient;
			LOGERROR (TEXT ("Couldn't decode Fudge envelope header, status ") << status);
			SetLastError (EIO_READ);
			return NULL;
		}
		LOGDEBUG (TEXT ("Fudge message header found - reading ") << header.numbytes << TEXT (" byte message"));
		if (!oBuffer.Read (poClient, header.numbytes, m_dwReadTimeout)) {
			int ec = GetLastError ();
			delete poClient;
			if (ec == ETIMEDOUT) {
				LOGWARN (TEXT ("Client connection timed out on read"));
				continue;
			} else {
				LOGERROR (TEXT ("Couldn't read from client connection, error ") << ec);
				return NULL;
			}
		}
		LOGDEBUG (TEXT ("Closing client"));
		delete poClient;
		ptr = (fudge_byte*)oBuffer.GetData ();
		FudgeMsgEnvelope env;
		status = FudgeCodec_decodeMsg (&env, ptr, header.numbytes);
		oBuffer.Discard (header.numbytes);
		if (status != FUDGE_OK) {
			LOGERROR (TEXT ("Couldn't decode Fudge message, status ") << status);
			SetLastError (EIO_READ);
			return NULL;
		}
		ClientConnect *pConnect;
		status = ClientConnect_fromFudgeMsg (FudgeMsgEnvelope_getMessage (env), &pConnect);
		FudgeMsgEnvelope_release (env);
		if (status != FUDGE_OK) {
			LOGERROR (TEXT ("Couldn't decode Fudge message, status ") << status);
			SetLastError (EIO_READ);
			return NULL;
		}
		return pConnect;
	} while (!IsClosed ());
	LOGINFO (TEXT ("Pipe closed"));
	return NULL;
}

/// Closes the IPC connection if it is still idle after the given timeout.
///
/// @param[in] dwTimeout timeout in milliseconds to close after. If zero is given then a timeout
/// is retrieved from the settings
/// @return TRUE if successful, FALSE if there was a problem (e.g. lazy closing not permitted or the
/// connection is already closed
bool CConnectionPipe::LazyClose (unsigned long dwTimeout) {
	if (!dwTimeout) {
		CSettings settings;
		dwTimeout = settings.GetIdleTimeout ();
		if (!dwTimeout) {
			LOGINFO (TEXT ("Immediate idle service termination"));
			return Close ();
		} else if (dwTimeout == 0xFFFFFFFF) {
			LOGINFO (TEXT ("Service idle termination suppressed by settings"));
			return false;
		}
	}
	LOGINFO (TEXT ("Service is idle, will terminate in ") << dwTimeout << TEXT ("ms"));
	return m_poPipe->LazyClose (dwTimeout);
}
