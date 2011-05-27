/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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
	static int DecrementAndGet (volatile unsigned int *pnValue) {
#ifdef _WIN32
		return InterlockedDecrement (pnValue);
#else
		// Note this only reliably returns 0
		return apr_atomic_dec32 (pnValue);
#endif
	}
	int DecrementAndGet () {
		return DecrementAndGet (&m_nValue);
	}
	static int IncrementAndGet (volatile unsigned int *pnValue) {
#ifdef _WIN32
		return InterlockedIncrement (pnValue);
#else
		return apr_atomic_inc32 (pnValue) + 1;
#endif
	}
	int IncrementAndGet () {
		return IncrementAndGet (&m_nValue);
	}
	int Get () const {
#ifdef _WIN32
		return m_nValue;
#else
		return apr_atomic_read32 ((volatile apr_uint32_t*)&m_nValue);
#endif
	}
	void Set (int nValue) {
#ifdef _WIN32
		m_nValue = nValue;
#else
		apr_atomic_set32 (&m_nValue, nValue);
#endif
	}
	int CompareAndSet (int nNewValue, int nCompareWith) {
#ifdef _WIN32
		return InterlockedCompareExchange (&m_nValue, nNewValue, nCompareWith);
#else /* ifdef _WIN32 */
		return apr_atomic_cas32 (&m_nValue, nNewValue, nCompareWith);
#endif /* ifdef _WIN32 */
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
		return (PTYPE)InterlockedExchangePointer ((void * volatile *)&m_pValue, (void*)pNewValue);
#else
		// Is the signature for the method wrong; shouldn't it be void * volatile * or volatile PVOID *?
		return (PTYPE)apr_atomic_xchgptr ((volatile void**)&m_pValue, (void*)pNewValue);
#endif
	}
	PTYPE CompareAndSet (PTYPE pNewValue, PTYPE pCompareWith) {
#ifdef _WIN32
		return (PTYPE)InterlockedCompareExchangePointer ((void * volatile *)&m_pValue, (void*)pNewValue, (void*)pCompareWith);
#else /* ifdef _WIN32 */
		// Is the signature for the method wrong; shouldn't it be void * volatile * or volatile PVOID *?
		return (PTYPE)apr_atomic_casptr ((volatile void**)&m_pValue, (void*)pNewValue, (void*)pCompareWith);
#endif /* ifdef _WIN32 */
	}
	PTYPE Get () const {
		return m_pValue;
	}
	void Set (PTYPE pValue) {
		m_pValue = pValue;
	}
};

#endif /* ifndef __inc_og_language_util_atomic_h */
