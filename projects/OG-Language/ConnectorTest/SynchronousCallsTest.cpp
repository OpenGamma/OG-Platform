/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Connector/SynchronousCalls.cpp

#ifdef _M_CEE
#ifdef _UNICODE
#define GetMessage	GetMessageW
#else
#define GetMessage	GetMessageA
#endif /* ifdef _UNICODE */
#endif /* ifdef _M_CEE */

#include "Connector/SynchronousCalls.h"
#include <Util/Thread.h>

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
	msg = poSlot->GetMessage (TIMEOUT_MESSAGE);
	ASSERT (!msg);
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

class CRapidPostThread : public CThread {
private:
	FudgeMsg m_msg;
	CSynchronousCallSlot *m_poSlot;
	CSynchronousCalls *m_poCalls;
	CSemaphore m_oSignal;
	volatile bool m_bPoison;
public:
	CRapidPostThread (FudgeMsg msg, CSynchronousCallSlot *poSlot, CSynchronousCalls *poCalls)
	: CThread (), m_oSignal (0, 1) {
		FudgeMsg_retain (msg);
		m_msg = msg;
		m_poSlot = poSlot;
		m_poCalls = poCalls;
		m_bPoison = false;
		ASSERT (Start ());
	}
	~CRapidPostThread () {
		FudgeMsg_release (m_msg);
	}
	void Run () {
		LOGDEBUG (TEXT ("Starting posting thread"));
		m_oSignal.Signal ();
		while (!m_bPoison) {
			int nHandle = m_poSlot->GetHandle ();
			CThread::Sleep (TIMEOUT_MESSAGE / 10);
			LOGINFO (TEXT ("Posting handle ") << nHandle);
			FudgeMsg_retain (m_msg);
			m_poCalls->PostAndRelease (nHandle, m_msg);
		}
	}
	void WaitForStart () {
		ASSERT (m_oSignal.Wait (TIMEOUT_THREAD));
	}
	void Stop () {
		m_bPoison = true;
	}
};

static void RapidCalls () {
	CSynchronousCalls oCalls;
	CSynchronousCallSlot *poSlot = oCalls.Acquire ();
	ASSERT (poSlot);
	FudgeMsg msg;
	ASSERT (FudgeMsg_create (&msg) == FUDGE_OK);
	CRapidPostThread *poPoster = new CRapidPostThread (msg, poSlot, &oCalls);
	LOGDEBUG (TEXT ("Waiting for posting thread"));
	poPoster->WaitForStart ();
	LOGDEBUG (TEXT ("Reading messages"));
	int i, nGot = 0;
	for (i = 0; i < 30; i++) {
		FudgeMsg msg2 = poSlot->GetMessage ((TIMEOUT_MESSAGE / 100) * i);
		if (msg2) {
			LOGINFO (TEXT ("Got message ") << (i + 1));
			nGot++;
		} else {
			LOGINFO (TEXT ("No message ") << (i + 1));
		}
		poSlot->Release ();
		poSlot = oCalls.Acquire ();
	}
	poPoster->Stop ();
	poSlot->Release ();
	ASSERT (CThread::WaitAndRelease (poPoster, TIMEOUT_THREAD));
	FudgeMsg_release (msg);
	LOGINFO (TEXT ("Got ") << nGot << TEXT (" of 30"));
	/* Assuming 1000ms for TIMEOUT_MESSAGE; possible run is
	 *
	 *  Time	Get			Send
	 *     0	Fail  1
	 * 	  10	Fail  2
	 * 	  30	Fail  3
	 * 	  60	Fail  4 
	 * 	 100    Fail  5		Fail 1
	 * 	 150	Fail  6
	 * 	 200				Fail 5 or 6
	 * 	 210	Fail  7
	 * 	 280	Fail  8
	 * 	 300				Fail 7
	 * 	 360	Fail  9
	 * 	 400				Fail 9
	 * 	 450	Fail 10
	 * 	 500	Fail 11		Fail 10
	 * 	 600				Poss 11,12
	 * 	 610	Poss 12
	 * 	 700				Fail 12
	 * 	 730	Fail 13
	 * 	 800				Fail 13
	 * 	 900				Done 14
	 * 	 860	Get 14
	 * 	1000	Fail 15		Fail 14
	 * 	1150	Fail 16
	 * 	1200				Fail 15 or 16
	 * 	1300				Done 17
	 *  1310	Get 17
	 *  1400				Fail 17
	 *  1480	Fail 18
	 *  1500				Fail 18
	 *  1600				Done 19
	 *  1660	Get 19
	 *  1700				Fail 19
	 *  1800				Done 20
	 *  1850	Get 20
	 *  1900				Fail 20
	 *  2000				Done 21
	 *  2050	Get 21
	 *  2100				Fail 21
	 *  2200				Done 22
	 *  2260	Get 22
	 *  2300				Fail 22
	 *  2400				Done 23
	 *  2480	Get 23
	 *  2500				Fail 23
	 *  2600				Done 24
	 *  2700				Fail 24
	 *  2710	Get 24
	 *  2800				Fail 24
	 *  2900				Done 25
	 *  2950	Get 25
	 *  3000				Fail 25
	 *  3100				Done 26
	 *  3200	Get 26		Fail 26
	 *  3300				Done 27
	 *  3400				Fail 27
	 *  3460	Get 27
	 *  3500				Fail 27
	 *  3600				Done 28
	 *  3700				Fail 28
	 *  3730	Get 28
	 *  3800				Fail 28
	 *  3900				Fail 29
	 *  4000				Fail 29
	 *  4010	Get 29
	 *  4100				Fail 29
	 *  4200				Done 30
	 *  4300	Get 30		Fail 30
	 */
	// Expect to miss all of the first ten, but get all of the last ten. Those in
	// the middle are a bit hit or miss. Perhaps we might expect around 14 (e.g. above)
	// but it's all dependent on scheduling.
	ASSERT (nGot > 10);
}

BEGIN_TESTS (SynchronousCallsTest)
	TEST (AllocateAndRelease)
	TEST (PostAndWait)
	TEST (RapidCalls)
END_TESTS
