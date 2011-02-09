/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Named pipes using either Win32 or POSIX
// Note that Unix and Windows "named pipes" have different semantics, so we use "Unix Domain Sockets" instead.

#include "Logging.h"
#include "NamedPipe.h"
#include "Thread.h"
#include "String.h"

LOGGING (com.opengamma.language.util.NamedPipe);

CNamedPipe::CNamedPipe (FILE_REFERENCE pipe, const TCHAR *pszName, bool bServer, bool bReader)
: CTimeoutIO (pipe) {
	m_pszName = _tcsdup (pszName);
	m_bServer = bServer;
	m_bReader = bReader;
}

CNamedPipe::~CNamedPipe () {
#ifndef _WIN32
	if (IsServer ()) {
		int file = GetFile ();
		if (file) {
			close (GetFile ());
			SetFile (0);
			if (PosixLastError (unlink (m_pszName))) {
				LOGDEBUG (TEXT ("Deleted pipe ") << m_pszName);
			} else {
				LOGWARN (TEXT ("Couldn't delete pipe ") << m_pszName << TEXT (", error ") << GetLastError ());
			}
		}
	}
#endif
	delete m_pszName;
}

#ifndef _WIN32
static bool _SetDefaultSocketOptions (int sock) {
	timeval tv;
	tv.tv_sec = TIMEOUT_IO_DEFAULT / 1000;
	tv.tv_usec = (TIMEOUT_IO_DEFAULT % 1000) * 1000;
	if (setsockopt (sock, SOL_SOCKET, SO_RCVTIMEO, &tv, sizeof (tv))) {
		int ec = GetLastError ();
		close (sock);
		LOGWARN (TEXT ("Couldn't set initial receive timeout, error ") << ec);
		SetLastError (ec);
		return false;
	}
	if (setsockopt (sock, SOL_SOCKET, SO_SNDTIMEO, &tv, sizeof (tv))) {
		int ec = GetLastError ();
		close (sock);
		LOGWARN (TEXT ("Couldn't set inital send timeout, error ") << ec);
		SetLastError (ec);
		return false;
	}
	// TODO: we need a small buffer for the tests, but a production system would be happy with a larger one
	//       therefore, make this configurable
	int i = 4096;
	if (setsockopt (sock, SOL_SOCKET, SO_SNDBUF, &i, sizeof (int))) {
		int ec = GetLastError ();
		close (sock);
		LOGWARN (TEXT ("Couldn't set send buffer size, error ") << ec);
		SetLastError (ec);
		return false;
	}
#ifdef SO_NOSIGPIPE
	i = 1;
	if (setsockopt (sock, SOL_SOCKET, SO_NOSIGPIPE, &i, sizeof (int))) {
		int ec = GetLastError ();
		close (sock);
		LOGWARN (TEXT ("Couldn't disable SIG_PIPE, error ") << ec);
		SetLastError (ec);
		return false;
	}
#endif /* ifdef SO_NOSIGPIPE */
	return true;
}
#endif /* ifndef _WIN32 */

static FILE_REFERENCE _CreatePipe (const TCHAR *pszName, bool bServer, bool bReader) {
#ifdef _WIN32
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
		// TODO: share or exclusive? We want a 1:1 on the pipe we've connected to - the server will open another for new clients
		handle = CreateFile (pszName, bReader ? GENERIC_READ : GENERIC_WRITE, 0/* bReader ? FILE_SHARE_READ : FILE_SHARE_WRITE */, NULL, OPEN_EXISTING, FILE_FLAG_OVERLAPPED, NULL);
	}
	if (handle == INVALID_HANDLE_VALUE) {
		DWORD dwError = GetLastError ();
		LOGWARN (TEXT ("Couldn't create pipe ") << pszName << TEXT(", error ") << dwError);
		SetLastError (dwError);
		return NULL;
	}
	LOGINFO (TEXT ("Created pipe ") << pszName);
	return handle;
#else
	int sock;
	sock = socket (AF_UNIX, SOCK_STREAM, 0);
	if (sock < 0) {
		int ec = GetLastError ();
		LOGWARN (TEXT ("Couldn't open pipe ") << pszName << TEXT (", error ") << ec);
		SetLastError (ec);
		return 0;
	}
	if (!_SetDefaultSocketOptions (sock)) {
		int ec = GetLastError ();
		close (sock);
		LOGWARN (TEXT ("Couldn't set default options ") << pszName << TEXT (", error ") << ec);
		SetLastError (ec);
		return 0;
	}
	struct sockaddr_un addr;
	addr.sun_family = AF_UNIX;
	StringCbPrintf (addr.sun_path, sizeof (addr.sun_path), TEXT ("%s"), pszName);
	if (bServer) {
		unlink (pszName);
		if (bind (sock, (struct sockaddr*)&addr, sizeof (addr.sun_family) + _tcslen (addr.sun_path))) {
			int ec = GetLastError ();
			close (sock);
			LOGWARN (TEXT ("Couldn't open pipe ") << pszName << TEXT (", error ") << ec);
			SetLastError (ec);
			return 0;
		}
		if (listen (sock, 0)) {
			int ec = GetLastError ();
			close (sock);
			LOGWARN (TEXT ("Couldn't open pipe ") << pszName << TEXT (", error ") << ec);
			SetLastError (ec);
			return 0;
		}
	} else {
		if (connect (sock, (struct sockaddr*)&addr, sizeof (addr.sun_family) + _tcslen (addr.sun_path))) {
			int ec = GetLastError ();
			close (sock);
			LOGWARN (TEXT ("Couldn't open pipe ") << pszName << TEXT (", error ") << ec);
			switch (ec) {
			case ECONNREFUSED :
				LOGDEBUG (TEXT ("Translating ECONNREFUSED to ENOENT"));
				ec = ENOENT;
				break;
			}
			SetLastError (ec);
			return 0;
		}
		LOGDEBUG (TEXT ("Connection accepted, waiting for handshake confirmation"));
		if (recv (sock, addr.sun_path, 1, 0) != 1) {
			int ec = GetLastError ();
			close (sock);
			LOGWARN (TEXT ("Handshake not received on ") << pszName << TEXT (", error ") << ec);
			SetLastError (ec);
			return 0;
		}
	}
	return sock;
