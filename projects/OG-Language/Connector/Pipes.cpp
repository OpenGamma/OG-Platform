/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Manages the pipes used by the service to talk to the JVM

#include "Pipes.h"
#include "Settings.h"

LOGGING (com.opengamma.language.connector.Pipes);

CClientPipes::CClientPipes (CNamedPipe *poOutput, CNamedPipe *poInput) {
	m_poOutput = poOutput;
	m_poInput = poInput;
	m_bConnected = false;
}

CClientPipes::~CClientPipes () {
	delete m_poOutput;
	delete m_poInput;
}

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

static long _ClockSuffix () {
	unsigned long n = GetTickCount ();
	return ((n & 0xFF) << 24) | ((n & 0xFF00) << 8) | ((n & 0xFF0000) >> 8) | (n >> 24);
}

CNamedPipe *CClientPipes::CreateInput (const TCHAR *pszPrefix, int nMaxAttempts) {
	return CreateInput (pszPrefix, nMaxAttempts, _ClockSuffix ());
}

CNamedPipe *CClientPipes::CreateOutput (const TCHAR *pszPrefix, int nMaxAttempts) {
	return CreateOutput (pszPrefix, nMaxAttempts, _ClockSuffix ());
}

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

bool CClientPipes::Connect (CNamedPipe *poService, unsigned long lTimeout) {
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
	PJAVACLIENT_CONNECT pjcc = JavaClientCreate (pszUserName, m_poOutput->GetName (), m_poInput->GetName ());
	if (!pjcc) {
		LOGWARN (TEXT ("Couldn't create pipe connection message, error ") << GetLastError ());
		return false;
	}
	LOGDEBUG (TEXT ("Writing connection message"));
	m_bConnected = poService->Write (pjcc, pjcc->cbSize, lTimeout) == (size_t)pjcc->cbSize;
	free (pjcc);
	if (m_bConnected) {
		LOGINFO (TEXT ("Connected to JVM"));
	} else {
		LOGWARN (TEXT ("Couldn't write connection message, error ") << GetLastError ());
	}
	return m_bConnected;
}

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
				TODO (TEXT ("Store the last send time"));
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

void *CClientPipes::PeekInput (size_t cb, unsigned long lTimeout) {
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
