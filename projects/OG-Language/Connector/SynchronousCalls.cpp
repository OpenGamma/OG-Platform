/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "SynchronousCalls.h"
#include <Util/Thread.h>
#include <Util/Error.h>

LOGGING (com.opengamma.language.connector.SynchronousCalls);

#define SLOT_INCREMENT	8

#define STATE_SEQUENCE_MASK	0x0FFFFFFF
#define STATE_STATE_MASK	0xF0000000
#define STATE_IDLE			0x10000000
#define STATE_MESSAGE_PRE	0x20000000
#define STATE_MESSAGE_OK	0x30000000
#define STATE_WAITING		0x40000000
#define STATE_DONE			0x50000000

/// Creates a new call slot.
///
/// @param[in] poOwner parent slot manager
/// @param[in] nIdentifier slot identifier
CSynchronousCallSlot::CSynchronousCallSlot (CSynchronousCalls *poOwner, fudge_i32 nIdentifier)
: m_oState (STATE_IDLE | 1), m_sem (0, 1), m_oSequence (1) {
	LOGDEBUG (TEXT ("Created call slot ") << nIdentifier);
	m_poOwner = poOwner;
	m_nIdentifier = nIdentifier;
}

/// Destroys a call slot, releasing any resources.
CSynchronousCallSlot::~CSynchronousCallSlot () {
	FudgeMsg msg = m_msg.Get ();
	if (msg) {
		LOGDEBUG (TEXT ("Releasing message in deleted call slot ") << m_nIdentifier);
		FudgeMsg_release (msg);
	}
}

/// Returns the current handle to use for the slot. Handles are a composition of the identifier and sequence counter. Lower
/// identifier numbers allow more bits for sequence values allowing better detection of messaging errors. The high order
/// bits are a header that indicates the bit lengths used.
///
/// @return the current handle
fudge_i32 CSynchronousCallSlot::GetHandle () const {
	if (m_nIdentifier >= 0) {
		int nSequence = m_oSequence.Get ();
#ifdef _DEBUG
		if (!(nSequence & 0x07FF)) {
			LOGINFO (TEXT ("Sequence ") << nSequence << TEXT (" on slot ") << m_nIdentifier);
		}
#endif /* ifdef _DEBUG */
		if (m_nIdentifier < 0x400) { // 10-bit identifiers, 19-bit sequences
			return 0x20000000 | (m_nIdentifier << 19) | (nSequence & 0x7FFFF);
		} else if (m_nIdentifier < 0x10000) { // 16-bit identifiers, 14-bit sequences
			return 0x40000000 | (m_nIdentifier << 14) | (nSequence & 0x3FFF);
		} else if (m_nIdentifier < 0x100000) { // 20-bit identifiers, 11-bit sequences
			return 0x80000000 | (m_nIdentifier << 20) | (nSequence & 0x7FF);
		} else {
			LOGFATAL (TEXT ("Identifier too large, ") << m_nIdentifier);
			assert (0);
		}
	} else {
		LOGFATAL (TEXT ("Identifier negative, ") << m_nIdentifier);
		assert (0);
	}
	return 0;
}

/// Releases the slot back to its parent. This may be called at the same time as a PostAndRelease
/// from a message receiving thread. This must not be called at the same time as GetMessage.
void CSynchronousCallSlot::Release () {
	int nSequence = m_oSequence.IncrementAndGet () & STATE_SEQUENCE_MASK;
	int nState = m_oState.Get (), nAltState;
	FudgeMsg msg;
retry:
	switch (nState & STATE_STATE_MASK) {
		case STATE_IDLE :
			nAltState = m_oState.CompareAndSet (STATE_IDLE | nSequence, nState);
			if (nAltState != nState) {
				LOGDEBUG (TEXT ("Retrying after state shift from ") << nState << TEXT (" to ") << nAltState);
				nState = nAltState;
				goto retry;
			}
			break;
		case STATE_MESSAGE_PRE :
			// A message is being posted; need to wait for the post to complete
			LOGDEBUG (TEXT ("Retrying during call to PostAndRelease on slot ") << m_nIdentifier);
			CThread::Yield ();
			nState = m_oState.Get ();
			goto retry;
		case STATE_MESSAGE_OK :
			// Message has been posted, but not consumed discard it
			msg = m_msg.GetAndSet (NULL);
			if (msg) {
				LOGDEBUG (TEXT ("Discarding message in released slot ") << m_nIdentifier);
				FudgeMsg_release (msg);
			}
			m_oState.Set (STATE_IDLE | nSequence);
			break;
		// STATE_WAITING cannot happen as calls to Release and GetMessage are mutually exclusive for a thread
		case STATE_DONE :
			m_oState.Set (STATE_IDLE | nSequence);
			break;
		default :
			LOGFATAL (TEXT ("Invalid state ") << nState);
			assert (0);
			break;
	}
	m_poOwner->Release (this);
}

