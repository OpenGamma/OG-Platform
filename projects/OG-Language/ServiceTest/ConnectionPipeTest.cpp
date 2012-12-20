/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Service/ConnectionPipe.cpp

#include "Service/ConnectionPipe.h"
#include "Service/Settings.h"
#include <Util/Thread.h>

LOGGING (com.opengamma.language.service.ConnectionPipeTest);

#define TIMEOUT			500
#define TIMEOUT_JOIN	(TIMEOUT * 4)

static CConnectionPipe *_CreateTestPipe () {
	// TODO: use the current user's name as a test suffix
	CConnectionPipe *po = CConnectionPipe::Create (TEXT ("TEST"));
	ASSERT (po);
	return po;
}

#define TEST_USERNAME		TEXT ("Username")
#define TEST_CPP2JAVA		TEXT ("\\\\.\\pipe\\Foo")
#define TEST_JAVA2CPP		TEXT ("\\\\.\\pipe\\Bar")
#define TEST_LANGUAGE		TEXT ("test")
#ifdef _DEBUG
#define TEST_DEBUG			FUDGE_TRUE
#else /* ifdef _DEBUG */
#define TEST_DEBUG			FUDGE_FALSE
#endif /* ifdef _DEBUG */

class CConnectionServerThread : public CThread {
private:
	CConnectionPipe *m_poPipe;
	bool m_bOk;
public:
	CConnectionServerThread (CConnectionPipe *poPipe) {
		m_poPipe = poPipe;
		m_bOk = false;
		ASSERT (Start ());
	}
	void Run () {
		ClientConnect *pcc = m_poPipe->ReadMessage ();
		if (pcc) {
			LOGDEBUG (TEXT ("UserName=") << pcc->_userName);
			LOGDEBUG (TEXT ("CPP2Java=") << pcc->_CPPToJavaPipe);
			LOGDEBUG (TEXT ("Java2CPP=") << pcc->_JavaToCPPPipe);
			LOGDEBUG (TEXT ("Language=") << pcc->_languageID);
			LOGDEBUG (TEXT ("Debug=") << pcc->_debug);
			ASSERT (!_tcscmp (pcc->_userName, TEST_USERNAME));
			ASSERT (!_tcscmp (pcc->_CPPToJavaPipe, TEST_CPP2JAVA));
			ASSERT (!_tcscmp (pcc->_JavaToCPPPipe, TEST_JAVA2CPP));
			ASSERT (!_tcscmp (pcc->_languageID, TEST_LANGUAGE));
			ASSERT (pcc->_debug == TEST_DEBUG);
			ClientConnect_free (pcc);
			m_bOk = true;
		}
	}
	bool IsOk () {
		return m_bOk;
	}
};

class CConnectionClientThread : public CThread {
private:
	const TCHAR *m_pszPipeName;
public:
	CConnectionClientThread (const TCHAR *pszPipeName) {
		m_pszPipeName = pszPipeName;
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
			ClientConnect cc;
			memset (&cc, 0, sizeof (cc));
			cc._userName = TEST_USERNAME;
			cc._CPPToJavaPipe = TEST_CPP2JAVA;
			cc._JavaToCPPPipe = TEST_JAVA2CPP;
			cc._languageID = TEST_LANGUAGE;
			cc._debug = TEST_DEBUG;
			FudgeMsg msg;
			ASSERT (ClientConnect_toFudgeMsg (&cc, &msg) == FUDGE_OK);
			FudgeMsgEnvelope env;
			ASSERT (FudgeMsgEnvelope_create (&env, 0, 0, 0, msg) == FUDGE_OK);
			fudge_byte *ptrBuffer;
			fudge_i32 cbBuffer;
			ASSERT (FudgeCodec_encodeMsg (env, &ptrBuffer, &cbBuffer) == FUDGE_OK);
			FudgeMsgEnvelope_release (env);
			FudgeMsg_release (msg);
			ASSERT (poPipe->Write (ptrBuffer, cbBuffer, TIMEOUT) == (size_t)cbBuffer);
			delete ptrBuffer;
		} else {
			LOGWARN (TEXT ("Couldn't open client pipe, error ") << GetLastError ());
		}
	}
};

static void NormalOperation () {
	CConnectionPipe *poPipe = _CreateTestPipe ();
	CConnectionServerThread *poServer = new CConnectionServerThread (poPipe);
	CConnectionClientThread *poClient = new CConnectionClientThread (poPipe->GetName ());
	ASSERT (poServer->Wait (TIMEOUT_JOIN));
	ASSERT (poClient->Wait (TIMEOUT_JOIN));
	ASSERT (poServer->IsOk ());
	CThread::Release (poServer);
	CThread::Release (poClient);
	delete poPipe;
}

// Note: The lazy cancellation behaviour and timeouts are built on Util components already tested

BEGIN_TESTS (ConnectionPipeTest)
	TEST (NormalOperation)
END_TESTS
