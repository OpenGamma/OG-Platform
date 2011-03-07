/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Util/Asynchronous.cpp

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

static void BasicOperations () {
	CAsynchronous *poCaller = new CAsynchronous ();
	int nRun1 = 0, nRun2 = 0, nRun3 = 0;
	CTestAsyncOperation1 *poRun1 = new CTestAsyncOperation1 (0, &nRun1);
	CTestAsyncOperation1 *poRun2 = new CTestAsyncOperation1 (2, &nRun2);
	CTestAsyncOperation1 *poRun3 = new CTestAsyncOperation1 (-1, &nRun3);
	ASSERT (poCaller->Run (poRun1));
	ASSERT (poCaller->Run (poRun2));
	ASSERT (poCaller->Run (poRun3));
	CThread::Sleep (TIMEOUT_COMPLETE);
	CAsynchronous::Release (poCaller);
	ASSERT (nRun1 == 1);
	ASSERT (nRun2 == 3);
	ASSERT (nRun3 == 0);
}

static void VitalOperations () {
	CAsynchronous *poCaller = new CAsynchronous ();
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

BEGIN_TESTS (AsynchronousTest)
	TEST (BasicOperations)
	TEST (VitalOperations)
END_TESTS
