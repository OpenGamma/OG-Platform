/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Util/NamedPipe.cpp

#include "Util/NamedPipe.h"
#include "Util/String.h"
#include "Util/Thread.h"
#include "Util/Error.h"

LOGGING (com.opengamma.language.util.NamedPipeTest);

#define TIMEOUT_PIPE			1000
#define TIMEOUT_HANG			(TIMEOUT_PIPE * 2)
#define TIMEOUT_JOIN			(TIMEOUT_PIPE * 3)
#define TIMEOUT_READWRITE		(TIMEOUT_PIPE / 3)
#define READWRITE_OPERATIONS	(TIMEOUT_HANG / TIMEOUT_READWRITE)

TCHAR *NamedPipeTest_CreatePipeName () {
	TCHAR szPipeTest[256];
#ifdef _WIN32
	StringCbPrintf (szPipeTest, sizeof (szPipeTest), TEXT ("\\\\.\\pipe\\OpenGammaLanguageAPI-Test"));
	// TODO: append the user's name to the string
#else
	StringCbPrintf (szPipeTest, sizeof (szPipeTest), TEXT ("%s/OpenGammaLanguageAPI-Test"), getenv ("HOME"));
#endif
	LOGDEBUG (TEXT ("Using ") << szPipeTest << TEXT (" for pipe tests"));
	return _tcsdup (szPipeTest);
}

static int _ReadAndWrite (CTimeoutIO *poIO, int nOperations, bool bThrottle, const TCHAR *pszLabel) {
	LOGDEBUG (pszLabel << TEXT (" ") << ((nOperations > 0) ? TEXT ("reading") : TEXT ("writing")));
	int nCount = 0;
	while (nOperations > 0) {
		char buffer[16];
		size_t cb = poIO->Read (buffer, sizeof (buffer), TIMEOUT_PIPE);
		LOGDEBUG (pszLabel << TEXT (" read ") << cb << TEXT (" bytes"));
		if (!cb) return nCount;
		nCount++;
		nOperations--;
		Sleep (bThrottle ? TIMEOUT_READWRITE : TIMEOUT_READWRITE / 3);
	}
	while (nOperations < 0) {
		size_t cb = poIO->Write ("Hello world", 11, TIMEOUT_PIPE);
		LOGDEBUG (pszLabel << TEXT (" wrote ") << cb << TEXT (" bytes"));
		if (!cb) return nCount;
		nCount++;
		nOperations++;
		Sleep (bThrottle ? TIMEOUT_READWRITE : TIMEOUT_READWRITE / 3);
	}
	return nCount;
}

#define FAIL_NONE				0
#define FAIL_NO_ACCEPT			1
#define FAIL_NO_CREATE			2
#define FAIL_NO_READWRITE		3
#define FAIL_PARTIAL_READWRITE	4

class CPipeServerThread : public CThread {
private:
	const TCHAR *m_pszPipeName;
	bool m_bRead;
	bool m_bAccept;
	int m_nReadWriteOperations;
	bool m_bThrottle;
	bool m_bRepeat;
	int m_nFailure;
public:
	CNamedPipe * volatile m_poPipe;
	CPipeServerThread (const TCHAR *pszPipeName, bool bRead, bool bAccept, int nReadWriteOperations, bool bThrottle = true) {
		m_pszPipeName = pszPipeName;
		m_bRead = bRead;
		m_bAccept = bAccept;
		m_nReadWriteOperations = bRead ? nReadWriteOperations : -nReadWriteOperations;
		m_bThrottle = bThrottle;
		m_bRepeat = false;
		m_nFailure = FAIL_NONE;
		m_poPipe = NULL;
		ASSERT (Start ());
	}
	void SetRepeat () {
		m_bRepeat = true;
	}
	void Run () {
		LOGDEBUG (TEXT ("Creating server pipe"));
		CNamedPipe *poServer = m_bRead ? CNamedPipe::ServerRead (m_pszPipeName) : CNamedPipe::ServerWrite (m_pszPipeName);
		if (poServer) {
			if (m_bAccept) {
repeatOperation:
				LOGDEBUG (TEXT ("Accepting client connection"));
				m_poPipe = poServer->Accept (TIMEOUT_PIPE);
				if (m_poPipe) {
					if (m_nReadWriteOperations) {
						int rw = _ReadAndWrite (m_poPipe, m_nReadWriteOperations, m_bThrottle, TEXT ("Server"));
						if (rw == 0) {
							LOGDEBUG (TEXT ("Didn't read or write"));
							m_nFailure = FAIL_NO_READWRITE;
						} else if (rw < abs (m_nReadWriteOperations)) {
							LOGDEBUG (TEXT ("Partial read or write - ") << rw << TEXT (", expected ") << abs (m_nReadWriteOperations));
							m_nFailure = FAIL_PARTIAL_READWRITE;
						}
					} else {
						LOGDEBUG (TEXT ("Sleeping"));
						Sleep (TIMEOUT_HANG);
					}
					LOGDEBUG (TEXT ("Deleting client connection object"));
					delete m_poPipe;
					m_poPipe = NULL;
					if (m_bRepeat) {
						LOGDEBUG (TEXT ("Repeating operation"));
						m_bRepeat = false;
						goto repeatOperation;
					}
				} else {
					LOGDEBUG (TEXT ("Didn't accept client"));
					m_nFailure = FAIL_NO_ACCEPT;
				}
			} else {
				LOGDEBUG (TEXT ("Sleeping"));
				Sleep (TIMEOUT_HANG);
			}
			LOGDEBUG (TEXT ("Deleting server object"));
			delete poServer;
		} else {
			LOGDEBUG (TEXT ("Couldn't create pipe"));
			m_nFailure = FAIL_NO_CREATE;
		}
	}
	int GetFailure () {
		return m_nFailure;
	}
};