/// Posts a message to the slot and releases a caller to GetMessage if there is one. Only one
/// thread may call this at any one time.
///
/// @param[in] nSequence sequence counter decoded from the message
/// @param[in] msg message payload
void CSynchronousCallSlot::PostAndRelease (int nSequence, FudgeMsg msg) {
	nSequence &= STATE_SEQUENCE_MASK;
	int nState = m_oState.Get (), nAltState;
retry:
	if ((nState & STATE_SEQUENCE_MASK) != nSequence) {
		LOGDEBUG (TEXT ("Sequence on slot ") << m_nIdentifier << TEXT (" already advanced"));
		FudgeMsg_release (msg);
		return;
	}
	switch (nState & STATE_STATE_MASK) {
		case STATE_IDLE :
			nAltState = m_oState.CompareAndSet (STATE_MESSAGE_PRE | nSequence, nState);
			if (nAltState != nState) {
				LOGDEBUG (TEXT ("Retrying after state shift from ") << nState << TEXT (" to ") << nAltState << TEXT (" on slot ") << m_nIdentifier);
				nState = nAltState;
				goto retry;
			}
			// Now in MESSAGE_PRE state, ready to store message
			msg = m_msg.GetAndSet (msg);
			if (msg) {
				LOGDEBUG (TEXT ("Discarding message already in slot ") << m_nIdentifier);
				FudgeMsg_release (msg);
			}
			// Now advance to MESSAGE_OK state, to indicate message is in the slot
			m_oState.Set (STATE_MESSAGE_OK | nSequence);
			break;
		// STATE_MESSAGE_PRE cannot happen as only one thread should ever call PostAndRelease
		case STATE_MESSAGE_OK :
			LOGWARN (TEXT ("Discarding duplicate message received on slot ") << m_nIdentifier);
			FudgeMsg_release (msg);
			break;
		case STATE_WAITING :
			// Message received with another thread waiting for it
			nAltState = m_oState.CompareAndSet (STATE_MESSAGE_PRE | nSequence, nState);
			if (nAltState != nState) {
				LOGDEBUG (TEXT ("Retrying after state shift from ") << nState << TEXT (" to ") << nAltState << TEXT (" on slot ") << m_nIdentifier);
				nState = nAltState;
				goto retry;
			}
			// Now in MESSAGE_PRE state, ready to store message
			msg = m_msg.GetAndSet (msg);
			if (msg) {
				LOGDEBUG (TEXT ("Discarding message already in slot ") << m_nIdentifier);
				FudgeMsg_release (msg);
			}
			// Now advance to MESSAGE_OK state, to indicate message is in the slot
			m_oState.Set (STATE_MESSAGE_OK | nSequence);
			// There was another thread waiting on the semaphore
			LOGDEBUG (TEXT ("Signalling semaphore on slot ") << m_nIdentifier);
			m_sem.Signal ();
			break;
		case STATE_DONE :
			LOGWARN (TEXT ("Discarding late delivery of message on slot ") << m_nIdentifier);
			FudgeMsg_release (msg);
			break;
		default :
			LOGFATAL (TEXT ("Invalid state ") << nState << TEXT (" on slot ") << m_nIdentifier);
			assert (0);
			FudgeMsg_release (msg);
			break;
	}
}

