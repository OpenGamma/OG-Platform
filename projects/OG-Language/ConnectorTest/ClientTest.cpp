/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Connector/Client.cpp

#include "Connector/Client.h"

LOGGING (com.opengamma.language.connector.ClientTest);

#define TIMEOUT_START		30000
#define TIMEOUT_HEARTBEAT	30000

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
	bool Running () { return m_bRunningEntered && !m_bRunningLeft && !m_bStoppedEntered; }
	bool Stopped () { return m_bRunningEntered && m_bRunningLeft && m_bStoppedEntered; }
};

static CClientService *g_poService;
static CStateCallback *g_poCallback;

static void Start () {
	g_poService = CClientService::Create ();
	ASSERT (g_poService);
	g_poCallback = new CStateCallback ();
	g_poService->SetStateChangeCallback (g_poCallback);
	ASSERT (g_poService->Start ());
	int n;
	LOGDEBUG (TEXT ("Waiting for client to start"));
	for (n = 0; (g_poService->GetState () != RUNNING) && (n < TIMEOUT_START / 100); n++) {
		CThread::Sleep (100);
	}
	ASSERT (g_poService->GetState () == RUNNING);
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

// The messaging is tested by the tests for Connector

BEGIN_TESTS (ClientTest)
	TEST (StartStop)
	TEST (Heartbeat)
	BEFORE_TEST (Start)
	AFTER_TEST (Stop)
END_TESTS