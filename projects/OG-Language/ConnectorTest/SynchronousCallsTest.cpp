/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Connector/SynchronousCalls.cpp

#include "Connector/SynchronousCalls.h"

LOGGING (com.opengamma.language.connector.SynchronousCallsTest);

#define TIMEOUT_MESSAGE	1000
#define TIMEOUT_THREAD	3000

static void AllocateAndRelease () {
	CSynchronousCalls oCalls;
	CSynchronousCallSlot *apSlot[20];
	int i, j;
	for (i = 0; i < 10; i++) {
		apSlot[i] = oCalls.Acquire ();
		ASSERT (apSlot[i]);
		for (j = 0; j < i; j++) {
			ASSERT (apSlot[j] != apSlot[i]);
			ASSERT (apSlot[j]->GetIdentifier () != apSlot[i]->GetIdentifier ());
			ASSERT (apSlot[j]->GetHandle () != apSlot[i]->GetHandle ());
		}
		ASSERT (apSlot[i]->GetSequence () == 1);
		LOGDEBUG (TEXT ("Acquired slot ") << apSlot[i]->GetIdentifier () << TEXT (", handle=") << apSlot[i]->GetHandle ());
	}
	for (i = 0; i < 10; i += 2) {
		apSlot[i]->Release ();
		LOGDEBUG (TEXT ("Released slot ") << apSlot[i]->GetIdentifier ());
		apSlot[i] = NULL;
	}
	for (i = 10; i < 20; i++) {
		apSlot[i] = oCalls.Acquire ();
		ASSERT (apSlot[i]);
		if (i < 15) {
			ASSERT (apSlot[i]->GetSequence () == 2);
		} else {
			ASSERT (apSlot[i]->GetSequence () == 1);
		}
		LOGDEBUG (TEXT ("Acquired slot ") << apSlot[i]->GetIdentifier () << TEXT (", handle=" ) << apSlot[i]->GetHandle ());
	}
	for (i = 0; i < 20; i++) {
		if (apSlot[i]) {
			apSlot[i]->Release ();
			LOGDEBUG (TEXT ("Released slot ") << apSlot[i]->GetIdentifier ());
		}
	}
}

class CPostMessageThread : public CThread {
private:
	FudgeMsg m_msg1, m_msg2;
	fudge_i32 m_nHandle1, m_nHandle2;
	CSynchronousCalls *m_poCalls;
public:
	CPostMessageThread (FudgeMsg msg1, fudge_i32 nHandle1, FudgeMsg msg2, fudge_i32 nHandle2, CSynchronousCalls *poCalls) : CThread () {
		FudgeMsg_retain (msg1);
		FudgeMsg_retain (msg1);
		m_msg1 = msg1;
		m_nHandle1 = nHandle1;
		FudgeMsg_retain (msg2);
		FudgeMsg_retain (msg2);
		m_msg2 = msg2;
		m_nHandle2 = nHandle2;
		m_poCalls = poCalls;
		ASSERT (Start ());
	}
	void Run () {
		LOGDEBUG (TEXT ("Sending first message"));
		m_poCalls->PostAndRelease (m_nHandle1, m_msg1);
		LOGDEBUG (TEXT ("Sending duplicate first message"));
		m_poCalls->PostAndRelease (m_nHandle1, m_msg1);
		LOGDEBUG (TEXT ("Sending second message"));
		m_poCalls->PostAndRelease (m_nHandle2, m_msg2);
		LOGDEBUG (TEXT ("Sending duplicate second message"));
		m_poCalls->PostAndRelease (m_nHandle2, m_msg2);
	}
};

static void PostAndWait () {
	CSynchronousCalls oCalls;
	CSynchronousCallSlot *poSlot = oCalls.Acquire ();
	ASSERT (poSlot);
	int nIdentifier = poSlot->GetIdentifier ();
	FudgeMsg msgA, msgB;
	ASSERT (FudgeMsg_create (&msgA) == FUDGE_OK);
	ASSERT (FudgeMsg_create (&msgB) == FUDGE_OK);
	FudgeMsg msg = poSlot->GetMessage (TIMEOUT_MESSAGE);
	ASSERT (!msg);
	fudge_i32 nHandle1 = poSlot->GetHandle ();
	poSlot->Release ();
	poSlot = oCalls.Acquire ();
	ASSERT (poSlot && (poSlot->GetIdentifier () == nIdentifier));
	fudge_i32 nHandle2 = poSlot->GetHandle ();
	ASSERT (nHandle1 != nHandle2);
	CThread *poSender = new CPostMessageThread (msgA, nHandle2, msgB, nHandle1, &oCalls);
	msg = poSlot->GetMessage (TIMEOUT_MESSAGE);
	ASSERT (msg);
	ASSERT (msg == msgA);
	FudgeMsg_release (msg);
	ASSERT (CThread::WaitAndRelease (poSender, TIMEOUT_THREAD));
	poSlot->Release ();
	poSlot = oCalls.Acquire ();
	ASSERT (poSlot && (poSlot->GetIdentifier () == nIdentifier));
	nHandle1 = poSlot->GetHandle ();
	ASSERT (nHandle1 != nHandle2);
	poSender = new CPostMessageThread (msgA, nHandle2, msgB, nHandle1, &oCalls);
	msg = poSlot->GetMessage (TIMEOUT_MESSAGE);
	ASSERT (msg);
	ASSERT (msg == msgB);
	ASSERT (CThread::WaitAndRelease (poSender, TIMEOUT_THREAD));
	poSlot->Release ();
	FudgeMsg_release (msgA);
	FudgeMsg_release (msgB);
}

BEGIN_TESTS (SynchronousCallsTest)
	TEST (AllocateAndRelease)
	TEST (PostAndWait)
END_TESTS