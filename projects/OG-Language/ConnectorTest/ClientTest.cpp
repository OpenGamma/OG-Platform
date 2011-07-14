/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Connector/Client.cpp

#include "Connector/Client.h"
#define FUDGE_NO_NAMESPACE
#include "Connector/com_opengamma_language_connector_Test.h"
#include "Connector/com_opengamma_language_connector_UserMessage.h"

LOGGING (com.opengamma.language.connector.ClientTest);

#define TEST_LANGUAGE		TEXT ("test")
#define TIMEOUT_START		30000
#define TIMEOUT_HEARTBEAT	30000
#define TIMEOUT_MESSAGE		3000
#define TIMEOUT_CALLBACK	1000

class CStateCallback : public CClientService::CStateChange {
private:
	bool m_bRunningEntered;
	bool m_bRunningLeft;
	bool m_bStoppedEntered;
protected:
	void OnStateChange (ClientServiceState ePreviousState, ClientServiceState eNewState) {
		LOGINFO (TEXT ("Previous state = ") << ePreviousState << TEXT (", new state = ") << eNewState);
		if (eNewState == RUNNING) {
			m_bRunningEntered = true;
		} else if (eNewState == STOPPED) {
			m_bStoppedEntered = true;
		}
		if (ePreviousState == RUNNING) {
			m_bRunningLeft = true;
		}
	}
public:
	CStateCallback () : CStateChange () {
		m_bRunningEntered = false;
		m_bRunningLeft = false;
		m_bStoppedEntered = false;
	}
	bool RunningLeft () { return m_bRunningLeft; }
	bool Running () { return m_bRunningEntered && !m_bRunningLeft && !m_bStoppedEntered; }
	bool Stopped () { return m_bRunningEntered && m_bRunningLeft && m_bStoppedEntered; }
};

class CMessageCallback : public CClientService::CMessageReceived {
private:
	CAtomicPointer<FudgeMsg> m_msg;
protected:
	void OnMessageReceived (FudgeMsg msg) {
		LOGINFO (TEXT ("Message received"));
		FudgeMsg_retain (msg);
		// Not expecting to have had another message
		ASSERT (!m_msg.GetAndSet (msg));
	}
public:
	~CMessageCallback () {
		FudgeMsg msg = m_msg.GetAndSet (NULL);
		if (msg) {
			FudgeMsg_release (msg);
		}
	}
	FudgeMsg GetMessage () {
		return m_msg.GetAndSet (NULL);
	}
};

static CClientService *g_poService;
static CStateCallback *g_poCallback;

static void Start () {
	g_poService = CClientService::Create (TEST_LANGUAGE);
	ASSERT (g_poService);
	g_poCallback = new CStateCallback ();
	g_poService->SetStateChangeCallback (g_poCallback);
	ASSERT (g_poService->Start ());
	int n;
	LOGDEBUG (TEXT ("Waiting for client to start"));
	for (n = 0; (g_poService->GetState () != RUNNING) && (g_poService->GetState () != ERRORED) && (g_poService->GetState () != STOPPED) && (n < TIMEOUT_START / 100); n++) {
		CThread::Sleep (100);
	}
	ASSERT (g_poService->GetState () == RUNNING);
	LOGDEBUG (TEXT ("Waiting for callback to arrive"));
	for (n = 0; !g_poCallback->Running () && (n < TIMEOUT_CALLBACK / 100); n++) {
		CThread::Sleep (100);
	}
	ASSERT (g_poCallback->Running ());
}

static void Stop () {
	ASSERT (g_poService->Stop ());
	ASSERT (g_poService->GetState () == STOPPED);
	ASSERT (g_poCallback->Stopped ());
	g_poService->SetStateChangeCallback (NULL);
	delete g_poCallback;
	CClientService::Release (g_poService);
}

static void StartStop () {
	// No-op; the fun is in Start and Stop
}

static void Heartbeat () {
	LOGDEBUG (TEXT ("Sleeping to test the heartbeat mechanism"));
	CThread::Sleep (TIMEOUT_HEARTBEAT);
	ASSERT (g_poService->GetState () == RUNNING);
}