/// Blocks the caller until a message is received (posted to the slot). This must not be called
/// at the same time as Release.
///
/// @param[in] lTimeout maximum time to wait for a message in milliseconds
/// @return the message received or NULL if there was a problem (e.g. timeout elapsed)
FudgeMsg CSynchronousCallSlot::GetMessage (unsigned long lTimeout) {
	int nState = m_oState.Get (), nAltState;
	int nSequence = nState & STATE_SEQUENCE_MASK;
	FudgeMsg msg = NULL;
retry:
	switch (nState & STATE_STATE_MASK) {
		case STATE_IDLE :
			nAltState = m_oState.CompareAndSet (STATE_WAITING | nSequence, nState);
			if (nAltState != nState) {
				LOGDEBUG (TEXT ("Retrying after state shift from ") << nState << TEXT (" to ") << nAltState << TEXT (" on slot ") << m_nIdentifier);
				nState = nAltState;
				goto retry;
			}
			LOGDEBUG (TEXT ("Waiting on semaphore on slot ") << m_nIdentifier);
			if (m_sem.Wait (lTimeout)) {
				LOGDEBUG (TEXT ("Semaphore signalled on slot ") << m_nIdentifier);
				msg = m_msg.GetAndSet (NULL);
				m_oState.Set (STATE_DONE | nSequence);
			} else {
				LOGDEBUG (TEXT ("Timeout ") << lTimeout << TEXT ("ms elapsed on semaphore for slot ") << m_nIdentifier);
				nState = STATE_WAITING | nSequence;
				nAltState = m_oState.CompareAndSet (STATE_IDLE | nSequence, nState);
				if (nAltState == nState) {
					// We timed out and reverted to the IDLE state
					SetLastError (ETIMEDOUT);
					break;
				}
				// We timed out, but the state was changed from WAITING which indicates delivery
				// and the semaphore has been (or is about to be) signalled.
				LOGDEBUG (TEXT ("Clearing late message arrival on slot ") << m_nIdentifier);
				if (!m_sem.Wait (lTimeout)) {
					LOGERROR (TEXT ("Semaphore was not signalled during post"));
					// assert (0);
				}
				nState = nAltState;
				goto retry;
			}
			break;
		case STATE_MESSAGE_PRE :
			// Message is being posted; need to wait for it to complete
			LOGDEBUG (TEXT ("Retrying during call to PostAndRelease on slot ") << m_nIdentifier);
			CThread::Yield ();
			nState = m_oState.Get ();
			goto retry;
		case STATE_MESSAGE_OK :
			// Message has been posted; accept it and move to the DONE state
			LOGDEBUG (TEXT ("Message delivered on slot ") << m_nIdentifier);
			msg = m_msg.GetAndSet (NULL);
			m_oState.Set (STATE_DONE | nSequence);
			break;
		// STATE_WAITING cannot happen as only one thread should call GetMessage
		case STATE_DONE :
			LOGWARN (TEXT ("Duplicate call to GetMessage"));
			SetLastError (EINVAL);
			break;
		default :
			LOGFATAL (TEXT ("Invalid state ") << nState << TEXT (" on slot ") << m_nIdentifier);
			assert (0);
			break;
	}
	return msg;
}

/// Creates a new synchronous call manager.
CSynchronousCalls::CSynchronousCalls () {
	m_ppoSlots = new CSynchronousCallSlot*[m_nAllocatedSlots = SLOT_INCREMENT];
	m_ppoFreeSlots = new CSynchronousCallSlot*[m_nMaxFreeSlots = m_nFreeSlots = m_nAllocatedSlots];
	int i;
	for (i = 0; i < m_nFreeSlots; i++) {
		m_ppoFreeSlots[i] = m_ppoSlots[i] = new CSynchronousCallSlot (this, i);
	}
}

/// Destroys a synchronous call manager.
CSynchronousCalls::~CSynchronousCalls () {
	if (m_nFreeSlots != m_nAllocatedSlots) {
		LOGFATAL (TEXT ("Not all slots released at destruction (") << (m_nAllocatedSlots - m_nFreeSlots) << TEXT (" outstanding"));
		assert (0);
	}
	int i;
	for (i = 0; i < m_nAllocatedSlots; i++) {
		delete m_ppoSlots[i];
	}
	delete m_ppoSlots;
	delete m_ppoFreeSlots;
}

/// Clears all slot semaphores. This is necessary if all slots are about to be reused
/// and some may have had undelivered messages in them.
void CSynchronousCalls::ClearAllSemaphores () {
	int i;
	for (i = 0; i < m_nAllocatedSlots; i++) {
		m_ppoSlots[i]->ResetSemaphore ();
	}
}

/// Signals all slot semaphores. This is necessary to unblock all callers in the event
/// of a serious communication failure and they will never receive their messages. Note
/// that some slots may or may not have had callers waiting on them so the slots may
/// end up in a mix of signalled and unsignalled states afterwards. One should either
/// follow this up with a ClearAllSemaphores, or discard the whole object and start again.
void CSynchronousCalls::SignalAllSemaphores () {
	int i;
	for (i = 0; i < m_nAllocatedSlots; i++) {
		m_ppoSlots[i]->SignalSemaphore ();
	}
}

