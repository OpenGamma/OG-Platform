/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Base class for an asynchronous callback service

#include "Logging.h"
#include "Asynchronous.h"

#define DEFAULT_TIMEOUT_INACTIVITY		 30000 /* 30s of inactivity and threads die */
#define DEFAULT_TIMEOUT_RESCHEDULE		   250 /* 1/4 s retry delay */
#define DEFAULT_TIMEOUT_INFO_PERIOD		  5000 /* 5s before writing to log */
#define DEFAULT_TIMEOUT_ABORT_PERIOD	120000 /* 2m before aborting the operation (and all that follow) */

LOGGING (com.opengamma.language.util.Asynchronous);

CAsynchronous::COperation::COperation (bool bVital) {
	m_bVital = bVital;
	m_poNext = NULL;
}

// If the operation was rescheduled, returns the number of times.
int CAsynchronous::COperation::WasRescheduled () const {
	if (m_nMustReschedule == 0) {
		return 0;
	} else if (m_nMustReschedule < 0) {
		return -m_nMustReschedule;
	} else {
		LOGFATAL ("WasRescheduled cannot be called after MustReschedule");
		return 0;
	}
}

// Flags the operation for rescheduling.
void CAsynchronous::COperation::MustReschedule () {
	if (m_nMustReschedule <= 0) {
		m_nMustReschedule = (-m_nMustReschedule) + 1;
	}
}

CAsynchronous::CAsynchronous ()
: m_oRefCount (1), m_semQueue (0, 1), m_semThread (1, 1) {
	m_poHead = m_poTail = NULL;
	m_poRunner = NULL;
	m_bWaiting = false;
	m_bPoison = false;
	m_lTimeoutAbortPeriod = DEFAULT_TIMEOUT_ABORT_PERIOD;
	m_lTimeoutInactivity = DEFAULT_TIMEOUT_INACTIVITY;
	m_lTimeoutInfoPeriod = DEFAULT_TIMEOUT_INFO_PERIOD;
	m_lTimeoutReschedule = DEFAULT_TIMEOUT_RESCHEDULE;
}

CAsynchronous::~CAsynchronous () {
	LOGDEBUG ("Deleting object");
	if (m_poRunner) {
		CThread::Release (m_poRunner);
	}
	if (m_poHead) {
		LOGWARN ("Operations in queue at destruction");
		while (m_poHead != NULL) {
			COperation *poOperation = m_poHead;
			m_poHead = poOperation->m_poNext;
			if (poOperation->m_bVital) {
				LOGERROR ("Vital operation in queue at destruction");
			} else {
				LOGDEBUG ("Deleting operation");
			}
			delete poOperation;
		}
	}
}

class CAsynchronousRunnerThread : public CThread {
private:
	CAsynchronous *m_poCaller;
public:
	CAsynchronousRunnerThread (CAsynchronous *poCaller)
	: CThread () {
		poCaller->Retain ();
		m_poCaller = poCaller;
	}
	~CAsynchronousRunnerThread () {
		if (m_poCaller) {
			CAsynchronous::Release (m_poCaller);
		}
	}
	void Run () {
		m_poCaller->MakeCallbacks (this);
		m_poCaller->OnThreadExit ();
		CAsynchronous::Release (m_poCaller);
		m_poCaller = NULL;
	}
};

void CAsynchronous::MakeCallbacks (const CThread *poRunner) {
	LOGDEBUG (TEXT ("Waiting for thread control semaphore"));
	m_semThread.Wait ();
	LOGDEBUG (TEXT ("Thread control semaphore signalled"));
	while (!m_bPoison) {
		EnterCriticalSection ();
		if (poRunner != m_poRunner) goto abandonLoop;
		while (!m_poHead) {
			m_bWaiting = true;
			LeaveCriticalSection ();
			LOGDEBUG ("List is empty - waiting on semaphore");
			if (!m_semQueue.Wait (GetTimeoutInactivity ())) {
				LOGDEBUG ("Thread inactivity timeout reached");
				EnterCriticalSection ();
				if (poRunner != m_poRunner) goto abandonLoop;
				if (!m_poHead) {
					LOGINFO ("Terminating idle callback thread");
					CThread::Release (m_poRunner);
					m_poRunner = NULL;
					m_bWaiting = false;
					LOGDEBUG (TEXT ("Signalling thread control semaphore"));
					m_semThread.Signal ();
					LeaveCriticalSection ();
					return;
				} else {
					LOGDEBUG ("Operation received on idle timeout boundary");
				}
				LeaveCriticalSection ();
			}
			if (m_bPoison) {
				LOGINFO ("Thread poisoned");
				goto abortLoop;
			}
			EnterCriticalSection ();
			if (poRunner != m_poRunner) goto abandonLoop;
			m_bWaiting = false;
		}
		COperation *poOperation = m_poHead;
		if (!(m_poHead = poOperation->m_poNext)) {
			m_poTail = NULL;
		}
		LeaveCriticalSection ();
		LOGDEBUG ("Calling run on asynchronous operation");
		poOperation->m_nMustReschedule = -poOperation->m_nMustReschedule;
		poOperation->Run ();
		if (poOperation->m_nMustReschedule > 0) {
			if (poOperation->m_nMustReschedule >= (GetTimeoutAbortPeriod () / GetTimeoutReschedule ())) {
				LOGERROR ("Aborting operation after " << poOperation->m_nMustReschedule << " attempts");
				goto abortLoop;
			} else {
				if ((poOperation->m_nMustReschedule % (GetTimeoutInfoPeriod () / GetTimeoutReschedule ())) == 0) {
					LOGINFO ("Rescheduling operation " << poOperation->m_nMustReschedule << " times");
				} else {
					LOGDEBUG ("Rescheduling operation " << poOperation->m_nMustReschedule << " time(s)");
				}
			}
			EnterCriticalSection ();
			if (poOperation->OnScheduled ()) {
				if (!(poOperation->m_poNext = m_poHead)) {
					m_poTail = NULL;
				}
				m_poHead = poOperation;
			} else {
				LOGDEBUG (TEXT ("Operation rejected reschedule"));
				delete poOperation;
			}
			LeaveCriticalSection ();
			CThread::Sleep (GetTimeoutReschedule ());
		} else {
			LOGDEBUG ("Deleting asynchronous operation");
			delete poOperation;
		}
	}
abortLoop:
	LOGDEBUG ("Callback thread aborting");
	EnterCriticalSection ();
	// Fall through to abandonLoop
abandonLoop:
	// Already in critical section when called from above
	while (m_poHead) {
		COperation *poOperation = m_poHead;
		m_poHead = poOperation->m_poNext;
		if (poOperation->m_bVital) {
			LOGINFO (TEXT ("Running vital operation"));
			poOperation->Run ();
		} else {
			LOGDEBUG (TEXT ("Discarding operation"));
		}
		delete poOperation;
	}
	if (m_poRunner != poRunner) {
		LOGDEBUG (TEXT ("Signalling thread control semaphore"));
		m_semThread.Signal ();
		LOGINFO (TEXT ("Callback thread abandonned"));
	} else {
		CThread::Release (m_poRunner);
		m_poRunner = NULL;
		LOGINFO (TEXT ("Callback thread aborted"));
	}
	m_bWaiting = false;
	LeaveCriticalSection ();
	return;
}

