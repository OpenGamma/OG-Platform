/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Util/NamedPipe.h"
#include "Util/String.h"
#include "Util/Thread.h"
#include "Util/Error.h"

LOGGING (com.opengamma.language.util.NamedPipeTest);

#define TIMEOUT_PIPE			2000
#define TIMEOUT_HANG			(TIMEOUT_PIPE * 2)
#define TIMEOUT_JOIN			(TIMEOUT_PIPE * 4)
#define TIMEOUT_READWRITE		(TIMEOUT_PIPE / 3)
#define READWRITE_OPERATIONS	(TIMEOUT_HANG / TIMEOUT_READWRITE)

// The packet size needs to be so that the client/server can't buffer it and say the write was successful
// but small enough to run the tests quickly
#ifdef _WIN32
#define PAYLOAD_SIZE				256
#else
#define PAYLOAD_SIZE				8192
#endif

static int _ReadAndWrite (CTimeoutIO *poIO, int nOperations, bool bThrottle, const TCHAR *pszLabel) {
#if !defined(_DEBUG) && !defined(FORCE_LOGGING_DEBUG)
	__unused (pszLabel)
#endif /* !_DEBUG && !FORCE_LOGGING_DEBUG */
	LOGDEBUG (pszLabel << TEXT (" ") << ((nOperations > 0) ? TEXT ("reading") : TEXT ("writing")));
	int nCount = 0;
	char *pBuffer = new char[PAYLOAD_SIZE];
	while (nOperations > 0) {
		size_t cbToRead = PAYLOAD_SIZE;
		do {
			LOGDEBUG (pszLabel << TEXT (" reading ") << cbToRead << TEXT (" bytes"));
			size_t cb = poIO->Read (pBuffer, cbToRead, TIMEOUT_PIPE);
			LOGDEBUG (pszLabel << TEXT (" read ") << cb << TEXT (" bytes"));
			if (!cb) {
				delete pBuffer;
				return nCount;
			}
			ASSERT (cb <= cbToRead);
			cbToRead -= cb;
		} while (cbToRead > 0);
		nCount++;
		nOperations--;
		CThread::Sleep (bThrottle ? TIMEOUT_READWRITE : TIMEOUT_READWRITE / 3);
	}
	while (nOperations < 0) {
		size_t cbToWrite = PAYLOAD_SIZE;
		do {
			LOGDEBUG (pszLabel << TEXT (" writing ") << cbToWrite << TEXT (" bytes"));
			size_t cb = poIO->Write (pBuffer, cbToWrite, TIMEOUT_PIPE);
			LOGDEBUG (pszLabel << TEXT (" wrote ") << cb << TEXT (" bytes"));
			if (!cb) {
				delete pBuffer;
				return nCount;
			}
			ASSERT (cb <= cbToWrite);
			cbToWrite -= cb;
		} while (cbToWrite > 0);
		nCount++;
		nOperations++;
		CThread::Sleep (bThrottle ? TIMEOUT_READWRITE : TIMEOUT_READWRITE / 3);
	}
	delete pBuffer;
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
		CNamedPipe *poServer = m_bRead ? CNamedPipe::ServerRead (m_pszPipeName, false) : CNamedPipe::ServerWrite (m_pszPipeName, false);
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
						CThread::Sleep (TIMEOUT_HANG);
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
				CThread::Sleep (TIMEOUT_HANG);
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

class CExclusivePipeServerThread : public CThread {
private:
	const TCHAR *m_pszPipeName;
	bool m_bRead;
public:
	CExclusivePipeServerThread (const TCHAR *pszPipeName, bool bRead) : CThread () {
		m_pszPipeName = pszPipeName;
		m_bRead = bRead;
		ASSERT (Start ());
	}
	void Run () {
		LOGDEBUG (TEXT ("Creating server pipe"));
		CNamedPipe *poServer = m_bRead ? CNamedPipe::ServerRead (m_pszPipeName, true) : CNamedPipe::ServerWrite (m_pszPipeName, true);
		ASSERT (poServer);
		LOGDEBUG (TEXT ("Deleting server object"));
		delete poServer;
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
				CThread::Sleep (TIMEOUT_PIPE / 10);
			}
			m_poPipe = m_bRead ? CNamedPipe::ClientRead (m_pszPipeName) : CNamedPipe::ClientWrite (m_pszPipeName);
		} while ((!m_poPipe) && (NativeGetLastError () == ENOENT) && (++nAttempt < 10));
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
				CThread::Sleep (TIMEOUT_HANG);
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
		CThread::Sleep (TIMEOUT_HANG / 2);
		CNamedPipe *poPipe = *m_ppoPipe;
		ASSERT (poPipe);
		if (m_lLazyClose) {
			LOGDEBUG (TEXT ("Lazy close"));
			poPipe->LazyClose (m_lLazyClose);
			if (m_bCancel) {
				LOGDEBUG (TEXT ("Sleeping"));
				CThread::Sleep (m_lLazyClose / 2);
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
	const TCHAR *pszPipeName = CNamedPipe::GetTestPipePrefix ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, true, true, 2);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, false, 2);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_NONE, FAIL_NONE);
	CThread::Release (poServer);
	CThread::Release (poClient);
}

