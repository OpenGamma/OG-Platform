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
#define TIMEOUT_CALL		3000

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

static void QueryAvailable () {
	CProcedureQueryAvailable query (g_poConnector);
	ASSERT (query.Send ());
	ASSERT (query.Recv (CRequestBuilder::GetDefaultTimeout ()));
}

BEGIN_TESTS(ProceduresTest)
	TEST (QueryAvailable)
	BEFORE_TEST (StartConnector)
	AFTER_TEST (StopConnector)
END_TESTS