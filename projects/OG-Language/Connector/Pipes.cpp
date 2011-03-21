/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Manages the pipes used by the service to talk to the JVM

#include "Pipes.h"
#include "Settings.h"
#include <Util/String.h>
#include <Util/Error.h>

LOGGING (com.opengamma.language.connector.Pipes);

CClientPipes::CClientPipes (CNamedPipe *poOutput, CNamedPipe *poInput) {
	m_poOutput = poOutput;
	m_poInput = poInput;
	m_bConnected = false;
	m_lLastWrite = GetTickCount ();
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
