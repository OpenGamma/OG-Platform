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
#define INVALID_FUDGE_MSG	((FudgeMsg)-1)

CSynchronousCallSlot::CSynchronousCallSlot (fudge_i32 nIdentifier) {
	m_nIdentifier = nIdentifier;
	m_nSequence = 0;
}

CSynchronousCallSlot::~CSynchronousCallSlot () {
	FudgeMsg msg = m_msg.Get ();
	if ((msg != NULL_FUDGE_MSG) && (msg != INVALID_FUDGE_MSG)) {
		LOGDEBUG (TEXT ("Releasing message in deleted call slot"));
		FudgeMsg_release (msg);
	}
}

// Handles are created as the identifier combined with a sequence. Lower identifier numbers allow more bits for sequence
// values allowing better detection of messaging errors. The high order bits are a header that indicates which are which

static void _IdentifierShiftSequenceMask (fudge_i32 nIdentifier, int *nIdentifierShift, fudge_i32 *nSequenceMask, fudge_i32 *nHeader) {
	if (nIdentifier >= 0) {
		if (nIdentifier < 0x400) { // 10-bit identifiers, 19-bit sequences
			*nIdentifierShift = 19;
			*nSequenceMask = 0x7FFFF;
			*nHeader = 0x20000000;
		} else if (nIdentifier < 0x10000) { // 16-bit identifiers, 14-bit sequences
			*nIdentifierShift = 16;
			*nSequenceMask = 0xFFFF;
			*nHeader = 0x40000000;
		} else if (nIdentifier < 0x100000) { // 20-bit identifiers, 11-bit sequences
			*nIdentifierShift = 20;
			*nSequenceMask = 0x7FF;
			*nHeader = 0x80000000;
		} else {
			LOGFATAL (TEXT ("Identifier too large, ") << nIdentifier);
			assert (0);
		}
	} else {
		LOGFATAL (TEXT ("Identifier negative, ") << nIdentifier);
		assert (0);
	}
}

fudge_i32 CSynchronousCallSlot::GetHandle () {
	int nShift;
	fudge_i32 nSequenceMask;
	fudge_i32 nHandle;
	_IdentifierShiftSequenceMask (m_nIdentifier, &nShift, &nSequenceMask, &nHandle);
	return nHandle | (m_nIdentifier << nShift) | (m_nSequence & nSequenceMask);
}

FudgeMsg CSynchronousCallSlot::GetMessage () {
	TODO (__FUNCTION__);
	return NULL_FUDGE_MSG;
}

void CSynchronousCallSlot::InvalidateMessage () {
	TODO (__FUNCTION__);
}

void CSynchronousCallSlot::Release () {
	TODO (__FUNCTION__);
}

CSynchronousCalls::CSynchronousCalls () {
	TODO (__FUNCTION__);
}

CSynchronousCalls::~CSynchronousCalls () {
	TODO (__FUNCTION__);
}

void CSynchronousCalls::ClearAllSemaphores () {
	TODO (__FUNCTION__);
}

void CSynchronousCalls::SignalAllSemaphores () {
	TODO (__FUNCTION__);
}

CSynchronousCallSlot *CSynchronousCalls::Acquire () {
	TODO (__FUNCTION__);
}
