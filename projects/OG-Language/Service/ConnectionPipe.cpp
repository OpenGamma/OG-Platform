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

void CConnectionPipe::Init () {
	LOGDEBUG (TEXT ("Connection pipe created"));
	m_cbBuffer = INITIAL_MESSAGE_BUFFER_SIZE;
	m_dwIdleTimeout = 0xFFFFFFFF;
	m_pBuffer = new char[m_cbBuffer];
	if (!m_pBuffer) {
		LOGFATAL (TEXT ("Out of memory"));
	}
	m_bClosed = false;
}

#ifdef _WIN32
CConnectionPipe::CConnectionPipe (HANDLE hPipe) {
	Init ();
	m_hPipe = hPipe;
	m_hEvent = CreateEvent (NULL, FALSE, FALSE, NULL);
	if (!m_hEvent) {
		LOGFATAL (TEXT ("Couldn't create event"));
	}
}
#else
CConnectionPipe::CConnectionPipe () {
	Init ();
	// TODO
}
#endif /* ifdef _WIN32 */

CConnectionPipe::~CConnectionPipe () {
	LOGDEBUG (TEXT ("Destroying connection pipe"));
#ifdef _WIN32
	if (m_hEvent) {
		CloseHandle (m_hEvent);
	}
	if (m_hPipe) {
		CloseHandle (m_hPipe);
	}
#endif
	if (m_pBuffer) {
		free (m_pBuffer);
	}
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
#ifdef _WIN32
	SECURITY_DESCRIPTOR sd;
	InitializeSecurityDescriptor (&sd, SECURITY_DESCRIPTOR_REVISION);
	// TODO [XLS-182] Get a SDDL from the registry for the DACL
	SetSecurityDescriptorDacl (&sd, TRUE, NULL, FALSE);
	SECURITY_ATTRIBUTES sa;
	ZeroMemory (&sa, sizeof (sa));
	sa.nLength = sizeof (sa);
	sa.lpSecurityDescriptor = &sd;
	sa.bInheritHandle = FALSE;
	HANDLE hPipe = CreateNamedPipe (pszPipeName, PIPE_ACCESS_INBOUND | FILE_FLAG_FIRST_PIPE_INSTANCE | FILE_FLAG_OVERLAPPED, PIPE_TYPE_MESSAGE | PIPE_READMODE_MESSAGE | PIPE_WAIT | PIPE_REJECT_REMOTE_CLIENTS, 1, 0, 0, 0, &sa);
	if (hPipe == INVALID_HANDLE_VALUE) {
		LOGWARN (TEXT ("Couldn't create pipe ") << pszPipeName);
		return NULL;
	}
	LOGINFO (TEXT ("Created connection pipe ") << pszPipeName);
	return new CConnectionPipe (hPipe);
#else
	TODO (TEXT ("Create the IPC"));
	return NULL;
#endif
}

PJAVACLIENT_CONNECT CConnectionPipe::ReadMessage () {
	do {
		LOGDEBUG (TEXT ("Waiting for client connection"));
		unsigned long cbRead;
#ifdef _WIN32
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
		DWORD cbReadTotal = 0;
		void *pBuffer = m_pBuffer;
		DWORD cbBuffer = m_cbBuffer;
		while (!ReadFile (m_hPipe, pBuffer, cbBuffer, &cbRead, NULL)) {
			DWORD dwError = GetLastError ();
			if (dwError == ERROR_MORE_DATA) {
				LOGDEBUG (TEXT ("Buffer too small - enlarging"));
				DWORD cbNewSize = m_cbBuffer << 1;
				pBuffer = new BYTE[cbNewSize];
				if (!pBuffer) {
					LOGFATAL (TEXT ("Out of memory"));
					return FALSE;
				}
				memcpy (pBuffer, m_pBuffer, m_cbBuffer);
				delete m_pBuffer;
				m_pBuffer = pBuffer;
				pBuffer = (char*)m_pBuffer + m_cbBuffer;
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
#else
		TODO (TEXT ("Read a message from the IPC"));
		cbRead = 0;
#endif
		if (cbRead < sizeof (JAVACLIENT_CONNECT)) {
			LOGERROR (TEXT ("Invalid message - read ") << cbRead << TEXT (", expected at least ") << sizeof (JAVACLIENT_CONNECT));
			continue;
		}
		PJAVACLIENT_CONNECT pjcc = (PJAVACLIENT_CONNECT)m_pBuffer;
		if (pjcc->cbSize != (fudge_i32)cbRead) {
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
		memcpy (pjcc, m_pBuffer, cbRead);
		return pjcc;
	} while (!m_bClosed);
	LOGINFO (TEXT ("Pipe closed"));
	return NULL;
}

void CConnectionPipe::Close () {
	LOGDEBUG (TEXT ("Signalling pipe close"));
	m_bClosed = true;
#ifdef _WIN32
	SetEvent (m_hEvent);
#endif
}

void CConnectionPipe::LazyClose (unsigned long dwTimeout) {
	if (!dwTimeout) {
		CSettings settings;
		dwTimeout = settings.GetIdleTimeout ();
		if (!dwTimeout) {
			LOGINFO (TEXT ("Immediate idle service termination"));
			Close ();
		} else if (dwTimeout == 0xFFFFFFFF) {
			LOGINFO (TEXT ("Service idle termination suppressed by settings"));
			return;
		}
	}
	LOGINFO (TEXT ("Service is idle, will terminate in ") << dwTimeout << TEXT ("ms"));
	m_dwIdleTimeout = dwTimeout;
#ifdef _WIN32
	SetEvent (m_hEvent);
#endif
}

void CConnectionPipe::CancelLazyClose () {
	if (m_dwIdleTimeout != 0xFFFFFFFF) {
		m_dwIdleTimeout = 0xFFFFFFFF;
		LOGINFO (TEXT ("Service not idle"))
	}
}