class CPipeClientThread : public CThread {
private:
	const TCHAR *m_pszPipeName;
	bool m_bRead;
	int m_nReadWriteOperations;
	bool m_bThrottle;
	bool m_bRepeat;
	int m_nFailure;
public:
	CNamedPipe * volatile m_poPipe;
	CPipeClientThread (const TCHAR *pszPipeName, bool bRead, int nReadWriteOperations, bool bThrottle = true) {
		m_pszPipeName = pszPipeName;
		m_bRead = bRead;
		m_nReadWriteOperations = bRead ? nReadWriteOperations : -nReadWriteOperations;
		m_bThrottle = bThrottle;
		m_bRepeat = false;
		m_nFailure = FAIL_NONE;
		m_poPipe = NULL;
		ASSERT (Start ());
	}
	void SetRepeat () {
		m_bRepeat = true;
	}
	void Run () {
repeatOperation:
		LOGDEBUG (TEXT ("Creating client pipe"));
		int nAttempt = 0;
		do {
			if (nAttempt) {
				LOGDEBUG (TEXT ("Retrying client connection"));
				Sleep (TIMEOUT_PIPE / 10);
			}
			m_poPipe = m_bRead ? CNamedPipe::ClientRead (m_pszPipeName) : CNamedPipe::ClientWrite (m_pszPipeName);
		} while ((!m_poPipe) && (NativeGetLastError () == ERROR_FILE_NOT_FOUND) && (++nAttempt < 10));
		if (m_poPipe) {
			if (m_nReadWriteOperations) {
				int rw = _ReadAndWrite (m_poPipe, m_nReadWriteOperations, m_bThrottle, TEXT ("Client"));
				if (rw == 0) {
					LOGDEBUG (TEXT ("Didn't read or write"));
					m_nFailure = FAIL_NO_READWRITE;
				} else if (rw < abs (m_nReadWriteOperations)) {
					LOGDEBUG (TEXT ("Partial read or write - ") << rw << TEXT (", expected ") << abs (m_nReadWriteOperations));
					m_nFailure = FAIL_PARTIAL_READWRITE;
				}
			} else {
				LOGDEBUG ("Sleeping");
				Sleep (TIMEOUT_HANG);
			}
			LOGDEBUG (TEXT ("Deleting client object"));
			delete m_poPipe;
			m_poPipe = NULL;
			if (m_bRepeat) {
				LOGDEBUG (TEXT ("Repeating operation"));
				m_bRepeat = false;
				goto repeatOperation;
			}
		} else {
			LOGDEBUG (TEXT ("Couldn't create pipe, error ") << NativeGetLastError ());
			m_nFailure = FAIL_NO_CREATE;
		}
	}
	int GetFailure () {
		return m_nFailure;
	}
};

class CPipeClosingThread : public CThread {
private:
	CNamedPipe * volatile *m_ppoPipe;
	unsigned long m_lLazyClose;
	bool m_bCancel;
public:
	CPipeClosingThread (CNamedPipe * volatile *ppoPipe, unsigned long lLazyClose, bool bCancel) {
		m_ppoPipe = ppoPipe;
		m_lLazyClose = lLazyClose;
		m_bCancel = bCancel;
		ASSERT (Start ());
	}
	void Run () {
		LOGDEBUG (TEXT ("Sleeping"));
		Sleep (TIMEOUT_HANG / 2);
		CNamedPipe *poPipe = *m_ppoPipe;
		ASSERT (poPipe);
		if (m_lLazyClose) {
			LOGDEBUG (TEXT ("Lazy close"));
			poPipe->LazyClose (m_lLazyClose);
			if (m_bCancel) {
				LOGDEBUG (TEXT ("Sleeping"));
				Sleep (m_lLazyClose / 2);
				LOGDEBUG (TEXT ("Cancelling lazy close"));
				poPipe->CancelLazyClose ();
			}
		} else {
			LOGDEBUG (TEXT ("Close"));
			poPipe->Close ();
		}
	}
};

