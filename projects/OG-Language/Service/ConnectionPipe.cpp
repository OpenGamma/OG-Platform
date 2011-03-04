/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Implementation of the IPC connection for incoming requests

#define _INTERNAL
#include "ConnectionPipe.h"
#include "Settings.h"

LOGGING (com.opengamma.language.service.ConnectionPipe);

CConnectionPipe::CConnectionPipe (CNamedPipe *poPipe, unsigned long dwReadTimeout) {
	LOGDEBUG (TEXT ("Connection pipe created"));
	m_poPipe = poPipe;
	m_dwReadTimeout = dwReadTimeout;
}

CConnectionPipe::~CConnectionPipe () {
	LOGDEBUG (TEXT ("Destroying connection pipe"));
	delete m_poPipe;
}

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

PJAVACLIENT_CONNECT CConnectionPipe::ReadMessage () {
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
		if (!oBuffer.Read (poClient, sizeof (JAVACLIENT_CONNECT), m_dwReadTimeout)) {
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
		PJAVACLIENT_CONNECT pConnect = (PJAVACLIENT_CONNECT)oBuffer.GetData ();
		size_t cbSize = pConnect->cbSize;
		LOGDEBUG (TEXT ("Initial message ") << cbSize << TEXT (" bytes"));
		if (!oBuffer.Read (poClient, cbSize, m_dwReadTimeout)) {
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
		pConnect = (PJAVACLIENT_CONNECT)oBuffer.GetData ();
		if (pConnect->cbChar != sizeof (TCHAR)) {
			LOGERROR (TEXT ("Unicode mismatch with client - expected ") << sizeof (TCHAR) << TEXT (", received ") << pConnect->cbChar);
			// TODO [XLS-173]: Convert the message to the correct character width
			continue;
		}
		pConnect = (PJAVACLIENT_CONNECT)malloc (cbSize);
		if (!pConnect) {
			LOGFATAL (TEXT ("Out of memory"));
			return NULL;
		}
		memcpy (pConnect, oBuffer.GetData (), cbSize);
		oBuffer.Discard (cbSize);
		cbSize = oBuffer.GetAvailable ();
		if (cbSize > 0) {
			LOGWARN (TEXT ("Extra characters found after client packet, ") << cbSize << TEXT (" bytes"));
		}
		return pConnect;
	} while (!IsClosed ());
	LOGINFO (TEXT ("Pipe closed"));
	return NULL;
}

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
