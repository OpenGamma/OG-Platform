/*
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

/// A single slot for coordinating a synchronous call. The slot has an identifier
/// which will be paired with the incoming message corresponding to the original
/// message. A semaphore within the slot will allow the caller to be blocked on
/// the response.
class CSynchronousCallSlot {
private:
	friend class CSynchronousCalls;

	/// Parent slot manager.
	CSynchronousCalls *m_poOwner;

	/// Received message.
	CAtomicPointer<FudgeMsg> m_msg;

	/// Internal state.
	CAtomicInt m_oState;

	/// Slot identifier.
	int m_nIdentifier;

	/// Completion semaphore.
	CSemaphore m_sem;

	/// Sequence counter. Once a slot is identified, its sequence counter must also
	/// match. This will detect late or stale messages. For example if a slot has
	/// timed out, been released and is being reused then the eventual delivery
	/// of the original message won't trigger completion of the reuse message.
	CAtomicInt m_oSequence;

	/// Reset the semaphore; forcing it to an unsignalled state.
	void ResetSemaphore () { m_sem.Wait (0); }

	/// Signal the semaphore.
	void SignalSemaphore () { m_sem.Signal (); }

	CSynchronousCallSlot (CSynchronousCalls *poOwner, fudge_i32 nIdentifier);
	~CSynchronousCallSlot ();
	void PostAndRelease (int nSequence, FudgeMsg msg);
public:

	/// Returns the slot identifier.
	///
	/// @return the identifier
	int GetIdentifier () const { return m_nIdentifier; }

	/// Returns the slot sequence number.
	///
	/// @return the sequence
	int GetSequence () const { return m_oSequence.Get (); }

	FudgeMsg GetMessage (unsigned long lTimeout);
	fudge_i32 GetHandle () const;
	void Release ();
};

/// Manages a blocking set of slots for synchronous calls.
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
