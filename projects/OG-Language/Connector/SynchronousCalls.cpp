/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Blocking slots for synchronous calls

#include "SynchronousCalls.h"

LOGGING (com.opengamma.language.connector.SynchronousCalls);

#define NULL_FUDGE_MSG		((FudgeMsg)0)
#define BUSY_FUDGE_MSG		((FudgeMsg)-1)
#define SLOT_INCREMENT	8

CSynchronousCallSlot::CSynchronousCallSlot (CSynchronousCalls *poOwner, fudge_i32 nIdentifier)
: m_sem (0, 1), m_oSequence (1) {
	LOGDEBUG (TEXT ("Created call slot ") << nIdentifier);
	m_poOwner = poOwner;
	m_nIdentifier = nIdentifier;
}

CSynchronousCallSlot::~CSynchronousCallSlot () {
	FudgeMsg msg = m_msg.Get ();
	if ((msg != NULL_FUDGE_MSG) && (msg != BUSY_FUDGE_MSG)) {
		LOGDEBUG (TEXT ("Releasing message in deleted call slot ") << m_nIdentifier);
		FudgeMsg_release (msg);
	}
}

// Handles are created as the identifier combined with a sequence. Lower identifier numbers allow more bits for sequence
// values allowing better detection of messaging errors. The high order bits are a header that indicates which are which

fudge_i32 CSynchronousCallSlot::GetHandle () {
	if (m_nIdentifier >= 0) {
		int nSequence = m_oSequence.Get ();
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

void CSynchronousCallSlot::Release () {
	m_oSequence.IncrementAndGet ();
	// Anything new that arrives will not match the sequence number
	FudgeMsg msg = m_msg.GetAndSet (NULL_FUDGE_MSG);
	// Clear out anything that arrived before we incremented the sequence
	if ((msg != NULL_FUDGE_MSG) && (msg != BUSY_FUDGE_MSG)) {
		LOGDEBUG (TEXT ("Releasing message in released call slot ") << m_nIdentifier);
		FudgeMsg_release (msg);
	}
	m_poOwner->Release (this);
}

void CSynchronousCallSlot::PostAndRelease (FudgeMsg msg) {
	msg = m_msg.GetAndSet (msg);
	if (msg == NULL_FUDGE_MSG) {
		// First message received
		LOGDEBUG (TEXT ("Signalling semaphore on slot ") << m_nIdentifier);
		m_sem.Signal ();
	} else if (msg == BUSY_FUDGE_MSG) {
		// This can happen if the other end sends duplicates
		LOGWARN (TEXT ("Message already delivered on slot ") << m_nIdentifier);
		msg = m_msg.GetAndSet (BUSY_FUDGE_MSG);
		if (msg == NULL_FUDGE_MSG) {
			// Slot has been released, we may lose data
retrySetNull:
			msg = m_msg.GetAndSet (NULL_FUDGE_MSG);
			if ((msg != NULL_FUDGE_MSG) && (msg != BUSY_FUDGE_MSG)) {
				LOGWARN (TEXT ("Message discarded on slot ") << m_nIdentifier << TEXT (" recovering from duplicate delivery error"));
				FudgeMsg_release (msg);
				goto retrySetNull;
			}
		} else if (msg == BUSY_FUDGE_MSG) {
			// No further action
		} else {
			// Hopefully this was our original message
			FudgeMsg_release (msg);
		}
	} else {
		// This can happen if the other end sends duplicates
		LOGDEBUG (TEXT ("Message already received on slot ") << m_nIdentifier);
		FudgeMsg_release (msg);
	}
}

FudgeMsg CSynchronousCallSlot::GetMessage (unsigned long lTimeout) {
	LOGDEBUG (TEXT ("Waiting on semaphore on slot ") << m_nIdentifier);
	if (m_sem.Wait (lTimeout)) {
		LOGDEBUG (TEXT ("Semaphore signalled"));
	} else {
		LOGDEBUG (TEXT ("Timeout elapsed on semaphore"));
	}
	FudgeMsg msg = m_msg.GetAndSet (BUSY_FUDGE_MSG);
	if (msg == BUSY_FUDGE_MSG) {
		LOGWARN (TEXT ("Duplicate call to GetMessage"));
		msg = NULL_FUDGE_MSG;
	}
	return msg;
}

CSynchronousCalls::CSynchronousCalls () {
	m_ppoSlots = new CSynchronousCallSlot*[m_nAllocatedSlots = SLOT_INCREMENT];
	m_ppoFreeSlots = new CSynchronousCallSlot*[m_nMaxFreeSlots = m_nFreeSlots = m_nAllocatedSlots];
	int i;
	for (i = 0; i < m_nFreeSlots; i++) {
		m_ppoFreeSlots[i] = m_ppoSlots[i] = new CSynchronousCallSlot (this, i);
	}
}

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

void CSynchronousCalls::ClearAllSemaphores () {
	int i;
	for (i = 0; i < m_nAllocatedSlots; i++) {
		m_ppoSlots[i]->ResetSemaphore ();
	}
}

void CSynchronousCalls::SignalAllSemaphores () {
	int i;
	for (i = 0; i < m_nAllocatedSlots; i++) {
		m_ppoSlots[i]->SignalSemaphore ();
	}
}

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
				m_ppoFreeSlots[m_nFreeSlots++] = ppoSlots[m_nAllocatedSlots++] = new CSynchronousCallSlot (this, m_nAllocatedSlots);
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

void CSynchronousCalls::PostAndRelease (fudge_i32 nHandle, FudgeMsg msg) {
	int nIdentifier, nSequence;
	if (nHandle & 0x80000000) {
		// 20-bit identifier, 11-bit sequence
		nIdentifier = (nHandle >> 11) & 0xFFFFF;
		nSequence = nHandle & 0x7FF;
	} else if (nHandle & 0x40000000) {
		// 16-bit identifier, 14-bit sequence
		nIdentifier = (nHandle >> 14) & 0xFFFF;
		nSequence = nHandle & 0x3FFF;
	} else if (nHandle & 0x20000000) {
		// 10-bit identifier, 19-bit sequence
		nIdentifier = (nHandle >> 19) & 0x3FF;
		nSequence = nHandle & 0x7FFFF;
	} else {
		FudgeMsg_release (msg);
		LOGFATAL (TEXT ("Bad handle, ") << nHandle);
		assert (0);
		return;
	}
	m_mutex.Enter ();
	if ((nIdentifier >= 0) && (nIdentifier < m_nAllocatedSlots)) {
		int nExpected = m_ppoSlots[nIdentifier]->GetSequence ();
		if (nExpected == nSequence) {
			LOGDEBUG (TEXT ("Delivering message ") << nSequence << TEXT (" to slot ") << nIdentifier);
			m_ppoSlots[nIdentifier]->PostAndRelease (msg);
		} else {
			LOGWARN (TEXT ("Invalid sequence ") << nSequence << TEXT (" on slot ") << nIdentifier << TEXT (", expected ") << nExpected);
			FudgeMsg_release (msg);
		}
	} else {
		LOGWARN (TEXT ("Invalid handle ") << nHandle << TEXT (", identifier ") << nIdentifier << TEXT (" out of range"));
		FudgeMsg_release (msg);
	}
	m_mutex.Leave ();
}