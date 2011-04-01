/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Errors.h"

LOGGING (com.opengamma.language.connector.Errors);

#define MESSAGE_BUFFER_SIZE 256

#define SAFE_INT(v) (v ? *v : 0)
#define SAFE_STRING(v) (v ? v : TEXT (""))

static TCHAR *_ParameterConversionError (int nIndex, const TCHAR *pszMessage) {
	TCHAR sz[MESSAGE_BUFFER_SIZE];
	StringCbPrintf (sz, sizeof (sz), TEXT ("Invalid parameter %d - %s"), nIndex + 1, pszMessage);
}

static TCHAR *_ResultConversionError (int nIndex, const TCHAR *pszMessage) {
	TCHAR sz[MESSAGE_BUFFER_SIZE];
	StringCbPrintf (sz, sizeof (sz), TEXT ("Invalid function result - %s"), pszMessage);
	return _tcsdup (sz);
}

static TCHAR *_DefaultError (int nCode) {
	TCHAR sz[MESSAGE_BUFFER_SIZE];
	StringCbPrintf (sz, sizeof (sz), TEXT ("Error %d"), nCode);
	return _tcsdup (sz);
}

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
		default :
			return _DefaultError (*pValue->_errorValue);
	}
}
