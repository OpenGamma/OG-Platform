/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "String.h"
#include "Atomic.h"
#include "Logging.h"

LOGGING (com.opengamma.language.util.String);

#ifdef _WIN32
#define xmalloc(x)	_aligned_malloc (x, MEMORY_ALLOCATION_ALIGNMENT)
#define xfree(x)	_aligned_free (x)
#else /* ifdef _WIN32 */
#define xmalloc		malloc
#define xfree		free
#endif /* ifdef _WIN32 */

/// Decrement the reference count on a shared string, releasing the string when it reaches zero.
///
/// @param[in] pstr shared string
void StringRelease (PRCSTRING pstr) {
	if (!pstr) {
		LOGWARN (TEXT ("Release called on NULL pointer"));
		return;
	}
	if (CAtomicInt::DecrementAndGet (&pstr->nCount) != 0) {
		return;
	}
	LOGDEBUG (TEXT ("Freeing string '") << pstr->szString << TEXT ("'"));
	xfree (pstr);
}

/// Creates a new shared string.
///
/// @param[in] pszString string value
/// @return the shared string
PRCSTRING StringCreate (const TCHAR *pszString) {
	if (!pszString) {
		LOGWARN (TEXT ("Create called on NULL pointer"));
		return NULL;
	}
	size_t len = _tcslen (pszString);
	// Note the [1] in the structure will contain the room for the trailing zero, so only need len characters extra
	PRCSTRING pstr = (PRCSTRING)xmalloc (sizeof (RCSTRING) + len * sizeof (TCHAR));
	if (!pstr) {
		LOGFATAL (TEXT ("Out of memory"));
		return NULL;
	}
	pstr->nCount = 1;
	memcpy (pstr->szString, pszString, (len + 1) * sizeof (TCHAR));
	LOGDEBUG (TEXT ("Allocated string '") << pstr->szString << TEXT ("'"));
	return pstr;
}

/// Increments the reference count on a shared string.
///
/// @param[in] pstr shared string
void StringRetain (PRCSTRING pstr) {
	if (!pstr) {
		LOGWARN (TEXT ("Retain called on NULL pointer"));
		return;
	}
	CAtomicInt::IncrementAndGet (&pstr->nCount);
}
