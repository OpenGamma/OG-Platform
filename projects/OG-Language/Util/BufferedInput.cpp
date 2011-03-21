/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// BufferedInput.cpp : Input buffer utility class.

#define _INTERNAL
#include "BufferedInput.h"
#include "Logging.h"

LOGGING(com.opengamma.language.util.BufferedInput);

CBufferedInput::CBufferedInput () {
	LOGDEBUG ("Creating input buffer, size " << INITIAL_BUFFER_SIZE);
	m_cbDataStart = 0;
	m_cbDataEnd = 0;
	m_pData = malloc (m_cbBuffer = INITIAL_BUFFER_SIZE);
}

CBufferedInput::~CBufferedInput () {
	LOGDEBUG ("Destroying input buffer, size " << m_cbBuffer);
	free (m_pData);
}

bool CBufferedInput::Read (CTimeoutIO *poSource, size_t cbMinimum, unsigned long timeout) {
	if (m_cbDataEnd - m_cbDataStart > cbMinimum) {
		// enough already in the buffer
		return true;
	} else {
		cbMinimum -= (m_cbDataEnd - m_cbDataStart);
	}
	size_t cbAvail = m_cbBuffer - m_cbDataEnd;
	if (cbAvail < cbMinimum) {
		LOGDEBUG (cbAvail << " bytes available, " << cbMinimum << " requested, start=" << m_cbDataStart << ", end=" << m_cbDataEnd << ", size=" << m_cbBuffer);
		if (cbAvail + m_cbDataStart >= cbMinimum) {
			// Shifting the data to the start of the buffer will be enough
			LOGDEBUG ("Shifting to start of the buffer");
			memmove (m_pData, (char*)m_pData + m_cbDataStart, m_cbDataEnd -= m_cbDataStart);
			m_cbDataStart = 0;
			cbAvail = m_cbBuffer - m_cbDataEnd;
		} else {
			// Need a bigger buffer
			LOGDEBUG ("Allocating a bigger buffer");
			size_t cbNewSize = m_cbBuffer + (cbMinimum - (cbAvail + m_cbDataStart));
			void *pNewData = malloc (cbNewSize);
			if (!pNewData) {
				LOGERROR ("Unable to alloc " << cbNewSize << " bytes for buffer");
				return false;
			}
			memcpy (pNewData, (char*)m_pData + m_cbDataStart, m_cbDataEnd -= m_cbDataStart);
			m_cbDataStart = 0;
			free (m_pData);
			m_pData = pNewData;
			m_cbBuffer = cbNewSize;
			// Buffer was resized to leave exactly the correct amount of room
			cbAvail = cbMinimum;
		}
		LOGDEBUG (cbAvail << " bytes available, " << cbMinimum << " requested, start=" << m_cbDataStart << ", end=" << m_cbDataEnd << ", size=" << m_cbBuffer);
	}
	size_t cbRead;
	while (cbMinimum > 0) {
		if ((cbRead = poSource->Read ((char*)m_pData + m_cbDataEnd, cbAvail, timeout)) == 0) {
			return false;
		}
		LOGDEBUG (cbRead << " bytes read to input buffer");
		cbAvail -= cbRead;
		m_cbDataEnd += cbRead;
		if (cbRead < cbMinimum) {
			cbMinimum -= cbRead;
		} else {
			cbMinimum = 0;
		}
	}
	return true;
}

void *CBufferedInput::GetData () {
	return (char*)m_pData + m_cbDataStart;
}

size_t CBufferedInput::GetAvailable () {
	return m_cbDataEnd - m_cbDataStart;
}

void CBufferedInput::Discard (size_t cbAmount) {
	LOGDEBUG ("Discarding " << cbAmount << " bytes from input buffer");
	m_cbDataStart += cbAmount;
	if (m_cbDataStart < m_cbDataEnd) {
		LOGDEBUG ((m_cbDataEnd - m_cbDataStart) << " bytes remaining");
	} else {
		if (m_cbDataStart > m_cbDataEnd) {
			LOGWARN ("Discarded " << cbAmount << " - " << (m_cbDataStart - m_cbDataEnd) << " bytes too much");
		}
		LOGDEBUG ("Buffer empty");
		m_cbDataStart = m_cbDataEnd = 0;
	}
}