/// Returns a call slot to the free list for re-use later.
///
/// @param[in] poSlot slot to return, never NULL
void CSynchronousCalls::Release (CSynchronousCallSlot *poSlot) {
	m_mutex.Enter ();
	if (m_nFreeSlots >= m_nMaxFreeSlots) {
		LOGDEBUG (TEXT ("Enlarging free slot list"));
		CSynchronousCallSlot **ppoFreeSlots = new CSynchronousCallSlot*[m_nMaxFreeSlots + SLOT_INCREMENT];
		if (ppoFreeSlots) {
			memcpy (ppoFreeSlots, m_ppoFreeSlots, sizeof (CSynchronousCallSlot*) * m_nMaxFreeSlots);
			delete m_ppoFreeSlots;
			ppoFreeSlots[m_nMaxFreeSlots] = poSlot;
			m_ppoFreeSlots = ppoFreeSlots;
			m_nFreeSlots = m_nMaxFreeSlots + 1;
			m_nMaxFreeSlots += SLOT_INCREMENT;
		} else {
			LOGFATAL (TEXT ("Out of memory"));
		}
	} else {
		m_ppoFreeSlots[m_nFreeSlots++] = poSlot;
	}
	m_mutex.Leave ();
}

/// Acquires a call slot. If there is one in the free list it is taken. If all slots
/// are already out and in use, a new one is created.
///
/// @return a call slot
CSynchronousCallSlot *CSynchronousCalls::Acquire () {
	CSynchronousCallSlot *poSlot;
	m_mutex.Enter ();
	if (m_nFreeSlots == 0) {
		CSynchronousCallSlot **ppoSlots = new CSynchronousCallSlot*[m_nAllocatedSlots + SLOT_INCREMENT];
		if (ppoSlots) {
			memcpy (ppoSlots, m_ppoSlots, sizeof (CSynchronousCallSlot*) * m_nAllocatedSlots);
			delete m_ppoSlots;
			// There will be at least SLOT_INCREMENT slots in the free list
			for (int i = 0; i < SLOT_INCREMENT; i++) {
				m_ppoFreeSlots[m_nFreeSlots++] = ppoSlots[m_nAllocatedSlots] = new CSynchronousCallSlot (this, m_nAllocatedSlots);
				m_nAllocatedSlots++;
			}
			m_ppoSlots = ppoSlots;
		} else {
			LOGFATAL (TEXT ("Out of memory"));
		}
	}
	poSlot = m_ppoFreeSlots[--m_nFreeSlots];
	m_mutex.Leave ();
	return poSlot;
}

// TODO: the slots don't have to be pointers as they don't move in the array once allocated; they should be inline fragments

/// Posts a message to the call slot identified by the handle and releases any caller blocked on
/// that slot. If the sequence number is invalid, the slot will not accept the message.
///
/// @param[in] nHandle message correlation handle; identifies the slot and slot sequence
/// @param[in] msg message to deliver
void CSynchronousCalls::PostAndRelease (fudge_i32 nHandle, FudgeMsg msg) {
	int nIdentifier, nMessageSequence, nMessageSequenceMask;
	if (nHandle & 0x80000000) {
		// 20-bit identifier, 11-bit sequence
		nIdentifier = (nHandle >> 11) & 0xFFFFF;
		nMessageSequence = nHandle & (nMessageSequenceMask = 0x7FF);
	} else if (nHandle & 0x40000000) {
		// 16-bit identifier, 14-bit sequence
		nIdentifier = (nHandle >> 14) & 0xFFFF;
		nMessageSequence = nHandle & (nMessageSequenceMask = 0x3FFF);
	} else if (nHandle & 0x20000000) {
		// 10-bit identifier, 19-bit sequence
		nIdentifier = (nHandle >> 19) & 0x3FF;
		nMessageSequence = nHandle & (nMessageSequenceMask = 0x7FFFF);
	} else {
		FudgeMsg_release (msg);
		LOGFATAL (TEXT ("Bad handle, ") << nHandle);
		assert (0);
		return;
	}
	m_mutex.Enter ();
	if ((nIdentifier >= 0) && (nIdentifier < m_nAllocatedSlots)) {
		int nSlotSequence = m_ppoSlots[nIdentifier]->GetSequence ();
		int nExpectedMessageSequence = nSlotSequence & nMessageSequenceMask;
		if (nExpectedMessageSequence == nMessageSequence) {
			LOGDEBUG (TEXT ("Delivering message ") << nSlotSequence << TEXT (" (transport sequence ") << nMessageSequence << TEXT (") to slot ") << nIdentifier);
			m_ppoSlots[nIdentifier]->PostAndRelease (nSlotSequence, msg);
		} else {
			LOGWARN (TEXT ("Invalid sequence ") << nMessageSequence << TEXT (" on slot ") << nIdentifier << TEXT (", expected ") << nExpectedMessageSequence);
			FudgeMsg_release (msg);
		}
	} else {
		LOGWARN (TEXT ("Invalid handle ") << nHandle << TEXT (", identifier ") << nIdentifier << TEXT (" out of range"));
		FudgeMsg_release (msg);
	}
	m_mutex.Leave ();
}
