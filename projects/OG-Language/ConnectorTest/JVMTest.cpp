/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Connector/JVM.cpp

#include "Connector/JVM.h"

LOGGING (com.opengamma.language.connector.JVMTest);

static void CreateStop () {
	CClientJVM *poJVM = CClientJVM::Start ();
	ASSERT (poJVM);
	ASSERT (poJVM->IsAlive ());
	ASSERT (poJVM->Stop ());
	delete poJVM;
}

BEGIN_TESTS (ClientJVMTest)
	TEST (CreateStop)
END_TESTS
