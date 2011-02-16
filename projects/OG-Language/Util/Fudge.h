/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_fudge_h
#define __inc_og_language_util_fudge_h

// Standard Fudge files and configuration

// Main header files
#include <fudge/fudge.h>
#include <fudge/message.h>
#include <fudge/envelope.h>
#include <fudge/header.h>
#include <fudge/codec.h>

// Proto-C required definitions for Unicode build
#include "Unicode.h"
#ifdef _UNICODE
#define FUDGE_STRING_TYPE UTF16
#define FUDGE_STRING_TYPE_CAST (fudge_byte*)
#else /* ifdef _UNICODE */
#define FUDGE_STRING_TYPE ASCII
#endif /* ifdef _UNICODE */
#define FUDGE_STRING_LENGTH _tcslen

// Force library initialization (C++)
#ifdef __cplusplus
class CFudgeInitialiser {
public:
	CFudgeInitialiser () {
		FudgeStatus status = Fudge_init ();
		assert (status == FUDGE_OK);
	}
};
#endif /* ifdef __cplusplus */

#endif /* ifndef __inc_og_language_util_fudge_h */