#define ASSERT_FAILURE(_server_,_client_) \
	if ((poServer->GetFailure () != _server_) || (poClient->GetFailure () != _client_)) { \
		LOGWARN (TEXT ("Expected server=") << _server_ << TEXT (", got=") << poServer->GetFailure ()); \
		LOGWARN (TEXT ("Expected client=") << _client_ << TEXT (", got=") << poClient->GetFailure ()); \
		ASSERT (0); \
	}

static void ClientToServerComplete () {
	TCHAR *pszPipeName = NamedPipeTest_CreatePipeName ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, true, true, 2);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, false, 2);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_NONE, FAIL_NONE);
	CThread::Release (poServer);
	CThread::Release (poClient);
	free (pszPipeName);
}

static void ClientToServerTimeoutConnectServer () {
	TCHAR *pszPipeName = NamedPipeTest_CreatePipeName ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, true, true, 1);
	Sleep (TIMEOUT_PIPE * 2);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poServer->GetFailure () == FAIL_NO_ACCEPT);
	CThread::Release (poServer);
	free (pszPipeName);
}

static void ClientToServerTimeoutConnectClient () {
	TCHAR *pszPipeName = NamedPipeTest_CreatePipeName ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, true, false, 0);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, false, 1);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_NONE, FAIL_NO_READWRITE); // Or Client=FAIL_NO_CREATE if pipe semantics different on non-Win32
	CThread::Release (poServer);
	CThread::Release (poClient);
	free (pszPipeName);
}

static void ClientToServerTimeoutReadServer () {
	TCHAR *pszPipeName = NamedPipeTest_CreatePipeName ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, true, true, 1);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, false, 0);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_NO_READWRITE, FAIL_NONE);
	CThread::Release (poServer);
	CThread::Release (poClient);
	free (pszPipeName);
}

static void ClientToServerTimeoutWriteClient () {
	TCHAR *pszPipeName = NamedPipeTest_CreatePipeName ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, true, true, 0);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, false, 1);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_NONE, FAIL_NO_READWRITE);
	CThread::Release (poServer);
	CThread::Release (poClient);
	free (pszPipeName);
}

static void ServerToClientComplete () {
	TCHAR *pszPipeName = NamedPipeTest_CreatePipeName ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, false, true, 2);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, true, 2);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_NONE, FAIL_NONE);
	CThread::Release (poServer);
	CThread::Release (poClient);
	free (pszPipeName);
}

static void ServerToClientTimeoutConnectServer () {
	TCHAR *pszPipeName = NamedPipeTest_CreatePipeName ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, false, true, 1);
	Sleep (TIMEOUT_PIPE * 2);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poServer->GetFailure () == FAIL_NO_ACCEPT);
	CThread::Release (poServer);
	free (pszPipeName);
}

static void ServerToClientTimeoutConnectClient () {
	TCHAR *pszPipeName = NamedPipeTest_CreatePipeName ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, false, false, 0);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, true, 1);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_NONE, FAIL_NO_READWRITE); // Or Client=FAIL_NO_CREATE if pipe semantics different on non-Win32
	CThread::Release (poServer);
	CThread::Release (poClient);
	free (pszPipeName);
}

static void ServerToClientTimeoutReadClient () {
	TCHAR *pszPipeName = NamedPipeTest_CreatePipeName ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, false, true, 0);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, true, 1);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_NONE, FAIL_NO_READWRITE);
	CThread::Release (poServer);
	CThread::Release (poClient);
	free (pszPipeName);
}

static void ServerToClientTimeoutWriteServer () {
	TCHAR *pszPipeName = NamedPipeTest_CreatePipeName ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, false, true, 1);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, true, 0);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_NO_READWRITE, FAIL_NONE);
	CThread::Release (poServer);
	CThread::Release (poClient);
	free (pszPipeName);
}

static void TestClose (bool bClientToServer, bool bCloseServer) {
	TCHAR *pszPipeName = NamedPipeTest_CreatePipeName ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, bClientToServer, true, READWRITE_OPERATIONS);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, !bClientToServer, READWRITE_OPERATIONS);
	CPipeClosingThread *poCloser = new CPipeClosingThread (bCloseServer ? &poServer->m_poPipe : &poClient->m_poPipe, 0, false);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT (poCloser->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_PARTIAL_READWRITE, FAIL_PARTIAL_READWRITE);
	CThread::Release (poServer);
	CThread::Release (poClient);
	CThread::Release (poCloser);
	free (pszPipeName);
}

