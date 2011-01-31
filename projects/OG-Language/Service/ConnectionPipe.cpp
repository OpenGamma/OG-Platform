/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Implementation of the IPC connection for incoming requests

#include "ConnectionPipe.h"
#include "Settings.h"

LOGGING (com.opengamma.language.service.ConnectionPipe);

#define INITIAL_MESSAGE_BUFFER_SIZE	1024

CConnectionPipe::CConnectionPipe (HANDLE hPipe) {
	LOGDEBUG (TEXT ("Connection pipe created"));
	m_hPipe = hPipe;
	m_cbBuffer = INITIAL_MESSAGE_BUFFER_SIZE;
	m_dwIdleTimeout = INFINITE;
	m_pbBuffer = new BYTE[m_cbBuffer];
	if (!m_pbBuffer) {
		LOGFATAL (TEXT ("Out of memory"));
	}
	m_hEvent = CreateEvent (NULL, FALSE, FALSE, NULL);
	if (!m_hEvent) {
		LOGFATAL (TEXT ("Couldn't create event"));
	}
	m_bClosed = FALSE;
}

CConnectionPipe::~CConnectionPipe () {
	LOGDEBUG (TEXT ("Destroying connection pipe"));
	if (m_hEvent) {
		CloseHandle (m_hEvent);
	}
	if (m_hPipe) {
		CloseHandle (m_hPipe);
	}
	delete m_pbBuffer;
}

CConnectionPipe *CConnectionPipe::Create (PCTSTR pszSuffix) {
	TCHAR szPipeName[MAX_PATH];
	CSettings settings;
	PCTSTR pszPipeName = settings.GetConnectionPipe ();
	LOGDEBUG (TEXT ("Creating connection pipe ") << pszPipeName);
	SECURITY_DESCRIPTOR sd;
	InitializeSecurityDescriptor (&sd, SECURITY_DESCRIPTOR_REVISION);
	// TODO [XLS-182] Get a SDDL from the registry for the DACL
	SetSecurityDescriptorDacl (&sd, TRUE, NULL, FALSE);
	SECURITY_ATTRIBUTES sa;
	ZeroMemory (&sa, sizeof (sa));
	sa.nLength = sizeof (sa);
	sa.lpSecurityDescriptor = &sd;
	sa.bInheritHandle = FALSE;
	if (pszSuffix) {
		StringCbPrintf (szPipeName, sizeof (szPipeName), TEXT ("%s%s"), pszPipeName, pszSuffix);
		pszPipeName = szPipeName;
	}
	HANDLE hPipe = CreateNamedPipe (pszPipeName, PIPE_ACCESS_INBOUND | FILE_FLAG_FIRST_PIPE_INSTANCE | FILE_FLAG_OVERLAPPED, PIPE_TYPE_MESSAGE | PIPE_READMODE_MESSAGE | PIPE_WAIT | PIPE_REJECT_REMOTE_CLIENTS, 1, 0, 0, 0, &sa);
	if (hPipe == INVALID_HANDLE_VALUE) {
		LOGWARN (TEXT ("Couldn't create pipe ") << pszPipeName);
		return NULL;
	}
	LOGINFO (TEXT ("Created connection pipe ") << pszPipeName);
	return new CConnectionPipe (hPipe);
}

