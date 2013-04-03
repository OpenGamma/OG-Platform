/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Service/JVM.cpp

#include "Service/JVM.h"

LOGGING (com.opengamma.language.service.JVMTest);

#define TIMEOUT_START_JVM		30000
#define TIMEOUT_STOP_JVM		10000

/**
 * Tries to start the JVM and invoke the same methods as MainTest/TestStartStop does.
 */
static void StartStop () {
	CErrorFeedback oFeedback;
	CJVM *pJvm = CJVM::Create (&oFeedback);
	ASSERT (pJvm);
	ASSERT (!pJvm->IsBusy (0));
	ASSERT (!pJvm->IsRunning ());
	LOGDEBUG (TEXT ("Starting JVM"));
	pJvm->Start (&oFeedback);
	int nAttempt = 0;
	while (pJvm->IsBusy (TIMEOUT_START_JVM / 10)) {
		LOGDEBUG (TEXT ("JVM busy"));
		ASSERT (++nAttempt < 10);
	}
	ASSERT (pJvm->IsRunning ());
	LOGDEBUG (TEXT ("Sending a client connection"));
	pJvm->UserConnection (TEXT ("TestUser"), TEXT ("Foo"), TEXT ("Bar"), TEXT ("test"));
	LOGDEBUG (TEXT ("Stopping JVM"));
	pJvm->Stop ();
	nAttempt = 0;
	while (pJvm->IsBusy (TIMEOUT_STOP_JVM / 10)) {
		LOGDEBUG (TEXT ("JVM busy"));
		ASSERT (++nAttempt < 10);
	}
	ASSERT (!pJvm->IsRunning ());
	LOGDEBUG (TEXT ("Destroying JVM"));
	delete pJvm;
}

//#define RUN_TESTS			// JVM limitations mean that ServiceTest will not pass if JVMTest is run

#ifndef RUN_TESTS
#undef BEGIN_TESTS
#define BEGIN_TESTS MANUAL_TESTS
#endif /* ifndef RUN_TESTS */

BEGIN_TESTS (JVMTest)
	TEST (StartStop)
END_TESTS
