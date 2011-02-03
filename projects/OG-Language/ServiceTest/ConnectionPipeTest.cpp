/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Service/ConnectionPipe.cpp

#include "Service/ConnectionPipe.h"
#include "Service/Settings.h"

LOGGING (com.opengamma.language.service.ConnectionPipeTest);

#define TIMEOUT			500
#define TIMEOUT_JOIN	(TIMEOUT * 4)

static CConnectionPipe *_CreateTestPipe () {
	// TODO: use the current user's name as a test suffix
	CConnectionPipe *po = CConnectionPipe::Create (TEXT ("TEST"));
	ASSERT (po);
	return po;
}

#define TEST_USERNAME		"Username"
#define TEST_CPP2JAVA		"\\\\.\\pipe\\Foo"
#define TEST_JAVA2CPP		"\\\\.\\pipe\\Bar"
#define __L(str)			L##str
#define _L(str)				__L(str)

class CServerThread : public CThread {
private:
	CConnectionPipe *m_poPipe;
	bool m_bOk;
public:
	CServerThread (CConnectionPipe *poPipe) {
		m_poPipe = poPipe;
		m_bOk = false;
		ASSERT (Start ());
	}
	void Run () {
		PJAVACLIENT_CONNECT pjcc = m_poPipe->ReadMessage ();
		if (pjcc) {
			LOGDEBUG (TEXT ("UserName=") << JavaClientGetUserName (pjcc));
			LOGDEBUG (TEXT ("CPP2Java=") << JavaClientGetCPPToJavaPipe (pjcc));
			LOGDEBUG (TEXT ("Java2CPP=") << JavaClientGetJavaToCPPPipe (pjcc));
			ASSERT (!_tcscmp (JavaClientGetUserName (pjcc), TEXT (TEST_USERNAME)));
			ASSERT (!_tcscmp (JavaClientGetCPPToJavaPipe (pjcc), TEXT (TEST_CPP2JAVA)));
			ASSERT (!_tcscmp (JavaClientGetJavaToCPPPipe (pjcc), TEXT (TEST_JAVA2CPP)));
			free (pjcc);
			m_bOk = true;
		}
	}
	bool IsOk () {
		return m_bOk;
	}
};

class CClientThread : public CThread {
private:
	const TCHAR *m_pszPipeName;
	bool m_bUnicode;
public:
	CClientThread (const TCHAR *pszPipeName, bool bUnicode) {
		m_pszPipeName = pszPipeName;
		m_bUnicode = bUnicode;
		ASSERT (Start ());
	}
	void Run () {
		int nAttempt = 0;
		CNamedPipe *poPipe;
		do {
			if (nAttempt) {
				Sleep (TIMEOUT / 10);
			}
			poPipe = CNamedPipe::ClientWrite (m_pszPipeName);
		} while (!poPipe && (GetLastError () == ENOENT) && (++nAttempt < 10));
		if (poPipe) {
			LOGDEBUG (TEXT ("Client connected"));
			PJAVACLIENT_CONNECT pjcc;
			if (m_bUnicode) {
				pjcc = JavaClientCreateW (_L (TEST_USERNAME), _L (TEST_CPP2JAVA), _L (TEST_JAVA2CPP));
			} else {
				pjcc = JavaClientCreateA (TEST_USERNAME, TEST_CPP2JAVA, TEST_JAVA2CPP);
			}
			ASSERT (pjcc);
			ASSERT (poPipe->Write (pjcc, pjcc->cbSize, TIMEOUT) == pjcc->cbSize);
			delete poPipe;
		} else {
			LOGWARN (TEXT ("Couldn't open client pipe, error ") << GetLastError ());
		}
	}
};

static void NormalOperation () {
	CConnectionPipe *poPipe = _CreateTestPipe ();
	CServerThread *poServer = new CServerThread (poPipe);
	CClientThread *poClient = new CClientThread (poPipe->GetName (), sizeof (TCHAR) == sizeof (wchar_t));
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT (poServer->IsOk ());
	CThread::Release (poServer);
	CThread::Release (poClient);
	delete poPipe;
}

static void UnicodeMismatch () {
	CConnectionPipe *poPipe = _CreateTestPipe ();
	// Use LazyClose to set a timeout
	poPipe->LazyClose (TIMEOUT);
	CServerThread *poServer = new CServerThread (poPipe);
	CClientThread *poClient = new CClientThread (poPipe->GetName (), sizeof (TCHAR) != sizeof (wchar_t));
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT (!poServer->IsOk ());
	CThread::Release (poServer);
	CThread::Release (poClient);
	delete poPipe;
}

// Note: The lazy cancellation behaviour and timeouts are built on Util components already tested

BEGIN_TESTS (ConnectionPipeTest)
	TEST (NormalOperation)
	TEST (UnicodeMismatch)
END_TESTS