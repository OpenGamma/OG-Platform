/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_bufferedinput_h
#define __inc_og_language_util_bufferedinput_h

#include "TimeoutIO.h"

class CBufferedInput {
private:
	size_t m_cbBuffer;
	size_t m_cbDataStart;
	size_t m_cbDataEnd;
	void *m_pData;
public:
	CBufferedInput ();
	~CBufferedInput ();
	bool Read (CTimeoutIO *poSource, size_t cbMinimum, unsigned long timeout);
	void *GetData ();
	size_t GetAvailable ();
	void Discard (size_t cbAmount);
};

#ifdef _INTERNAL
#define INITIAL_BUFFER_SIZE	512
#undef _INTERNAL
#endif /* ifdef _INTERNAL */

#endif /* ifndef __inc_og_language_util_bufferedinput_h */
