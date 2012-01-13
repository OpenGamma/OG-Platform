/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Util/Fudge.h"

// Test the objects and functions in Util/Fudge.cpp

LOGGING (com.opengamma.language.util.FudgeTest);

static CFudgeInitialiser g_oFudgeInitialiser;

#define TEST_MESSAGES 6

static FudgeMsg _BoolMessage () {
	FudgeMsg msg;
	ASSERT (FudgeMsg_create (&msg) == FUDGE_OK);
	ASSERT (FudgeMsg_addFieldBool (msg, NULL, NULL, FUDGE_TRUE) == FUDGE_OK);
	return msg;
}

static FudgeMsg _IntMessage () {
	FudgeMsg msg;
	ASSERT (FudgeMsg_create (&msg) == FUDGE_OK);
	ASSERT (FudgeMsg_addFieldI32 (msg, NULL, NULL, 42) == FUDGE_OK);
	return msg;
}

static FudgeMsg _DoubleMessage () {
	FudgeMsg msg;
	ASSERT (FudgeMsg_create (&msg) == FUDGE_OK);
	ASSERT (FudgeMsg_addFieldF64 (msg, NULL, NULL, 3.14) == FUDGE_OK);
	return msg;
}

static FudgeMsg _StringMessage () {
	FudgeMsg msg;
	ASSERT (FudgeMsg_create (&msg) == FUDGE_OK);
	FudgeString str;
	ASSERT (FudgeString_createFromASCIIZ (&str, "Foo") == FUDGE_OK);
	ASSERT (FudgeMsg_addFieldString (msg, NULL, NULL, str) == FUDGE_OK);
	FudgeString_release (str);
	return msg;
}

static FudgeMsg _SingleMessageMessage () {
	FudgeMsg msg;
	ASSERT (FudgeMsg_create (&msg) == FUDGE_OK);
	ASSERT (FudgeMsg_addFieldMsg (msg, NULL, NULL, _IntMessage ()) == FUDGE_OK);
	return msg;
}

static FudgeMsg _MultipleMessageMessage () {
	FudgeMsg msg;
	ASSERT (FudgeMsg_create (&msg) == FUDGE_OK);
	ASSERT (FudgeMsg_addFieldMsg (msg, NULL, NULL, _BoolMessage ()) == FUDGE_OK);
	ASSERT (FudgeMsg_addFieldMsg (msg, NULL, NULL, _IntMessage ()) == FUDGE_OK);
	ASSERT (FudgeMsg_addFieldMsg (msg, NULL, NULL, _DoubleMessage ()) == FUDGE_OK);
	ASSERT (FudgeMsg_addFieldMsg (msg, NULL, NULL, _StringMessage ()) == FUDGE_OK);
	ASSERT (FudgeMsg_addFieldMsg (msg, NULL, NULL, _SingleMessageMessage ()) == FUDGE_OK);
	return msg;
}

static void _CreateMessages (FudgeMsg *pmsg) {
	pmsg[0] = _BoolMessage ();
	pmsg[1] = _IntMessage ();
	pmsg[2] = _DoubleMessage ();
	pmsg[3] = _StringMessage ();
	pmsg[4] = _SingleMessageMessage ();
	pmsg[5] = _MultipleMessageMessage ();
}

static void _ReleaseMessages (FudgeMsg *pmsg) {
	int i;
	for (i = 0; i < TEST_MESSAGES; i++) {
		FudgeMsg_release (pmsg[i]);
	}
}

static void Hash () {
	int i;
	FudgeMsg a[TEST_MESSAGES];
	_CreateMessages (a);
	for (i = 0; i < TEST_MESSAGES; i++) {
		size_t hc = FudgeMsg_hash (a[i]);
		LOGINFO (TEXT ("Hash ") << i << TEXT (" = ") << hc);
	}
	_ReleaseMessages (a);
}

static void Compare () {
	int i, j;
	FudgeMsg a[TEST_MESSAGES], b[TEST_MESSAGES];
	_CreateMessages (a);
	_CreateMessages (b);
	for (i = 0; i < TEST_MESSAGES; i++) {
		for (j = 0; j < TEST_MESSAGES; j++) {
			int result = FudgeMsg_compare (a[i], b[j]);
			LOGINFO (TEXT ("a[") << i << TEXT ("],b[") << j << TEXT ("]=") << result);
			if (result) {
				ASSERT (i != j);
			} else {
				ASSERT (i == j);
			}
		}
	}
	_ReleaseMessages (a);
	_ReleaseMessages (b);
}

BEGIN_TESTS (FudgeTest)
	TEST (Hash)
	TEST (Compare)
END_TESTS
