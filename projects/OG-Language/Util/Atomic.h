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

template <typename PTYPE> class CAtomicPointer {
private:
	PTYPE volatile m_pValue;
public:
	CAtomicPointer (PTYPE pValue = NULL) {
		m_pValue = pValue;
	}
	PTYPE GetAndSet (PTYPE pNewValue) {
#ifdef _WIN32
		return InterlockedExchangePointer ((void * volatile *)&m_pValue, (void*)pNewValue);
#else
		// Is the signature for the method wrong; shouldn't it be void * volatile * or volatile PVOID *?
		return apr_atomic_xchgptr ((volatile void**)&m_pValue, (void*)pNewValue);
#endif
	}
	PTYPE Get () {
		return m_pValue;
	}
	void Set (PTYPE pValue) {
		m_pValue = pValue;
	}
};

#endif /* ifndef __inc_og_language_util_atomic_h */