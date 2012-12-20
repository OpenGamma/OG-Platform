/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
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
#include <fudge/datetime.h>

// Proto-C required definitions for Unicode build
#include "Unicode.h"
#ifdef _UNICODE
#define FUDGE_STRING_TYPE UTF16
#define FUDGE_STRING_TYPE_CAST (fudge_byte*)
#else /* ifdef _UNICODE */
#define FUDGE_STRING_TYPE ASCII
#endif /* ifdef _UNICODE */
#define FUDGE_STRING_LENGTH _tcslen

#ifdef __cplusplus
/// Forces library initialisation during static startup.
class CFudgeInitialiser {
public:
	/// Creates a CFudgeInitialiser instance, initialising the Fudge library.
	CFudgeInitialiser () {
#ifndef NDEBUG
		FudgeStatus status = 
#endif /* ifndef NDEBUG */
			Fudge_init ();
		assert (status == FUDGE_OK);
	}
};
#endif /* ifdef __cplusplus */

size_t FudgeMsg_hash (const FudgeMsg msg);
int FudgeMsg_compare (const FudgeMsg a, const FudgeMsg b);

#endif /* ifndef __inc_og_language_util_fudge_h */
