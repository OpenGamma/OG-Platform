/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_atomic_h
#define __inc_og_language_util_atomic_h

// Atomic operations using Win32 or APR

#ifndef _WIN32
#include <apr-1/apr_atomic.h>
#endif

class CAtomicInt {
private:
	volatile unsigned int m_nValue;
public:
	CAtomicInt (unsigned int nValue = 0) {
		m_nValue = nValue;
	}
	int DecrementAndGet () {
#ifdef _WIN32
		return InterlockedDecrement (&m_nValue);
#else
		// Note this only reliably returns 0
		return apr_atomic_dec32 (&m_nValue);
#endif
	}
	int IncrementAndGet () {
#ifdef _WIN32
		return InterlockedIncrement (&m_nValue);
#else
		return apr_atomic_inc32 (&m_nValue) + 1;
#endif
	}
	int Get () {
#ifdef _WIN32
		return m_nValue;
#else
		return apr_atomic_read32 (&m_nValue);
#endif
	}
	void Set (int nValue) {
#ifdef _WIN32
		m_nValue = nValue;
#else
		apr_atomic_set32 (&m_nValue, nValue);
#endif
	}
};

#endif /* ifndef __inc_og_language_util_atomic_h */