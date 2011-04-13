/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_asynchronous_h
#define __inc_og_language_asynchronous_h

// Base class for an asynchronous callback service

#include "Semaphore.h"
#include "Mutex.h"
#include "Thread.h"

class CAsynchronous {
public:
	class COperation : public IRunnable {
	private:
		bool m_bVital;
		int m_nMustReschedule;
		COperation *m_poNext;
		friend class CAsynchronous;
	protected:
		COperation (bool bVital = false);
		int WasRescheduled ();
		void MustReschedule ();
		// OnScheduled is called after the operation is added to a queue, while the
		// critical section is still held, and before the operation can be run. Do
		// not schedule further operations or a deadlock will occur! Return FALSE to
		// cancel the scheduling.
		virtual bool OnScheduled () { return true; }
	public:
		~COperation () { }
	};
private:
	CAtomicInt m_oRefCount;
	CMutex m_mutex;
	COperation *m_poHead;
	COperation *m_poTail;
	CThread *m_poRunner;
	bool m_bWaiting;
	CSemaphore m_semQueue;
	CSemaphore m_semThread;
	bool m_bPoison;
	long m_lTimeoutInactivity;
	long m_lTimeoutReschedule;
	long m_lTimeoutInfoPeriod;
	long m_lTimeoutAbortPeriod;
	void MakeCallbacks (CThread *poRunner);
	friend class CAsynchronousRunnerThread;
protected:
	CAsynchronous ();
	virtual ~CAsynchronous ();
	virtual void OnThreadExit () { }
	void EnterCriticalSection () { m_mutex.Enter (); }
	void LeaveCriticalSection () { m_mutex.Leave (); }
public:
	static CAsynchronous *Create () { return new CAsynchronous (); }
	// Returns true if the operation was accepted, and delete will at some point be called on the
	// operation.
	bool Run (COperation *poOperation);
	virtual void Poison ();
	void Retain () { m_oRefCount.IncrementAndGet (); }
	static void Release (CAsynchronous *poCaller) { if (!poCaller->m_oRefCount.DecrementAndGet ()) delete poCaller; }
	static void PoisonAndRelease (CAsynchronous *poCaller) { poCaller->Poison (); Release (poCaller); }
	long GetTimeoutInactivity () { return m_lTimeoutInactivity; }
	void SetTimeoutInactivity (long lTimeoutInactivity) { m_lTimeoutInactivity = lTimeoutInactivity; }
	long GetTimeoutReschedule () { return m_lTimeoutReschedule; }
	void SetTimeoutReschedule (long lTimeoutReschedule) { m_lTimeoutReschedule = lTimeoutReschedule; }
	long GetTimeoutInfoPeriod () { return m_lTimeoutInfoPeriod; }
	void SetTimeoutInfoPeriod (long lTimeoutInfoPeriod) { m_lTimeoutInfoPeriod = lTimeoutInfoPeriod; }
	long GetTimeoutAbortPeriod () { return m_lTimeoutAbortPeriod; }
	void SetTimeoutAbortPeriod (long lTimeoutAbortPeriod) { m_lTimeoutAbortPeriod = lTimeoutAbortPeriod; }
	bool RecycleThread ();
};

#endif /* ifndef __inc_og_language_asynchronous_h */
