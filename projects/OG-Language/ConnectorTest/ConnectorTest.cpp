/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Connector/Connector.cpp

#include "Connector/Connector.h"
#define FUDGE_NO_NAMESPACE
#include "Connector/com_opengamma_language_connector_Test.h"

LOGGING (com.opengamma.language.connector.ConnectorTest);

#define TEST_LANGUAGE		TEXT ("test")
#define TIMEOUT_STARTUP		30000
#define TIMEOUT_CALL		3000
#ifdef TIMEOUT_ASYNC
#undef TIMEOUT_ASYNC
#endif /* ifdef TIMEOUT_ASYNC */
#define TIMEOUT_ASYNC		1000

class CTestMessageCallback : public CConnector::CCallback {
private:
	CSemaphore m_oSemaphore;
	CMutex m_oMutex;
	FudgeMsg m_msg;
	int m_nMessages;
	int m_nDisconnects;
protected:
	void OnMessage (FudgeMsg msgPayload) {
		LOGDEBUG (TEXT ("Message received"));
		m_oMutex.Enter ();
		if (m_msg) {
			FudgeMsg_release (m_msg);
		}
		m_msg = msgPayload;
		FudgeMsg_retain (m_msg);
		m_nMessages++;
		m_oMutex.Leave ();
		m_oSemaphore.Signal ();
	}
	void OnThreadDisconnect () {
		LOGDEBUG (TEXT ("Callback thread disconnected"));
		m_oMutex.Enter ();
		m_nDisconnects++;
		m_oMutex.Leave ();
	}
public:
	CTestMessageCallback () : CCallback () {
		m_msg = NULL;
		m_nMessages = 0;
		m_nDisconnects = 0;
	}
	~CTestMessageCallback () {
		LOGDEBUG (TEXT ("Callback object destroyed"));
		if (m_msg) {
			FudgeMsg_release (m_msg);
		}
	}
	bool WaitForMessage (FudgeMsg *pmsg) {
		if (!m_oSemaphore.Wait (TIMEOUT_ASYNC)) {
			LOGDEBUG (TEXT ("Timeout waiting for message"));
			return false;
		}
		bool bResult;
		m_oMutex.Enter ();
		if (m_msg) {
			*pmsg = m_msg;
			FudgeMsg_retain (m_msg);
			bResult = true;
		} else {
			bResult = false;
		}
		m_oMutex.Leave ();
		return bResult;
	}
	bool Disconnected () {
		m_oMutex.Enter ();
		LOGDEBUG (TEXT ("messages=") << m_nMessages << TEXT (", disconnects=") << m_nDisconnects);
		bool bResult = (m_nDisconnects == 1);
		m_oMutex.Leave ();
		return bResult;
	}
};

static CConnector *g_poConnector;
static fudge_i32 g_nonce;
static CTestMessageCallback *g_poCallback;

static void StartConnector () {
	g_poConnector = CConnector::Start (TEST_LANGUAGE);
	ASSERT (g_poConnector);
	ASSERT (g_poConnector->WaitForStartup (TIMEOUT_STARTUP));
	g_poCallback = new CTestMessageCallback ();
	ASSERT (g_poConnector->AddCallback (Test_Class, g_poCallback));
	g_nonce = (fudge_i32)GetTickCount ();
}

static void StopConnector () {
	ASSERT (g_poConnector->Stop ());
	CConnector::Release (g_poConnector);
	g_poConnector = NULL;
	int n;
	for (n = 0; !g_poCallback->Disconnected () && (n < 10); n++) {
		LOGDEBUG (TEXT ("Waiting for disconnect to propogate"));
		CThread::Sleep (TIMEOUT_CALL / 10);
	}
	ASSERT (g_poCallback->Disconnected ());
	CConnector::CCallback::Release (g_poCallback);
}

static void CreateTestMessage (FudgeMsg *pmsg, Operation operation) {
	Test test;
	memset (&test, 0, sizeof (test));
	test._operation = operation;
	test._nonce = g_nonce;
	ASSERT (Test_toFudgeMsg (&test, pmsg) == FUDGE_OK);
}

static void CheckTestResponse (FudgeMsg msg, Operation operation) {
	Test *ptest;
	ASSERT (Test_fromFudgeMsg (msg, &ptest) == FUDGE_OK);
	ASSERT (ptest);
	ASSERT (ptest->_operation == operation);
	ASSERT (ptest->_nonce == g_nonce);
	free (ptest);
}