#endif
}

#ifndef _WIN32
bool CNamedPipe::SetTimeout (unsigned long timeout) {
	if (!CTimeoutIO::SetTimeout (timeout)) return false;
	timeval tv;
	tv.tv_sec = timeout / 1000;
	tv.tv_usec = (timeout % 1000) * 1000;
	if (setsockopt (GetFile (), SOL_SOCKET, SO_RCVTIMEO, &tv, sizeof (tv))) {
		int ec = GetLastError ();
		LOGWARN (TEXT ("Couldn't set receive timeout on pipe, error ") << ec);
		SetLastError (ec);
		return 0;
	}
	if (setsockopt (GetFile (), SOL_SOCKET, SO_SNDTIMEO, &tv, sizeof (tv))) {
		int ec = GetLastError ();
		LOGWARN (TEXT ("Couldn't set send timeout on pipe, error ") << ec);
		SetLastError (ec);
		return 0;
	}
	LOGDEBUG (TEXT ("Timeout on pipe set to ") << timeout << TEXT ("ms"));
	return true;
}
#endif /* ifndef _WIN32 */

CNamedPipe *CNamedPipe::ClientRead (const TCHAR *pszName) {
	FILE_REFERENCE hFile = _CreatePipe (pszName, false, true);
	return hFile ? new CNamedPipe (hFile, pszName, false, true) : NULL;
}

CNamedPipe *CNamedPipe::ClientWrite (const TCHAR *pszName) {
	FILE_REFERENCE hFile = _CreatePipe (pszName, false, false);
	return hFile ? new CNamedPipe (hFile, pszName, false, false) : NULL;
}

CNamedPipe *CNamedPipe::ServerRead (const TCHAR *pszName) {
	FILE_REFERENCE hFile = _CreatePipe (pszName, true, true);
	return hFile ? new CNamedPipe (hFile, pszName, true, true) : NULL;
}

CNamedPipe *CNamedPipe::ServerWrite (const TCHAR *pszName) {
	FILE_REFERENCE hFile = _CreatePipe (pszName, true, false);
	return hFile ? new CNamedPipe (hFile, pszName, true, false) : NULL;
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
		DWORD dwError = GetLastError ();
		LOGWARN (TEXT ("Couldn't create replacement pipe for server, error ") << dwError);
		SetLastError (dwError);
		return NULL;
	}
	CNamedPipe *poClient = new CNamedPipe (GetFile (), GetName (), false, IsReader ());
	SetFile (handle);
	return poClient;
#else
	struct sockaddr_un addr;
	bool bLazyWait = false;
	if (IsLazyClosing ()) {
		bLazyWait = true;
		timeout = IsLazyClosing ();
	}
timeoutOperation:
	SetTimeout (timeout);
	socklen_t len = sizeof (addr);
	LOGDEBUG (TEXT ("Before call to accept"));
	int sock = accept (GetFile (), (struct sockaddr*)&addr, &len);
	LOGDEBUG (TEXT ("After call to accept"));
	CancelTimeout ();
	if (sock < 0) {
		int ec = GetLastError ();
		if (ec == EINTR) {
			LOGDEBUG (TEXT ("Accept interrupted"));
			if (IsLazyClosing ()) {
				if (bLazyWait) {
					LOGINFO (TEXT ("Closing file on idle timeout"));
					Close ();
				}
			}
			if (!IsClosed () && !bLazyWait) {
				bLazyWait = true;
				timeout = IsLazyClosing ();
				LOGDEBUG (TEXT ("Resuming operation with idle timeout of ") << timeout << TEXT ("ms"));
				goto timeoutOperation;
			}
		}
		LOGWARN (TEXT ("Couldn't accept client, error ") << ec);
		SetLastError (ec);
		return NULL;
	}
	LOGINFO (TEXT ("Connection accepted on ") << GetName ());
	if (!_SetDefaultSocketOptions (sock)) {
		int ec = GetLastError ();
		close (sock);
		LOGWARN (TEXT ("Couldn't set default socket options, error ") << ec);
		SetLastError (ec);
		return NULL;
	}
	if (send (sock, "!", 1, 0) != 1) {
		int ec = GetLastError ();
		close (sock);
		LOGWARN (TEXT ("Couldn't send handshake message, error ") << ec);
		SetLastError (ec);
		return NULL;
	} else {
		LOGDEBUG (TEXT ("Handshake message written"));
	}
	return new CNamedPipe (sock, GetName (), false, IsReader ());
#endif
}