PJAVACLIENT_CONNECT CConnectionPipe::ReadMessage () {
	do {
		LOGDEBUG (TEXT ("Waiting for client connection"));
		OVERLAPPED overlap;
		ZeroMemory (&overlap, sizeof (overlap));
		overlap.hEvent = m_hEvent;
		if (!ConnectNamedPipe (m_hPipe, &overlap)) {
			DWORD dwError = GetLastError ();
			switch (dwError) {
			case ERROR_IO_PENDING :
				LOGDEBUG (TEXT ("Waiting for completion event"));
waitAgainOnSignal:
				if (WaitForSingleObject (m_hEvent, m_dwIdleTimeout) == WAIT_TIMEOUT) {
					if (m_bClosed || !HasOverlappedIoCompleted (&overlap)) {
						LOGINFO (TEXT ("Pipe closed on idle timeout"));
						m_bClosed = TRUE;
						CloseHandle (m_hPipe);
						m_hPipe = NULL;
						return NULL;
					}
				} else {
					if (m_bClosed) {
						LOGINFO (TEXT ("Pipe closed"));
						CloseHandle (m_hPipe);
						m_hPipe = NULL;
						return NULL;
					} else {
						if (!HasOverlappedIoCompleted (&overlap)) {
							goto waitAgainOnSignal;
						}
					}
				}
				break;
			case ERROR_PIPE_CONNECTED :
				LOGDEBUG (TEXT ("Already connected"));
				break;
			default :
				LOGERROR (TEXT ("Couldn't connect to pipe, error ") << dwError);
				return NULL;
			}
		}
		LOGDEBUG (TEXT ("Reading from pipe"));
		DWORD cbRead, cbReadTotal = 0;
		PBYTE pbBuffer = m_pbBuffer;
		DWORD cbBuffer = m_cbBuffer;
		while (!ReadFile (m_hPipe, pbBuffer, cbBuffer, &cbRead, NULL)) {
			DWORD dwError = GetLastError ();
			if (dwError == ERROR_MORE_DATA) {
				LOGDEBUG (TEXT ("Buffer too small - enlarging"));
				DWORD cbNewSize = m_cbBuffer << 1;
				pbBuffer = new BYTE[cbNewSize];
				if (!pbBuffer) {
					LOGFATAL (TEXT ("Out of memory"));
					return FALSE;
				}
				memcpy (pbBuffer, m_pbBuffer, m_cbBuffer);
				delete m_pbBuffer;
				m_pbBuffer = pbBuffer;
				pbBuffer = m_pbBuffer + m_cbBuffer;
				cbBuffer = cbNewSize - m_cbBuffer;
				m_cbBuffer = cbNewSize;
				cbReadTotal += cbRead;
			} else {
				LOGWARN (TEXT ("Couldn't read from pipe, error ") << dwError);
				return NULL;
			}
		}
		cbRead += cbReadTotal;
		if (!DisconnectNamedPipe (m_hPipe)) {
			LOGWARN (TEXT ("Couldn't disconnect client from pipe, error ") << GetLastError ());
			// Carry on though
		}
		if (cbRead < sizeof (JAVACLIENT_CONNECT)) {
			LOGERROR (TEXT ("Invalid message - read ") << cbRead << TEXT (", expected at least ") << sizeof (JAVACLIENT_CONNECT));
			continue;
		}
		PJAVACLIENT_CONNECT pjcc = (PJAVACLIENT_CONNECT)m_pbBuffer;
		if (pjcc->cbSize != cbRead) {
			LOGERROR (TEXT ("Invalid message - read ") << cbRead << TEXT (", declared size ") << pjcc->cbSize);
			continue;
		}
		if (pjcc->cbChar != sizeof (TCHAR)) {
			LOGERROR (TEXT ("Unicode mismatch with client - expected ") << sizeof (TCHAR) << TEXT (", received ") << pjcc->cbChar);
			// TODO [XLS-173]: Convert the message to the correct character width
			continue;
		}
		pjcc = (PJAVACLIENT_CONNECT)malloc (cbRead);
		if (!pjcc) {
			LOGFATAL (TEXT ("Out of memory"));
			return NULL;
		}
		memcpy (pjcc, m_pbBuffer, cbRead);
		return pjcc;
	} while (!m_bClosed);
	LOGINFO (TEXT ("Pipe closed"));
	return NULL;
}

void CConnectionPipe::Close () {
	LOGDEBUG (TEXT ("Signalling pipe close"));
	m_bClosed = TRUE;
	SetEvent (m_hEvent);
}

void CConnectionPipe::LazyClose (DWORD dwTimeout) {
	if (!dwTimeout) {
		CSettings settings;
		dwTimeout = settings.GetIdleTimeout ();
		if (!dwTimeout) {
			LOGINFO (TEXT ("Immediate idle service termination"));
			Close ();
		} else if (dwTimeout == INFINITE) {
			LOGINFO (TEXT ("Service idle termination suppressed by settings"));
			return;
		}
	}
	LOGINFO (TEXT ("Service is idle, will terminate in ") << dwTimeout << TEXT ("ms"));
	m_dwIdleTimeout = dwTimeout;
	SetEvent (m_hEvent);
}

void CConnectionPipe::CancelLazyClose () {
	if (m_dwIdleTimeout != INFINITE) {
		m_dwIdleTimeout = INFINITE;
		LOGINFO (TEXT ("Service not idle"))
	}
}