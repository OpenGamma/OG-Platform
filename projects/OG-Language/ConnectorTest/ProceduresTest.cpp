/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the procedure methods

#include "Connector/Procedures.h"

LOGGING (com.opengamma.language.connector.ProceduresTest);

#define TEST_LANGUAGE		TEXT ("test")
#define TIMEOUT_STARTUP		30000

static CConnector *g_poConnector;

static void StartConnector () {
	g_poConnector = CConnector::Start (TEST_LANGUAGE);
	ASSERT (g_poConnector);
	ASSERT (g_poConnector->WaitForStartup (TIMEOUT_STARTUP));
}

static void StopConnector () {
	ASSERT (g_poConnector->Stop ());
	CConnector::Release (g_poConnector);
	g_poConnector = NULL;
}

static void QueryAvailable (int nTimeout) {
	CProcedureQueryAvailable query (g_poConnector);
	ASSERT (query.Send ());
	com_opengamma_language_procedure_Available *pAvailable = query.Recv (nTimeout);
	ASSERT (pAvailable);
	LOGINFO (TEXT ("Received ") << pAvailable->fudgeCountProcedure << TEXT (" definitions"));
	ASSERT (pAvailable->fudgeCountProcedure > 0);
	int i;
	for (i = 0; i < pAvailable->fudgeCountProcedure; i++) {
		LOGDEBUG (TEXT ("Function ") << i << TEXT (": ") << pAvailable->_procedure[i]->_definition->fudgeParent._name << TEXT (" (") << pAvailable->_procedure[i]->_identifier << TEXT (")"));
	}
}

static void QueryAvailableFirst () {
	QueryAvailable (TIMEOUT_STARTUP);
}

static void QueryAvailableSecond () {
	QueryAvailable (CRequestBuilder::GetDefaultTimeout () * 2);
}

static void InvokeInvalid () {
	CProcedureInvoke invoke (g_poConnector);
	invoke.SetInvocationId (99);
	ASSERT (invoke.Send ());
	com_opengamma_language_procedure_Result *pResult = invoke.Recv (CRequestBuilder::GetDefaultTimeout ());
	ASSERT (pResult);
	ASSERT (!pResult->fudgeCountResult);
}

BEGIN_TESTS(ProceduresTest)
	TEST (QueryAvailableFirst)
	TEST (InvokeInvalid)
	TEST (QueryAvailableSecond)
	BEFORE_TEST (StartConnector)
	AFTER_TEST (StopConnector)
END_TESTS
