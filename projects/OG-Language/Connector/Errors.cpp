/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Errors.h"
#include <Util/Error.h>

LOGGING (com.opengamma.language.connector.Errors);

#define MESSAGE_BUFFER_SIZE 256

#define SAFE_INT(v) (v ? *v : 0)
#define SAFE_STRING(v) (v ? v : TEXT (""))

/// Processes the ERROR_PARAMETER_CONVERSION error.
///
/// @param[in] nIndex faulty parameter index, zero based
/// @param[in] pszMessage reason for failed conversion, never NULL
/// @return the full error message
static TCHAR *_ParameterConversionError (int nIndex, const TCHAR *pszMessage) {
	TCHAR sz[MESSAGE_BUFFER_SIZE];
	StringCbPrintf (sz, sizeof (sz), TEXT ("Invalid parameter %d type - %s"), nIndex + 1, pszMessage);
	return _tcsdup (sz);
}

/// Processes the ERROR_RESULT_CONVERSION error.
///
/// @param[in] nIndex result index, zero based
/// @param[in] pszMessage reason for failed conversion, never NULL
/// @return the full error message
static TCHAR *_ResultConversionError (int nIndex, const TCHAR *pszMessage) {
	__unused (nIndex)
	TCHAR sz[MESSAGE_BUFFER_SIZE];
	StringCbPrintf (sz, sizeof (sz), TEXT ("Invalid function result - %s"), pszMessage);
	return _tcsdup (sz);
}

/// Processes the ERROR_INVALID_ARGUMENT error.
///
/// @param[in] nIndex parameter index, zero based
/// @param[in] pszMessage reason for failed conversion, never NULL
/// @return the full error message
static TCHAR *_InvalidArgumentError (int nIndex, const TCHAR *pszMessage) {
	TCHAR sz[MESSAGE_BUFFER_SIZE];
	StringCbPrintf (sz, sizeof (sz), TEXT ("Invalid parameter %d value - %s"), nIndex + 1, pszMessage);
	return _tcsdup (sz);
}

/// Processes the ERROR_INTERNAL error.
///
/// @param[in] pszMessage reason for the internal error, never NULL
/// @return the full error message
static TCHAR *_InternalError (const TCHAR *pszMessage) {
	TCHAR sz[MESSAGE_BUFFER_SIZE];
	StringCbPrintf (sz, sizeof (sz), TEXT ("Internal error - %s"), pszMessage);
	return _tcsdup (sz);
}

/// Processes any other errors
///
/// @param[in] nCode error code
/// @return a string representation of the code
static TCHAR *_DefaultError (int nCode) {
	TCHAR sz[MESSAGE_BUFFER_SIZE];
	StringCbPrintf (sz, sizeof (sz), TEXT ("Error %d"), nCode);
	return _tcsdup (sz);
}

/// Converts a value containing an error into a string that can be displayed to the user.
/// The string is allocated on the heap and must be freed by the caller after use.
///
/// @param[in] pValue value to process
/// @return a string representation of the error, or NULL if the value did not contain an
///         error or an internal fault occurred (e.g. out of memory)
TCHAR *CError::ToString (const com_opengamma_language_Value *pValue) {
	if (!pValue->_errorValue) {
		LOGWARN (TEXT ("NULL error value"));
		return NULL;
	}
	switch (*pValue->_errorValue) {
		case ERROR_PARAMETER_CONVERSION :
			return _ParameterConversionError (SAFE_INT (pValue->_intValue), SAFE_STRING (pValue->_stringValue));
		case ERROR_RESULT_CONVERSION :
			return _ResultConversionError (SAFE_INT (pValue->_intValue), SAFE_STRING (pValue->_stringValue));
		case ERROR_INVALID_ARGUMENT :
			return _InvalidArgumentError (SAFE_INT (pValue->_intValue), SAFE_STRING (pValue->_stringValue));
		case ERROR_INTERNAL :
			return _InternalError (SAFE_STRING (pValue->_stringValue));
		default :
			return _DefaultError (*pValue->_errorValue);
	}
}
