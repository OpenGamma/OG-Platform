/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_bufferedinput_h
#define __inc_og_language_util_bufferedinput_h

#include "TimeoutIO.h"

/// Reads from a CTimeoutIO data source, making sure that at least a minimum count of bytes
/// is available to the caller.
class CBufferedInput {
private:

	/// Size of the buffer pointed to by m_pData
	size_t m_cbBuffer;

	/// Index of the first byte used in the buffer.
	size_t m_cbDataStart;

	/// Index of the last byte used in the buffer.
	size_t m_cbDataEnd;

	/// Data buffer
	void *m_pData;

public:
	CBufferedInput ();
	~CBufferedInput ();
	bool Read (CTimeoutIO *poSource, size_t cbMinimum, unsigned long timeout);
	const void *GetData () const;
	size_t GetAvailable () const;
	void Discard (size_t cbAmount);
};

#ifdef _INTERNAL
#define INITIAL_BUFFER_SIZE	512
#undef _INTERNAL
#endif /* ifdef _INTERNAL */

#endif /* ifndef __inc_og_language_util_bufferedinput_h */
