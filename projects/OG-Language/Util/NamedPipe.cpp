/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Named pipes using either Win32 or POSIX

#include "Logging.h"
#include "NamedPipe.h"

LOGGING (com.opengamma.language.util.NamedPipe);

CNamedPipe::CNamedPipe (FILE_REFERENCE pipe, const TCHAR *pszName, bool bServer, bool bReader)
: CTimeoutIO (pipe) {
	m_pszName = _tcsdup (pszName);
	m_bServer = bServer;
	m_bReader = bReader;
}

CNamedPipe::~CNamedPipe () {
	delete m_pszName;
}

#ifdef _WIN32
static HANDLE _CreatePipe (const TCHAR *pszName, bool bServer, bool bReader) {
	HANDLE handle;
	if (bServer) {
		SECURITY_DESCRIPTOR sd;
		SECURITY_ATTRIBUTES sa;
		InitializeSecurityDescriptor (&sd, SECURITY_DESCRIPTOR_REVISION);
		// TODO [XLS-182] Get a SDDL from the registry for the DACL (and pass it into this library)
		SetSecurityDescriptorDacl (&sd, TRUE, NULL, FALSE);
		ZeroMemory (&sa, sizeof (sa));
		sa.nLength = sizeof (sa);
		sa.lpSecurityDescriptor = &sd;
		sa.bInheritHandle = FALSE;
		handle = CreateNamedPipe (pszName, (bReader ? PIPE_ACCESS_INBOUND : PIPE_ACCESS_OUTBOUND) | FILE_FLAG_OVERLAPPED, PIPE_TYPE_MESSAGE | PIPE_READMODE_MESSAGE | PIPE_WAIT | PIPE_REJECT_REMOTE_CLIENTS, PIPE_UNLIMITED_INSTANCES, 0, 0, 0, &sa);
	} else {
		handle = CreateFile (pszName, bReader ? GENERIC_READ : GENERIC_WRITE, bReader ? FILE_SHARE_READ : FILE_SHARE_WRITE, NULL, OPEN_EXISTING, FILE_FLAG_OVERLAPPED, NULL);
	}
	if (handle == INVALID_HANDLE_VALUE) {
		DWORD dwError = GetLastError ();
		LOGWARN (TEXT ("Couldn't create pipe ") << pszName << TEXT(", error ") << dwError);
		SetLastError (dwError);
		return NULL;
	}
	LOGINFO (TEXT ("Created pipe ") << pszName);
	return handle;
}
#endif /* ifdef _WIN32 */

CNamedPipe *CNamedPipe::ClientRead (const TCHAR *pszName) {
#ifdef _WIN32
	HANDLE hFile = _CreatePipe (pszName, false, true);
	return hFile ? new CNamedPipe (hFile, pszName, false, true) : NULL;
#else
	TODO (TEXT ("Connect to the IPC"));
	return NULL;
#endif
}

CNamedPipe *CNamedPipe::ClientWrite (const TCHAR *pszName) {
#ifdef _WIN32
	HANDLE hFile = _CreatePipe (pszName, false, false);
	return hFile ? new CNamedPipe (hFile, pszName, false, false) : NULL;
#else
	TODO (TEXT ("Connect to the IPC"));
	return NULL;
#endif
}

CNamedPipe *CNamedPipe::ServerRead (const TCHAR *pszName) {
#ifdef _WIN32
	HANDLE hFile = _CreatePipe (pszName, true, true);
	return hFile ? new CNamedPipe (hFile, pszName, true, true) : NULL;
#else
	TODO (TEXT ("Create the IPC"));
	return NULL;
#endif
}

CNamedPipe *CNamedPipe::ServerWrite (const TCHAR *pszName) {
#ifdef _WIN32
	HANDLE hFile = _CreatePipe (pszName, true, false);
	return hFile ? new CNamedPipe (hFile, pszName, true, false) : NULL;
#else
	TODO (TEXT ("Create the IPC"));
	return NULL;
#endif
}

CNamedPipe *CNamedPipe::Accept (unsigned long timeout) {
	assert (IsServer ());
#ifdef _WIN32
	if (!ConnectNamedPipe (GetFile (), GetOverlapped ())) {
		if (GetLastError () != ERROR_PIPE_CONNECTED) {
			if (!WaitOnOverlapped (timeout)) {
				DWORD dwError = GetLastError ();
				LOGWARN (TEXT ("Overlapped result not available, error ") << dwError);
				SetLastError (dwError);
				return NULL;
			}
		}
	}
	HANDLE handle = _CreatePipe (GetName (), IsServer (), IsReader ());
	if (!handle) {
		LOGWARN (TEXT ("Couldn't create replacement pipe for server, error ") << GetLastError ());
		return NULL;
	}
	CNamedPipe *poClient = new CNamedPipe (GetFile (), GetName (), false, IsReader ());
	SetFile (handle);
	return poClient;
#else
	TODO (TEXT ("Accept an IPC connection"));
	return NULL;
#endif
}
