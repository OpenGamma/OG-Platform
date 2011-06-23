/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_atomic_h
#define __inc_og_language_util_atomic_h

#ifndef _WIN32
#include <apr-1/apr_atomic.h>
#endif

/// Wrapper for a volatile integer supporting atomic operations. All operations are inlined and the
/// only internal memory is the integer variable itself. An optimising compiler will produce the
/// same code as if the interlocked calls are used directly; typically then reducing down to intrinsics.
class CAtomicInt {
private:

	/// Integer value.
	volatile unsigned int m_nValue;

public:

	/// Creates a new integer.
	///
	/// @param[in] nValue default initial value
	CAtomicInt (unsigned int nValue = 0) {
		m_nValue = nValue;
	}

	/// Decrements an integer, returning the decremented value.
	///
	/// @param[in,out] pnValue the value to decrement
	/// @return the decremented value
	static int DecrementAndGet (volatile unsigned int *pnValue) {
#ifdef _WIN32
		return InterlockedDecrement (pnValue);
#else
		// Note this only reliably returns 0
		return apr_atomic_dec32 (pnValue);
#endif
	}

	/// Decrements the integer, returning the decremented value.
	///
	/// @return the decremented value
	int DecrementAndGet () {
		return DecrementAndGet (&m_nValue);
	}

	/// Increments an integer, returning the incremented value.
	///
	/// @param[in,out] pnValue the value to increment
	/// @return the incremented value
	static int IncrementAndGet (volatile unsigned int *pnValue) {
#ifdef _WIN32
		return InterlockedIncrement (pnValue);
#else
		return apr_atomic_inc32 (pnValue) + 1;
#endif
	}

	/// Increments the integer, returning the incremented value.
	///
	/// @return the incremented value
	int IncrementAndGet () {
		return IncrementAndGet (&m_nValue);
	}

	/// Returns the current value.
	///
	/// @return the current value
	int Get () const {
#ifdef _WIN32
		return m_nValue;
#else
		return apr_atomic_read32 ((volatile apr_uint32_t*)&m_nValue);
#endif
	}

	/// Sets the current value.
	///
	/// @param[in] nValue the current value
	void Set (int nValue) {
#ifdef _WIN32
		m_nValue = nValue;
#else
		apr_atomic_set32 (&m_nValue, nValue);
#endif
	}

	/// Compare and Exchange the current value.
	///
	/// @param[in] nNewValue new value to set if the comparand matches
	/// @param[in] nCompareWith comparand to test against
	/// @return the original value
	int CompareAndSet (int nNewValue, int nCompareWith) {
#ifdef _WIN32
		return InterlockedCompareExchange (&m_nValue, nNewValue, nCompareWith);
#else /* ifdef _WIN32 */
		return apr_atomic_cas32 (&m_nValue, nNewValue, nCompareWith);
#endif /* ifdef _WIN32 */
	}
};

/// Wrapper for a volatile pointer. All operations are inlined and the only internal memory
/// is the integer variable itself. An optimising compiler will produce the same code as if
/// the interlocked calls are used directly; typically then reducing down to intrinsics.
#ifdef _WIN32
///
/// Note that the ATLCONV.H header in the platform SDK redefines the InterlockedExchangePointer
/// function. If you are using ATL, include this header before the ATL headers.
#endif /* ifdef _WIN32 */
template <typename PTYPE> class CAtomicPointer {
private:

	/// Pointer value.
	PTYPE volatile m_pValue;

public:

	/// Creates a new pointer.
	///
	/// @param[in] pValue initial value
	CAtomicPointer (PTYPE pValue = NULL) {
		m_pValue = pValue;
	}

	/// Exchange operation.
	///
	/// @param[in] pNewValue new value to set
	/// @return the original value
	PTYPE GetAndSet (PTYPE pNewValue) {
#ifdef _WIN32
		return (PTYPE)InterlockedExchangePointer ((void * volatile *)&m_pValue, (void*)pNewValue);
#else
		// Is the signature for the method wrong; shouldn't it be void * volatile * or volatile PVOID *?
		return (PTYPE)apr_atomic_xchgptr ((volatile void**)&m_pValue, (void*)pNewValue);
#endif
	}

	/// Compare and Exchange operation.
	///
	/// @param[in] pNewValue new value to set if the comparand matches
	/// @param[in] pCompareWith comparand to test against
	/// @return the original value
	PTYPE CompareAndSet (PTYPE pNewValue, PTYPE pCompareWith) {
#ifdef _WIN32
		return (PTYPE)InterlockedCompareExchangePointer ((void * volatile *)&m_pValue, (void*)pNewValue, (void*)pCompareWith);
#else /* ifdef _WIN32 */
		// Is the signature for the method wrong; shouldn't it be void * volatile * or volatile PVOID *?
		return (PTYPE)apr_atomic_casptr ((volatile void**)&m_pValue, (void*)pNewValue, (void*)pCompareWith);
#endif /* ifdef _WIN32 */
	}

	/// Returns the current value
	///
	/// @return the current value
	PTYPE Get () const {
		return m_pValue;
	}

	/// Sets the current value
	///
	/// @param[in] pValue new value
	void Set (PTYPE pValue) {
		m_pValue = pValue;
	}

};

#endif /* ifndef __inc_og_language_util_atomic_h */