bool CAsynchronous::Run (COperation *poOperation) {
	if (!poOperation) {
		LOGWARN ("NULL pointer passed");
		return false;
	}
	bool bResult = true;
	EnterCriticalSection ();
	if (!m_bPoison) {
		if (!m_poRunner) {
			m_poRunner = new CAsynchronousRunnerThread (this);
			if (!m_poRunner) {
				LOGFATAL (TEXT ("Out of memory"));
				bResult = false;
				goto abortRun;
			} else if (m_poRunner->Start ()) {
				LOGINFO (TEXT ("Callback thread created, ID ") << m_poRunner->GetThreadId ());
			} else {
				LOGFATAL (TEXT ("Couldn't create callback thread, error ") << GetLastError ());
				CThread::Release (m_poRunner);
				m_poRunner = NULL;
				bResult = false;
				goto abortRun;
			}
		}
		if (poOperation->OnScheduled ()) {
			if (m_poTail) {
				m_poTail->m_poNext = poOperation;
			} else {
				m_poHead = poOperation;
				if (m_bWaiting) {
					LOGDEBUG ("Signalling semaphore");
					if (!m_semQueue.Signal ()) {
						LOGWARN (TEXT ("Couldn't signal semaphore, error ") << GetLastError ());
					}
					// Clear the waiting flag, e.g. if there is another call to Run before the
					// dispatch thread completes its wait and clears it itself.
					m_bWaiting = false;
				}
			}
			m_poTail = poOperation;
			poOperation->m_poNext = NULL;
			poOperation->m_nMustReschedule = 0;
		} else {
			LOGDEBUG (TEXT ("Operation refused to schedule"));
			delete poOperation;
		}
	} else {
		LOGWARN ("Caller has been poisoned");
		bResult = false;
	}
abortRun:
	LeaveCriticalSection ();
	return bResult;
}

void CAsynchronous::Poison () {
	EnterCriticalSection ();
	m_bPoison = true;
	m_semQueue.Signal ();
	LeaveCriticalSection ();
}

bool CAsynchronous::RecycleThread () {
	bool bResult = false;
	EnterCriticalSection ();
	if (m_poRunner) {
		CAsynchronousRunnerThread *poNewThread = new CAsynchronousRunnerThread (this);
		if (poNewThread) {
			if (poNewThread->Start ()) {
				LOGINFO (TEXT ("Callback thread created, ID ") << poNewThread->GetThreadId ());
				CThread::Release (m_poRunner);
				m_poRunner = poNewThread;
				if (m_bWaiting) {
					LOGDEBUG (TEXT ("Signalling semaphore"));
					if (!m_semQueue.Signal ()) {
						LOGWARN (TEXT ("Couldn't signal semaphore, error ") << GetLastError ());
					}
					// Clear the waiting flag, e.g. if there is another call to Run or RecycleThread
					// before the dispatch thread completes its wait and clear it itself.
					m_bWaiting = false;
				}
				bResult = true;
			} else {
				LOGERROR (TEXT ("Couldn't create callback thread, error ") << GetLastError ());
				CThread::Release (poNewThread);
			}
		} else {
			LOGFATAL (TEXT ("Out of memory"));
		}
	} else {
		LOGWARN (TEXT ("Runner thread not active"));
	}
	LeaveCriticalSection ();
	return bResult;
}
