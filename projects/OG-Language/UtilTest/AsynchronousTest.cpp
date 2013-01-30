/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Util/Asynchronous.h"

LOGGING (com.opengamma.language.util.AsynchronousTest);

#define TIMEOUT_COMPLETE	1000

class CTestAsyncOperation1 : public CAsynchronous::COperation {
private:
	int m_nRescheduleCount;
	int *m_pnRunCount;
public:
	CTestAsyncOperation1 (int nRescheduleCount, int *pnRunCount) {
		m_nRescheduleCount = nRescheduleCount;
		m_pnRunCount = pnRunCount;
	}
	void Run () {
		(*m_pnRunCount)++;
		if (m_nRescheduleCount > 0) {
			m_nRescheduleCount--;
			MustReschedule ();
		}
	}
	bool OnScheduled () {
		return m_nRescheduleCount >= 0;
	}
};

class CTestAsyncOperation2 : public CAsynchronous::COperation {
private:
	int *m_pnRunCount;
public:
	CTestAsyncOperation2 (bool bVital, int *pnRunCount)
	: COperation (bVital) {
		m_pnRunCount = pnRunCount;
	}
	void Run () {
		CThread::Sleep (TIMEOUT_COMPLETE / 3);
		(*m_pnRunCount)++;
	}
};

class CThreadRecordingOperation : public CAsynchronous::COperation {
private:
	CThread::THREAD_REF *m_phThread;
public:
	CThreadRecordingOperation (CThread::THREAD_REF *phThread) {
		m_phThread = phThread;
	}
	void Run () {
		*m_phThread = CThread::GetThreadRef ();
	}
};

class CDummyThread : public CThread {
public:
	CDummyThread () : CThread () {
		ASSERT (Start ());
	}
	void Run () {
		Sleep (TIMEOUT_COMPLETE);
	}
};

static void BasicOperations () {
	CAsynchronous *poCaller = CAsynchronous::Create ();
	ASSERT (poCaller);
	int nRun1 = 0, nRun2 = 0, nRun3 = 0;
	CTestAsyncOperation1 *poRun1 = new CTestAsyncOperation1 (0, &nRun1);
	CTestAsyncOperation1 *poRun2 = new CTestAsyncOperation1 (2, &nRun2);
	CTestAsyncOperation1 *poRun3 = new CTestAsyncOperation1 (-1, &nRun3);
	ASSERT (poCaller->Run (poRun1));
	ASSERT (poCaller->Run (poRun2));
	ASSERT (poCaller->Run (poRun3));
	CThread::Sleep (TIMEOUT_COMPLETE);
	CAsynchronous::PoisonAndRelease (poCaller);
	CThread::Sleep (TIMEOUT_COMPLETE / 6);
	ASSERT (nRun1 == 1);
	ASSERT (nRun2 == 3);
	ASSERT (nRun3 == 0);
}

static void VitalOperations () {
	CAsynchronous *poCaller = CAsynchronous::Create ();
	ASSERT (poCaller);
	int nRun1 = 0, nRun2 = 0, nRun3 = 0, nRun4 = 0, nRun5 = 0;
	CTestAsyncOperation2 *poRun1 = new CTestAsyncOperation2 (false, &nRun1);
	CTestAsyncOperation2 *poRun2 = new CTestAsyncOperation2 (false, &nRun2);
	CTestAsyncOperation2 *poRun3 = new CTestAsyncOperation2 (true, &nRun3);
	CTestAsyncOperation2 *poRun4 = new CTestAsyncOperation2 (false, &nRun4);
	CTestAsyncOperation2 *poRun5 = new CTestAsyncOperation2 (true, &nRun5);
	ASSERT (poCaller->Run (poRun1));
	ASSERT (poCaller->Run (poRun2));
	ASSERT (poCaller->Run (poRun3));
	ASSERT (poCaller->Run (poRun4));
	ASSERT (poCaller->Run (poRun5));
	CThread::Sleep (TIMEOUT_COMPLETE / 6);
	CAsynchronous::PoisonAndRelease (poCaller);
	CThread::Sleep (TIMEOUT_COMPLETE);
	ASSERT (nRun1 == 1); // started before the poison
	ASSERT (nRun2 == 0); // non-vital operation skipped
	ASSERT (nRun3 == 1);
	ASSERT (nRun4 == 0);
	ASSERT (nRun5 == 1);
}

static void ThreadIdleTimeout () {
	CAsynchronous *poCaller = CAsynchronous::Create ();
	ASSERT (poCaller);
	poCaller->SetTimeoutInactivity (TIMEOUT_COMPLETE / 2);
	CThread::THREAD_REF hThread1 = 0, hThread2 = 0;
	CThreadRecordingOperation *poRun1 = new CThreadRecordingOperation (&hThread1);
	CThreadRecordingOperation *poRun2 = new CThreadRecordingOperation (&hThread2);
	ASSERT (poCaller->Run (poRun1));
	ASSERT (poCaller->Run (poRun2));
	CThread::Sleep (TIMEOUT_COMPLETE / 6);
	ASSERT (hThread1 == hThread2);
	poRun2 = new CThreadRecordingOperation (&hThread2);
	CThread::Sleep (TIMEOUT_COMPLETE);
	// Some O/Ss will re-use the thread ID/reference data so create a dummy thread
	CThread *poDummyThread = new CDummyThread ();
	CThread::Release (poDummyThread);
	ASSERT (poCaller->Run (poRun2));
	CThread::Sleep (TIMEOUT_COMPLETE / 6);
	if (hThread2) {
		ASSERT (hThread1 != hThread2);
	} else {
		LOGWARN (TEXT ("ThreadIdleTimeout test might have failed - no thread identity"));
	}
	CAsynchronous::PoisonAndRelease (poCaller);
	CThread::Sleep (TIMEOUT_COMPLETE / 6);
}

static void ThreadRecycling () {
	CAsynchronous *poCaller = CAsynchronous::Create ();
	ASSERT (poCaller);
	CThread::THREAD_REF hThread1 = 0, hThread2 = 0;
	CThreadRecordingOperation *poRun1 = new CThreadRecordingOperation (&hThread1);
	CThreadRecordingOperation *poRun2 = new CThreadRecordingOperation (&hThread2);
	ASSERT (poCaller->Run (poRun1));
	CThread::Sleep (TIMEOUT_COMPLETE / 6);
	if (hThread1) {
		ASSERT (poCaller->RecycleThread ());
		ASSERT (poCaller->Run (poRun2));
		CThread::Sleep (TIMEOUT_COMPLETE / 6);
		ASSERT (hThread1 != hThread2);
	} else {
		LOGWARN (TEXT ("ThreadRecycling test might have failed - no thread identity"));
	}
	CAsynchronous::PoisonAndRelease (poCaller);
	CThread::Sleep (TIMEOUT_COMPLETE / 6);
}

/// Tests the functions and objects in Util/Asynchronous.cpp
BEGIN_TESTS (AsynchronousTest)
	TEST (BasicOperations)
	TEST (VitalOperations)
	TEST (ThreadIdleTimeout)
	TEST (ThreadRecycling)
END_TESTS
