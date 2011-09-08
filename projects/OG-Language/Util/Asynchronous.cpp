/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

#include "Logging.h"
#include "Asynchronous.h"

/// Default value for CAsynchronous::m_lTimeoutInactivity, 30s
#define DEFAULT_TIMEOUT_INACTIVITY		 30000

/// Default value for CAsynchronous::m_lTimeoutReschedule, 1/4s
#define DEFAULT_TIMEOUT_RESCHEDULE		   250

/// Default value for CAsynchronous::m_lTimeoutInfoPeriod, 5s
#define DEFAULT_TIMEOUT_INFO_PERIOD		  5000

/// Default value for CAsynchronous::m_lTimeoutAbortPeriod, 2m
#define DEFAULT_TIMEOUT_ABORT_PERIOD	120000

LOGGING (com.opengamma.language.util.Asynchronous);

/// Creates a new operation for asynchronous callback.
///
/// @param[in] bVital true if this operation is vital and must be completed after scheduling,
///                   false if it should be discarded.
CAsynchronous::COperation::COperation (bool bVital) {
	m_bVital = bVital;
	m_poNext = NULL;
}

/// Returns the number of times the operation has been rescheduled (if any). An operation
/// implementation may use this to determine whether to reschedule itself rather than rely
/// on the timeouts.
///
/// @return zero if the operation has not been rescheduled, otherwise the number of times.
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

/// Flags the operation for rescheduling.
void CAsynchronous::COperation::MustReschedule () {
	if (m_nMustReschedule <= 0) {
		m_nMustReschedule = (-m_nMustReschedule) + 1;
	}
}

/// Creates a new asynchronous callback service object. There is initially an empty operation
/// queue and no execution thread.
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

/// Destroys the callback service object. The object should have been poisoned, released any
/// resources and cleared its operation queue. If this has not happened, messages are written
/// to the log.
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

/// Execution thread for making callbacks.
class CAsynchronousRunnerThread : public CThread {
private:

	/// Parent callback service object
	CAsynchronous *m_poCaller;

public:

	/// Creates a new execution thread.
	///
	/// @param[in] poCaller parent service object, never NULL
	CAsynchronousRunnerThread (CAsynchronous *poCaller)
	: CThread () {
		poCaller->Retain ();
		m_poCaller = poCaller;
	}

	/// Destroys the execution thread.
	~CAsynchronousRunnerThread () {
		if (m_poCaller) {
			CAsynchronous::Release (m_poCaller);
		}
	}

	/// Invokes methods on the caller to process the operation queue before
	/// terminating.
	void Run () {
		m_poCaller->MakeCallbacks (this);
		m_poCaller->OnThreadExit ();
		CAsynchronous::Release (m_poCaller);
		m_poCaller = NULL;
	}

};

/// Process the operation queue in FIFO order.
///
/// @param[in] poRunner the execution thread instance invoking this method
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
				// Abandon if thread recycling has happened
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
	m_poTail = NULL;
	if (m_poRunner != poRunner) {
		// Thread recycling has happened
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

/// Schedules an operation for later execution on the execution thread. If no thread
/// currently exists, one is started. The callback service will take ownership of
/// the operation after this call. If the return value is true, the caller may no
/// longer use the object. It will be deleted by the asynchronous framework.
///
/// @param[in] poOperation operation to schedule, never NULL
/// @return true if the operation was scheduled (or discarded), false otherwise
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

/// Requests the service terminate. The execution thread is notified and (after completing
/// any currently executing operation) will discard any pending operations from the queue
/// (executing any marked as vital) before terminating.
void CAsynchronous::Poison () {
	EnterCriticalSection ();
	m_bPoison = true;
	m_semQueue.Signal ();
	LeaveCriticalSection ();
}

/// Attempts to terminate any existing execution thread and start another in its place. Any
/// non-vital operations in the queue will be discarded in the process. Any vital operations
/// will be executed by the terminating thread before the new one proceeds.
///
/// @return true if the threads were recycled, false if there was an error or there was
///         no execution thread
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
