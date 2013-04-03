/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Logging.h"
#include "NamedPipe.h"
#include "Thread.h"
#include "String.h"

LOGGING (com.opengamma.language.util.NamedPipe);

/// Creates a new named pipe instance.
///
/// @param[in] pipe underlying O/S file handle
/// @param[in] pszName name of the pipe, not NULL
/// @param[in] bServer true if this is a server that can accept connections
/// @param[in] bReader true if this is the read end (or a server that returns read ends, i.e. accepts connections from clients that will write)
CNamedPipe::CNamedPipe (FILE_REFERENCE pipe, const TCHAR *pszName, bool bServer, bool bReader)
: CTimeoutIO (pipe) {
	m_pszName = _tcsdup (pszName);
	m_bServer = bServer;
	m_bReader = bReader;
}

/// Destroys the pipe instance, freeing any resources.
CNamedPipe::~CNamedPipe () {
#ifndef _WIN32
	if (IsServer ()) {
		int file = GetFile ();
		if (file) {
			close (file);
			SetFile (0);
			if (unlink (m_pszName)) {
				LOGWARN (TEXT ("Couldn't delete pipe ") << m_pszName << TEXT (", error ") << GetLastError ());
			} else {
				LOGDEBUG (TEXT ("Deleted pipe ") << m_pszName);
			}
		}
	}
#endif
	delete m_pszName;
}

#ifndef _WIN32

#define TIMEOUT_IO_DEFAULT 1000

/// Set timeout, buffer, and signalling properties on a Unix Domain Socket.
///
/// @param[in] sock socket to configure
/// @return true if the socket was configured, false if there was an error
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

/// Asynchronous thread to unblock the client end of a pipe. Client ends are created by the O/S in
/// a blocked state until the server appears. We require a non-blocking/timeout operation when
/// establishing a real connection, so trick it into thinking the connection exists initially with
/// this dummy server, but then break the pipe immediately. Instead of a "not-connected" we will
/// see a "broken-pipe" error when attempting to use the client for the first time.
class CNamedPipeOpenThread : public CThread {
private:

	/// Name of the pipe
	TCHAR *m_pszName;

	/// File mode to open the pipe in (opposite of the requested client)
	int m_nMode;

protected:

	/// Destroys the thread instance
	~CNamedPipeOpenThread () {
		delete m_pszName;
	}

public:

	/// Creates a new thread instance
	///
	/// @param[in] pszName name of the pipe, not NULL
	/// @param[in] nMode file mode to open the pipe in
	CNamedPipeOpenThread (const TCHAR *pszName, int nMode) : CThread () {
		m_pszName = _tcsdup (pszName);
		m_nMode = nMode;
	}

	/// Runs the thread, opening the pipe and closing it immediately.
	void Run () {
		LOGDEBUG (TEXT ("Opening pipe to unblock exclusive server end"));
		int file = open (m_pszName, m_nMode);
		if (file <= 0) {
			LOGWARN (TEXT ("Couldn't open pipe, error ") << GetLastError ());
		} else {
			LOGDEBUG (TEXT ("Pipe opened"));
			close (file);
			LOGDEBUG (TEXT ("Pipe closed"));
		}
	}

};

#endif /* ifndef _WIN32 */

