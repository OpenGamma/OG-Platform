/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_errors_h
#define __inc_og_language_connector_errors_h

#include "com_opengamma_language_Value.h"

// Error constants. Must match those in Client/src/com/opengamma/language/error/Constants.java

// Errors 1 to 99 reserved for infrastructure
#define ERROR_FIRST_RESERVED		1
#define ERROR_LAST_RESERVED			99

#define ERROR_INVOCATION(x)			(ERROR_FIRST_RESERVED + x)
#define ERROR_PARAMETER_CONVERSION	ERROR_INVOCATION (0)
#define ERROR_RESULT_CONVERSION		ERROR_INVOCATION (1)
#define ERROR_INVALID_ARGUMENT		ERROR_INVOCATION (2)
#define ERROR_INTERNAL				ERROR_INVOCATION (3)

// Errors 100 to 999 reserved for built-in OpenGamma functions
#define ERROR_FIRST_OPENGAMMA		100
#define ERROR_LAST_OPENGAMMA		999

// Errors 1000 to 9999 for use by a custom language binding only; these codes must not be
// used by the core infrastructure or OpenGamma built-in functions

#define ERROR_FIRST_USER			1000
#define ERROR_LAST_USER				9999

/// Error utility functions for handling standard messages from the Java stack.
class CError {
private:

	/// Private constructor to prevent instantiation.
	CError () { }

public:
	static TCHAR *ToString (const com_opengamma_language_Value *pValue);
};

#endif /* ifndef __inc_og_language_connector_errors_h */
