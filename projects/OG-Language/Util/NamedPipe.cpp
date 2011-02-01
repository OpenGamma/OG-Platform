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

CNamedPipe::CNamedPipe (FILE_REFERENCE pipe, const TCHAR *pszName)
: CTimeoutIO (pipe) {
	m_pszName = _tcsdup (pszName);
}

CNamedPipe::~CNamedPipe () {
	delete m_pszName;
}

CNamedPipe *CNamedPipe::ClientRead (const TCHAR *pszName) {
	TODO (__FUNCTION__);
	return NULL;
}

CNamedPipe *CNamedPipe::ClientWrite (const TCHAR *pszName) {
	TODO (__FUNCTION__);
	return NULL;
}

/*
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
*/

CNamedPipe *CNamedPipe::ServerRead (const TCHAR *pszName) {
	TODO (__FUNCTION__);
	return NULL;
}

CNamedPipe *CNamedPipe::ServerWrite (const TCHAR *pszName) {
	TODO (__FUNCTION__);
	return NULL;
}

CNamedPipe *CNamedPipe::Accept (unsigned long timeout) {
	TODO (__FUNCTION__);
	return NULL;
}
