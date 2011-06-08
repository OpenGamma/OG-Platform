/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

#define _INTERNAL
#include "BufferedInput.h"
#include "Logging.h"

LOGGING(com.opengamma.language.util.BufferedInput);

/// Creates a new reader object
CBufferedInput::CBufferedInput () {
	LOGDEBUG ("Creating input buffer, size " << INITIAL_BUFFER_SIZE);
	m_cbDataStart = 0;
	m_cbDataEnd = 0;
	m_pData = malloc (m_cbBuffer = INITIAL_BUFFER_SIZE);
}

/// Destroys the reader object, releasing memory used for the buffer.
CBufferedInput::~CBufferedInput () {
	LOGDEBUG ("Destroying input buffer, size " << m_cbBuffer);
	free (m_pData);
}

/// Reads the next amount of available data into the buffer.
///
/// @param[in] poSource source of data to read
/// @param[in] cbMinimum minimum number of bytes to have in the buffer
/// @param[in] timeout maximum timeout, in milliseconds, to wait for data
/// @return true if at least cbMinimum bytes are available in the buffer
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

/// Returns the data available in the buffer, up to GetAvailable bytes will be present
/// from this address onwards. Content beyond or before the address is undefined.
///
/// @return address of the first available byte
const void *CBufferedInput::GetData () const {
	return (char*)m_pData + m_cbDataStart;
}

/// Returns the number of bytes currently available in the buffer.
///
/// @return the number of bytes available
size_t CBufferedInput::GetAvailable () const {
	return m_cbDataEnd - m_cbDataStart;
}

/// Discards a number of bytes from the buffer.
///
/// @param[in] cbAmount number of bytes to discard
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