static void ClientToServerCloseServer () {
	TestClose (true, true);
}

static void ClientToServerCloseClient () {
	TestClose (true, false);
}

static void ServerToClientCloseServer () {
	TestClose (false, true);
}

static void ServerToClientCloseClient () {
	TestClose (false, false);
}

static void TestLazyClose (bool bClientToServer, bool bCloseServer) {
	TCHAR *pszPipeName = NamedPipeTest_CreatePipeName ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, bClientToServer, true, READWRITE_OPERATIONS, !bCloseServer);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, !bClientToServer, READWRITE_OPERATIONS, bCloseServer);
	CPipeClosingThread *poCloser = new CPipeClosingThread (bCloseServer ? &poServer->m_poPipe : &poClient->m_poPipe, TIMEOUT_READWRITE * 2, false);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT (poCloser->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_NONE, FAIL_NONE);
	CThread::Release (poServer);
	CThread::Release (poClient);
	CThread::Release (poCloser);
	poServer = new CPipeServerThread (pszPipeName, bClientToServer, true, READWRITE_OPERATIONS, !bCloseServer);
	poClient = new CPipeClientThread (pszPipeName, !bClientToServer, READWRITE_OPERATIONS, bCloseServer);
	poCloser = new CPipeClosingThread (bCloseServer ? &poServer->m_poPipe : &poClient->m_poPipe, TIMEOUT_READWRITE / 2, false);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT (poCloser->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_PARTIAL_READWRITE, FAIL_PARTIAL_READWRITE);
	CThread::Release (poServer);
	CThread::Release (poClient);
	CThread::Release (poCloser);
	free (pszPipeName);
}

static void ClientToServerLazyCloseServer () {
	TestLazyClose (true, true);
}

static void ClientToServerLazyCloseClient () {
	TestLazyClose (true, false);
}

static void ServerToClientLazyCloseServer () {
	TestLazyClose (false, true);
}

static void ServerToClientLazyCloseClient () {
	TestLazyClose (false, false);
}

static void TestCancelLazyClose (bool bClientToServer, bool bCloseServer) {
	TCHAR *pszPipeName = NamedPipeTest_CreatePipeName ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, bClientToServer, true, READWRITE_OPERATIONS);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, !bClientToServer, READWRITE_OPERATIONS);
	CPipeClosingThread *poCloser = new CPipeClosingThread (bCloseServer ? &poServer->m_poPipe : &poClient->m_poPipe, TIMEOUT_READWRITE * 2, true);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT (poCloser->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_NONE, FAIL_NONE);
	CThread::Release (poServer);
	CThread::Release (poClient);
	CThread::Release (poCloser);
	free (pszPipeName);
}

static void ClientToServerCancelLazyCloseServer () {
	TestCancelLazyClose (true, true);
}

static void ClientToServerCancelLazyCloseClient () {
	TestCancelLazyClose (true, false);
}

static void ServerToClientCancelLazyCloseServer () {
	TestCancelLazyClose (false, true);
}

static void ServerToClientCancelLazyCloseClient () {
	TestCancelLazyClose (false, false);
}

static void RepeatedOperation () {
	TCHAR *pszPipeName = NamedPipeTest_CreatePipeName ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, true, true, 2);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, false, 2);
	poServer->SetRepeat ();
	poClient->SetRepeat ();
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_NONE, FAIL_NONE);
	CThread::Release (poServer);
	CThread::Release (poClient);
	free (pszPipeName);
}

BEGIN_TESTS (NamedPipeTest)
	TEST (ClientToServerComplete)
	TEST (ClientToServerTimeoutConnectServer)
	TEST (ClientToServerTimeoutConnectClient)
	TEST (ClientToServerTimeoutReadServer)
	TEST (ClientToServerTimeoutWriteClient)
	TEST (ServerToClientComplete)
	TEST (ServerToClientTimeoutConnectServer)
	TEST (ServerToClientTimeoutConnectClient)
	TEST (ServerToClientTimeoutWriteServer)
	TEST (ServerToClientTimeoutReadClient)
	TEST (ClientToServerCloseServer)
	TEST (ClientToServerCloseClient)
	TEST (ServerToClientCloseServer)
	TEST (ServerToClientCloseClient)
	TEST (ClientToServerLazyCloseServer)
	TEST (ClientToServerLazyCloseClient)
	TEST (ServerToClientLazyCloseServer)
	TEST (ServerToClientLazyCloseClient)
	TEST (ClientToServerCancelLazyCloseServer)
	TEST (ClientToServerCancelLazyCloseClient)
	TEST (ServerToClientCancelLazyCloseServer)
	TEST (ServerToClientCancelLazyCloseClient)
	TEST (RepeatedOperation)
END_TESTS