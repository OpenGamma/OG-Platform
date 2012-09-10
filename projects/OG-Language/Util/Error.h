/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_error_h
#define __inc_og_language_util_error_h

#include <errno.h>

#define __unused(x) (void)(x);

#ifndef _WIN32

// Returns the last error on the calling thread. For compatability with Win32 style API.
#define GetLastError() errno

// Sets the value returned by GetLastError. For compatability with Win32 style API.
#define SetLastError(_expr_) (errno = (_expr_))

#endif /* ifndef _WIN32 */

#ifdef __cplusplus

/// Wraps a function that returns its error code (or zero for success) as a function that
/// returns a boolean and calls SetLastError if there is a problem.
///
/// @param[in] result original function result (zero for success, or error code)
/// @return true if successful, false if there was an error
inline bool PosixLastError(int result) {
	if (result == 0) return true;
	SetLastError (result);
	return false;
}

#endif /* ifdef __cplusplus */

#ifdef __cplusplus_cli
/// Expose the Win32 ::GetLastError call which is not normally available to a CLI after
/// crossing the managed/unmanaged bridge.
#pragma unmanaged
static int NativeGetLastError () {
	return GetLastError ();
}
#pragma managed

#else /* ifdef __cplusplus_cli */

// Allow compatability between CLI and compiled C++ for access to error codes.
#define NativeGetLastError GetLastError

#endif /* ifdef __cplusplus_cli */

#ifdef _WIN32

// Don't use POSIX compatible calls with the standard Win32 library, so replace the
// POSIX names used in portable code with Win32 error codes that end up in GetLastError
// which we are using in place of errno.

#undef EALREADY
// Map Win32/ERROR_INVALID_STATE to/from Posix/EALREADY
#define EALREADY		ERROR_INVALID_STATE

#undef ECANCELED
// Map Win32/ERROR_CANCELLED to/from Posix/ECANCELED
#define ECANCELED		ERROR_CANCELLED

#undef EINVAL
// Map Win32/ERROR_INVALID_PARAMETER to/from Posix/EINVAL
#define EINVAL			ERROR_INVALID_PARAMETER

#undef EIO
// Map Win32/ERROR_READ_FAULT to EIO_READ
#define EIO_READ		ERROR_READ_FAULT
// Map Win32/ERROR_WRITE_FAULT to EIO_WRITE
#define EIO_WRITE		ERROR_WRITE_FAULT
// Use IS_EIO in place of testing against Posix/EIO. It will match both EIO_READ and EIO_WRITE.
#define IS_EIO(ec)		((ec == ERROR_READ_FAULT) || (ec == ERROR_WRITE_FAULT))

#undef ENOENT
// Map Win32/ERROR_FILE_NOT_FOUND to Posix/ENOENT
#define ENOENT			ERROR_FILE_NOT_FOUND

#undef ENOMEM
// Map Win32/ERROR_OUTOFMEMORY to Posix/ENOMEM
#define ENOMEM			ERROR_OUTOFMEMORY

#undef ENOTCONN
// Map Win32/ERROR_NOT_CONNECTED to Posix/ENOTCONN
#define ENOTCONN		ERROR_NOT_CONNECTED

#undef ETIMEDOUT
// Map Win32/ERROR_TIMEOUT to Posix/ETIMEDOUT
#define ETIMEDOUT		ERROR_TIMEOUT

#else /* ifdef _WIN32 */

// A read error, maps to EIO when there is no distinction between read and write on I/O errors
#define EIO_READ		EIO

// A write error, maps to EIO when there is no distinction between read and write on I/O errors
#define EIO_WRITE		EIO

// Use IS_EIO in place of testing against Posix/EIO to allow when read and write errors can be distinguished.
#define IS_EIO(ec)		(ec == EIO)

#endif /* ifdef _WIN32 */

#endif /* ifndef __inc_og_language_util_error_h */