/// Creates the underlying O/S file handle.
///
/// @param[in] pszName name of the pipe
/// @param[in] bServer true if this is a server end, false if it is a client
/// @param[in] bExclusive false if this is a pipe that can be shared by multiple clients or servers
/// @param[in] bReader true if this is the reading end of a pipe, false if it is the writing end
/// @return the O/S file handle
static CTimeoutIO::FILE_REFERENCE _CreatePipe (const TCHAR *pszName, bool bServer, bool bExclusive, bool bReader) {
#ifdef _WIN32
	HANDLE handle;
	if (bServer) {
		SECURITY_DESCRIPTOR sd;
		SECURITY_ATTRIBUTES sa;
		InitializeSecurityDescriptor (&sd, SECURITY_DESCRIPTOR_REVISION);
		// TODO [PLAT-1119] Get a SDDL from the registry for the DACL (and pass it into this library)
		SetSecurityDescriptorDacl (&sd, TRUE, NULL, FALSE);
		ZeroMemory (&sa, sizeof (sa));
		sa.nLength = sizeof (sa);
		sa.lpSecurityDescriptor = &sd;
		sa.bInheritHandle = FALSE;
		handle = CreateNamedPipe (
			pszName,
			(bReader ? PIPE_ACCESS_INBOUND : PIPE_ACCESS_OUTBOUND) | (bExclusive ? FILE_FLAG_FIRST_PIPE_INSTANCE : 0) | FILE_FLAG_OVERLAPPED,
			(bExclusive ? PIPE_TYPE_BYTE | PIPE_READMODE_BYTE : PIPE_TYPE_MESSAGE | PIPE_READMODE_MESSAGE) | PIPE_WAIT | PIPE_REJECT_REMOTE_CLIENTS,
			bExclusive ? 1 : PIPE_UNLIMITED_INSTANCES,
			0,
			0,
			0,
			&sa);
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
#else /* ifdef _WIN32 */
	if (bExclusive) {
		// Security note: we create the FIFO with world R/W so that the OG-Language service may work with it
		// if it is running as a different user. We always have user and group RW in case it is the same user
		// or same group as us. If the world R/W is bad, then set the pipe location to an area that only the
		// creating user and OG-Language service user have access to.
		mode_t mOriginal = umask (bReader ? 4 : 2);
		if (mkfifo (pszName, 0666)) {
			int ec = GetLastError ();
			umask (mOriginal);
			LOGWARN (TEXT ("Couldn't create pipe ") << pszName << TEXT (", error ") << ec);
			SetLastError (ec);
			return 0;
		}
		umask (mOriginal);
		CThread *poOpener = new CNamedPipeOpenThread (pszName, bReader ? O_WRONLY : O_RDONLY);
		poOpener->Start ();
		CThread::Release (poOpener);
		int file = open (pszName, bReader ? O_RDONLY : O_WRONLY);
		if (file <= 0) {
			int ec = GetLastError ();
			LOGWARN (TEXT ("Couldn't open pipe ") << pszName << TEXT (", error ") << ec);
			if (unlink (pszName)) {
				LOGWARN (TEXT ("Couldn't delete pipe ") << pszName << TEXT (", error ") << GetLastError ());
			}
			SetLastError (ec);
			return 0;
		}
		LOGINFO (TEXT ("Created pipe ") << pszName);
		return file;
	} else {
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
			if (!unlink (pszName)) {
				LOGINFO (TEXT ("Deleted previous instance of ") << pszName);
			}
			mode_t mOriginal = umask (0);
			if (bind (sock, (struct sockaddr*)&addr, sizeof (addr.sun_family) + _tcslen (addr.sun_path))) {
				int ec = GetLastError ();
				umask (mOriginal);
				close (sock);
				LOGWARN (TEXT ("Couldn't open pipe ") << pszName << TEXT (", error ") << ec);
				SetLastError (ec);
				return 0;
			}
			umask (mOriginal);
			if (fcntl (sock, F_SETFL, O_NONBLOCK) || listen (sock, 0)) {
				int ec = GetLastError ();
				close (sock);
				LOGWARN (TEXT ("Couldn't open pipe ") << pszName << TEXT (", error ") << ec);
				SetLastError (ec);
				return 0;
			}
			LOGINFO (TEXT ("Created server Unix Domain Socket ") << pszName);
		} else {
			if (connect (sock, (struct sockaddr*)&addr, sizeof (addr.sun_family) + _tcslen (addr.sun_path))) {
				int ec = GetLastError ();
				close (sock);
				LOGWARN (TEXT ("Couldn't open pipe ") << pszName << TEXT (", error ") << ec);
				switch (ec) {
				case EAGAIN :
					LOGDEBUG (TEXT ("Translating EAGAIN to ENOENT"));
					ec = ENOENT;
					break;
				case ECONNREFUSED :
					LOGDEBUG (TEXT ("Translating ECONNREFUSED to ENOENT"));
					ec = ENOENT;
					break;
				}
				SetLastError (ec);
				return 0;
			}
			LOGDEBUG (TEXT ("Connection accepted, waiting for handshake confirmation"));
			if ((recv (sock, addr.sun_path, 1, 0) != 1) || fcntl (sock, F_SETFL, O_NONBLOCK)) {
				int ec = GetLastError ();
				close (sock);
				LOGWARN (TEXT ("Handshake not received on ") << pszName << TEXT (", error ") << ec);
				switch (ec) {
				case EAGAIN :
					LOGDEBUG (TEXT ("Translating EAGAIN to ENOENT"));
					ec = ENOENT;
					break;
				}
				SetLastError (ec);
				return 0;
			}
			LOGINFO (TEXT ("Connected to Unix Domain Socket ") << pszName);
		}
		return sock;
	}
#endif /* ifdef _WIN32 */
}