static void StartStop () {
	// No-op; the fun is in the StartConnector and StopConnector methods
}

static void SyncCall (long lTimeout = TIMEOUT_CALL) {
	FudgeMsg msgToSend;
	FudgeMsg msgReceived;
	CreateTestMessage (&msgToSend, ECHO_REQUEST);
	ASSERT (g_poConnector->Call (msgToSend, &msgReceived, lTimeout));
	ASSERT (msgReceived);
	CheckTestResponse (msgReceived, ECHO_RESPONSE);
	FudgeMsg_release (msgReceived);
	ASSERT (!g_poCallback->WaitForMessage (&msgReceived));
	FudgeMsg_release (msgToSend);
}

static void AsyncCall () {
	FudgeMsg msgToSend;
	CreateTestMessage (&msgToSend, VOID_REQUEST);
	ASSERT (g_poConnector->Send (msgToSend));
	// Pause to allow the client to deliver or we see weird things happening in the log (e.g. it poisons itself before processing the message)
	CThread::Sleep (TIMEOUT_ASYNC);
	FudgeMsg_release (msgToSend);
	ASSERT (!g_poCallback->WaitForMessage (&msgToSend));
}

static void SyncCallWithAsyncCallback () {
	FudgeMsg msgToSend;
	FudgeMsg msgReceived;
	CreateTestMessage (&msgToSend, ECHO_REQUEST_A);
	ASSERT (g_poConnector->Call (msgToSend, &msgReceived, TIMEOUT_CALL));
	ASSERT (msgReceived);
	CheckTestResponse (msgReceived, ECHO_RESPONSE);
	FudgeMsg_release (msgReceived);
	ASSERT (g_poCallback->WaitForMessage (&msgReceived));
	CheckTestResponse (msgReceived, ECHO_RESPONSE_A);
	FudgeMsg_release (msgReceived);
	FudgeMsg_release (msgToSend);
}

static void AsyncCallWithAsyncCallback () {
	FudgeMsg msgToSend;
	FudgeMsg msgReceived;
	CreateTestMessage (&msgToSend, VOID_REQUEST_A);
	ASSERT (g_poConnector->Send (msgToSend));
	ASSERT (g_poCallback->WaitForMessage (&msgReceived));
	CheckTestResponse (msgReceived, VOID_RESPONSE_A);
	FudgeMsg_release (msgReceived);
	FudgeMsg_release (msgToSend);
	// Check explicit callback unregistration
	ASSERT (!g_poCallback->Disconnected ());
	ASSERT (g_poConnector->RemoveCallback (g_poCallback));
}

static void JVMCrash () {
	FudgeMsg msgToSend;
	FudgeMsg msgReceived;
	CreateTestMessage (&msgToSend, CRASH_REQUEST);
	unsigned long tStart = GetTickCount ();
	ASSERT (!g_poConnector->Call (msgToSend, &msgReceived, 2 * TIMEOUT_CALL));
	unsigned long tEnd = GetTickCount ();
	// If all is good, the JVM will have crashed quickly and the call was aborted before the timeout
	ASSERT (tEnd - tStart < TIMEOUT_CALL);
	FudgeMsg_release (msgToSend);
	// And now it should be operational again
	SyncCall (TIMEOUT_STARTUP);
}

static void JVMHang () {
	FudgeMsg msgToSend;
	FudgeMsg msgReceived;
	CreateTestMessage (&msgToSend, PAUSE_REQUEST);
	unsigned long tStart = GetTickCount ();
	ASSERT (!g_poConnector->Call (msgToSend, &msgReceived, 4 * TIMEOUT_CALL));
	unsigned long tEnd = GetTickCount ();
	// If all is good, the JVM will have stopped beating and we aborted the call before the timeout
	ASSERT (tEnd - tStart < 3 * TIMEOUT_CALL);
	FudgeMsg_release (msgToSend);
	// And now it should be operational again
	SyncCall (TIMEOUT_STARTUP);
}

BEGIN_TESTS (ConnectorTest)
	TEST (StartStop)
	TEST (SyncCall)
	TEST (AsyncCall)
	TEST (SyncCallWithAsyncCallback)
	TEST (AsyncCallWithAsyncCallback)
	TEST (JVMCrash)
	TEST (JVMHang)
	BEFORE_TEST (StartConnector)
	AFTER_TEST (StopConnector)
END_TESTS