static void Message () {
	int i = GetTickCount ();
	FudgeMsg msgUser;
	FudgeMsg msgTest;
	ASSERT (FudgeMsg_create (&msgTest) == FUDGE_OK);
	ASSERT (Test_addClass (msgTest) == FUDGE_OK);
	ASSERT (Test_setOperation (msgTest, ECHO_REQUEST) == FUDGE_OK);
	ASSERT (Test_setNonce (msgTest, i + 1) == FUDGE_OK);
	ASSERT (FudgeMsg_create (&msgUser) == FUDGE_OK);
	ASSERT (UserMessage_setHandle (msgUser, i) == FUDGE_OK);
	ASSERT (UserMessage_setFudgeMsgPayload (msgUser, msgTest) == FUDGE_OK);
	FudgeMsg_release (msgTest);
	CMessageCallback message;
	g_poService->SetMessageReceivedCallback (&message);
	LOGDEBUG (TEXT ("Sending REQUEST message"));
	g_poService->Send (msgUser);
	FudgeMsg_release (msgUser);
	int n;
	LOGDEBUG (TEXT ("Waiting for response"));
	FudgeMsg msg;
	for (n = 0; !(msg = message.GetMessage ()) && (n < TIMEOUT_MESSAGE / 100); n++) {
		CThread::Sleep (100);
	}
	ASSERT (msg);
	int j;
	ASSERT (UserMessage_getHandle (msg, &j) == FUDGE_OK);
	ASSERT (i == j);
	FudgeMsg_release (msg);
}

static void Stash () {
	int i = GetTickCount ();
	FudgeMsg msgUser;
	FudgeMsg msgTest;
	ASSERT (FudgeMsg_create (&msgTest) == FUDGE_OK);
	ASSERT (Test_addClass (msgTest) == FUDGE_OK);
	ASSERT (Test_setOperation (msgTest, CRASH_REQUEST) == FUDGE_OK);
	ASSERT (Test_setNonce (msgTest, i + 1) == FUDGE_OK);
	ASSERT (FudgeMsg_create (&msgUser) == FUDGE_OK);
	ASSERT (UserMessage_setHandle (msgUser, i) == FUDGE_OK);
	ASSERT (UserMessage_setFudgeMsgPayload (msgUser, msgTest) == FUDGE_OK);
	FudgeMsg_release (msgTest);
	CMessageCallback message;
	g_poService->SetMessageReceivedCallback (&message);
	LOGDEBUG (TEXT ("Sending CRASH_REQUEST message"));
	ASSERT (g_poService->Send (msgUser));
	FudgeMsg_release (msgUser);
	LOGDEBUG (TEXT ("Waiting for callback to arrive"));
	int n;
	for (n = 0; !g_poCallback->RunningLeft () && (n < TIMEOUT_MESSAGE / 100); n++) {
		CThread::Sleep (100);
	}
	LOGDEBUG (TEXT ("Sending STASH_REQUEST message"));
	ASSERT (FudgeMsg_create (&msgTest) == FUDGE_OK);
	ASSERT (Test_addClass (msgTest) == FUDGE_OK);
	ASSERT (Test_setOperation (msgTest, STASH_REQUEST) == FUDGE_OK);
	ASSERT (Test_setNonce (msgTest, i + 3) == FUDGE_OK);
	ASSERT (FudgeMsg_create (&msgUser) == FUDGE_OK);
	ASSERT (UserMessage_setHandle (msgUser, i + 2) == FUDGE_OK);
	ASSERT (UserMessage_setFudgeMsgPayload (msgUser, msgTest) == FUDGE_OK);
	FudgeMsg_release (msgTest);
	for (n = 0; !g_poService->Send (msgUser) && (n < TIMEOUT_START / 100); n++) {
		CThread::Sleep (100);
	}
	FudgeMsg_release (msgUser);
	LOGDEBUG (TEXT ("Waiting for response"));
	FudgeMsg msg;
	for (n = 0; !(msg = message.GetMessage ()) && (n < TIMEOUT_MESSAGE / 100); n++) {
		CThread::Sleep (100);
	}
	ASSERT (msg);
	int j;
	ASSERT (UserMessage_getHandle (msg, &j) == FUDGE_OK);
	ASSERT (i + 2 == j);
	FudgeMsg_release (msg);
}

// The messaging is tested by the tests for Connector

BEGIN_TESTS (ClientTest)
	TEST (StartStop)
	TEST (Heartbeat)
	TEST (Message)
	TEST (Stash)
	BEFORE_TEST (Start)
	AFTER_TEST (Stop)
END_TESTS
