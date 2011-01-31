/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_error_h
#define __inc_og_language_util_error_h

// Standard error reporting/handling

#ifndef _WIN32
#define GetLastError() errno
#define SetLastError(_expr_) (errno = (_expr_))
#endif /* ifndef _WIN32 */

inline bool PosixLastError(int result) {
	if (result == 0) return true;
	SetLastError (result);
	return false;
}

#endif /* ifndef __inc_og_language_util_error_h */