static void ClientToServerTimeoutConnectServer () {
	const TCHAR *pszPipeName = CNamedPipe::GetTestPipePrefix ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, true, true, 1);
	CThread::Sleep (TIMEOUT_PIPE * 2);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poServer->GetFailure () == FAIL_NO_ACCEPT);
	CThread::Release (poServer);
}

static void ClientToServerTimeoutConnectClient () {
	const TCHAR *pszPipeName = CNamedPipe::GetTestPipePrefix ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, true, false, 0);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, false, 1);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
#ifdef _WIN32
	ASSERT_FAILURE (FAIL_NONE, FAIL_NO_READWRITE);
#else
	ASSERT_FAILURE (FAIL_NONE, FAIL_NO_CREATE);
#endif
	CThread::Release (poServer);
	CThread::Release (poClient);
}

static void ClientToServerTimeoutReadServer () {
	const TCHAR *pszPipeName = CNamedPipe::GetTestPipePrefix ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, true, true, 1);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, false, 0);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_NO_READWRITE, FAIL_NONE);
	CThread::Release (poServer);
	CThread::Release (poClient);
}

static void ClientToServerTimeoutWriteClient () {
	const TCHAR *pszPipeName = CNamedPipe::GetTestPipePrefix ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, true, true, 0);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, false, 1);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_NONE, FAIL_NO_READWRITE);
	CThread::Release (poServer);
	CThread::Release (poClient);
}

static void ServerToClientComplete () {
	const TCHAR *pszPipeName = CNamedPipe::GetTestPipePrefix ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, false, true, 2);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, true, 2);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_NONE, FAIL_NONE);
	CThread::Release (poServer);
	CThread::Release (poClient);
}

static void ServerToClientTimeoutConnectServer () {
	const TCHAR *pszPipeName = CNamedPipe::GetTestPipePrefix ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, false, true, 1);
	CThread::Sleep (TIMEOUT_PIPE * 2);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poServer->GetFailure () == FAIL_NO_ACCEPT);
	CThread::Release (poServer);
}

static void ServerToClientTimeoutConnectClient () {
	const TCHAR *pszPipeName = CNamedPipe::GetTestPipePrefix ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, false, false, 0);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, true, 1);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
#ifdef _WIN32
	ASSERT_FAILURE (FAIL_NONE, FAIL_NO_READWRITE);
#else
	ASSERT_FAILURE (FAIL_NONE, FAIL_NO_CREATE);
#endif
	CThread::Release (poServer);
	CThread::Release (poClient);
}

static void ServerToClientTimeoutReadClient () {
	const TCHAR *pszPipeName = CNamedPipe::GetTestPipePrefix ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, false, true, 0);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, true, 1);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_NONE, FAIL_NO_READWRITE);
	CThread::Release (poServer);
	CThread::Release (poClient);
}

static void ServerToClientTimeoutWriteServer () {
	const TCHAR *pszPipeName = CNamedPipe::GetTestPipePrefix ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, false, true, 1);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, true, 0);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_NO_READWRITE, FAIL_NONE);
	CThread::Release (poServer);
	CThread::Release (poClient);
}

static void TestClose (bool bClientToServer, bool bCloseServer) {
	const TCHAR *pszPipeName = CNamedPipe::GetTestPipePrefix ();
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
	const TCHAR *pszPipeName = CNamedPipe::GetTestPipePrefix ();
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
	poCloser = new CPipeClosingThread (bCloseServer ? &poServer->m_poPipe : &poClient->m_poPipe, TIMEOUT_READWRITE / 3, false);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT (poCloser->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_PARTIAL_READWRITE, FAIL_PARTIAL_READWRITE);
	CThread::Release (poServer);
	CThread::Release (poClient);
	CThread::Release (poCloser);
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
	const TCHAR *pszPipeName = CNamedPipe::GetTestPipePrefix ();
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
	const TCHAR *pszPipeName = CNamedPipe::GetTestPipePrefix ();
	CPipeServerThread *poServer = new CPipeServerThread (pszPipeName, true, true, 2);
	CPipeClientThread *poClient = new CPipeClientThread (pszPipeName, false, 2);
	poServer->SetRepeat ();
	poClient->SetRepeat ();
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT_FAILURE (FAIL_NONE, FAIL_NONE);
	CThread::Release (poServer);
	CThread::Release (poClient);
}

static void ServerWriteExclusive () {
	const TCHAR *pszPipeName = CNamedPipe::GetTestPipePrefix ();
	CExclusivePipeServerThread *poServer = new CExclusivePipeServerThread (pszPipeName, false);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	CThread::Release (poServer);
}

static void ServerReadExclusive () {
	const TCHAR *pszPipeName = CNamedPipe::GetTestPipePrefix ();
	CExclusivePipeServerThread *poServer = new CExclusivePipeServerThread (pszPipeName, true);
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	CThread::Release (poServer);
}

/// Tests the functions and objects in Util/NamedPipe.cpp
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
	TEST (ServerWriteExclusive)
	TEST (ServerReadExclusive)
END_TESTS
