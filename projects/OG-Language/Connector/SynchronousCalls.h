/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_synchronouscalls_h
#define __inc_og_language_connector_synchronouscalls_h

// Blocking slots for synchronous calls

#include <Util/Atomic.h>
#include <Util/Mutex.h>
#include <Util/Semaphore.h>

class CSynchronousCalls;

class CSynchronousCallSlot {
private:
	CSynchronousCalls *m_poOwner;
	CAtomicPointer<FudgeMsg> m_msg;
	CAtomicInt m_oState;
	int m_nIdentifier;
	CSemaphore m_sem;
	CAtomicInt m_oSequence;
	friend class CSynchronousCalls;
	CSynchronousCallSlot (CSynchronousCalls *poOwner, int nIdentifier);
	~CSynchronousCallSlot ();
	void ResetSemaphore () { m_sem.Wait (0); }
	void SignalSemaphore () { m_sem.Signal (); }
	void PostAndRelease (int nSequence, FudgeMsg msg);
public:
	fudge_i32 GetHandle () const;
	int GetIdentifier () const { return m_nIdentifier; }
	int GetSequence () const { return m_oSequence.Get (); }
	FudgeMsg GetMessage (unsigned long lTimeout);
	void Release ();
};

class CSynchronousCalls {
private:
	friend class CSynchronousCallSlot;
	CMutex m_mutex;
	CSynchronousCallSlot **m_ppoSlots;
	int m_nAllocatedSlots;
	CSynchronousCallSlot **m_ppoFreeSlots;
	int m_nFreeSlots;
	int m_nMaxFreeSlots;
	void Release (CSynchronousCallSlot *poSlot);
public:
	CSynchronousCalls ();
	~CSynchronousCalls ();
	void ClearAllSemaphores ();
	void SignalAllSemaphores ();
	CSynchronousCallSlot *Acquire ();
	void PostAndRelease (fudge_i32 handle, FudgeMsg msg);
};

#endif /* ifndef __inc_og_language_connector_synchronouscalls_h */