/// Creates a reading client on the named pipe.
///
/// @param[in] pszName pipe name
/// @return the named pipe instance, the read methods from CTimeoutIO are valid on it
CNamedPipe *CNamedPipe::ClientRead (const TCHAR *pszName) {
	FILE_REFERENCE hFile = _CreatePipe (pszName, false, false, true);
	return hFile ? new CNamedPipe (hFile, pszName, false, true) : NULL;
}

/// Creates a writing client on the named pipe.
///
/// @param[in] pszName pipe name
/// @return the named pipe instance, the write methods from CTimeoutIO are valid on it
CNamedPipe *CNamedPipe::ClientWrite (const TCHAR *pszName) {
	FILE_REFERENCE hFile = _CreatePipe (pszName, false, false, false);
	return hFile ? new CNamedPipe (hFile, pszName, false, false) : NULL;
}

/// Creates a server to accept incoming requests from writing clients on the named pipe.
///
/// @param[in] pszName pipe name
/// @param[in] bExclusive false if this is a pipe that can be shared by multiple servers
/// @return the named pipe instance, the Accept method is valid on it
CNamedPipe *CNamedPipe::ServerRead (const TCHAR *pszName, bool bExclusive) {
	FILE_REFERENCE hFile = _CreatePipe (pszName, true, bExclusive, true);
	return hFile ? new CNamedPipe (hFile, pszName, true, true) : NULL;
}

/// Creates a server to accept incoming requests from reading clients on the named pipe.
///
/// @param[in] pszName pipe name
/// @param[in] bExclusive false if this is a pipe that can be shared by multiple servers
/// @return the named pipe instance, the Accept method is valid on it
CNamedPipe *CNamedPipe::ServerWrite (const TCHAR *pszName, bool bExclusive) {
	FILE_REFERENCE hFile = _CreatePipe (pszName, true, bExclusive, false);
	return hFile ? new CNamedPipe (hFile, pszName, true, false) : NULL;
}


/// Accepts an incoming connection on a server pipe.
///
/// @param[in] timeout maximum time to wait for a client in milliseconds
/// @return this end of a pipe connected to the client, or NULL if none was available
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
	HANDLE handle = _CreatePipe (GetName (), IsServer (), false, IsReader ());
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
failedOperation:
	struct sockaddr_un addr;
	bool bLazyWait = false;
	unsigned long lLazy = IsLazyClosing ();
	if (lLazy) {
		bLazyWait = true;
		timeout = lLazy;
	}
timeoutOperation:
	int sock;
	if (BeginOverlapped (timeout, true)) {
		socklen_t len = sizeof (addr);
		sock = accept (GetFile (), (struct sockaddr*)&addr, &len);
		EndOverlapped ();
	} else {
		sock = -1;
	}
	if (sock < 0) {
		int ec = GetLastError ();
		if (ec == EINTR) {
			LOGDEBUG (TEXT ("Accept interrupted"));
			lLazy = IsLazyClosing ();
			if (lLazy) {
				if (bLazyWait) {
					LOGINFO (TEXT ("Closing file on idle timeout"));
					Close ();
				}
			}
			if (!IsClosed () && !bLazyWait) {
				bLazyWait = true;
				timeout = lLazy;
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
	if ((send (sock, "!", 1, 0) != 1) || fcntl (sock, F_SETFL, O_NONBLOCK)) {
		int ec = GetLastError ();
		close (sock);
		if (ec == EPIPE) {
			LOGWARN (TEXT ("Client disconnected before receiving handshake"));
			goto failedOperation;
		}
		LOGWARN (TEXT ("Couldn't send handshake message, error ") << ec);
		SetLastError (ec);
		return NULL;
	} else {
		LOGDEBUG (TEXT ("Handshake message written"));
	}
	return new CNamedPipe (sock, GetName (), false, IsReader ());
#endif
}

/// Creates a string prefix for pipe names to be used by unit tests. On Win32 this is OpenGammaLanguageAPI-Test
/// suffixed with the user's name. On Posix, this is OpenGammaLanguageAPI-Test in the user's home folder.
///
/// @return name prefix
const TCHAR *CNamedPipe::GetTestPipePrefix () {
	static TCHAR szPipeTest[256] = { 0 };
	if (!szPipeTest[0]) {
#ifdef _WIN32
		StringCbPrintf (szPipeTest, sizeof (szPipeTest), TEXT ("\\\\.\\pipe\\OpenGammaLanguageAPI-Test"));
		// TODO: append the user's name to the string
#else
		StringCbPrintf (szPipeTest, sizeof (szPipeTest), TEXT ("%s/OpenGammaLanguageAPI-Test"), getenv ("HOME"));
#endif
		LOGDEBUG (TEXT ("Using ") << szPipeTest << TEXT (" for pipe tests"));
	}
	return szPipeTest;
}
