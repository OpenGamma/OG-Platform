/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Tests overlapped calls to Functions, LiveData and Procedures

#include "Connector/Functions.h"
#include "Connector/LiveData.h"
#include "Connector/Procedures.h"

LOGGING (com.opengamma.language.connector.OverlappedTest);

#define TEST_LANGUAGE		TEXT ("test")
#define TIMEOUT_STARTUP		30000
#define TIMEOUT_CALL		3000
#define RETRIES				10

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
	double tOverlapped = 0, tInline = 0;
	int i;
	for (i = 0; i < RETRIES; i++) {
		tOverlapped -= GetTickCount ();
		CFunctionQueryAvailable queryFunctions (g_poConnector);
		CLiveDataQueryAvailable queryLiveData (g_poConnector);
		CProcedureQueryAvailable queryProcedures (g_poConnector);
		ASSERT (queryFunctions.Send ());
		ASSERT (queryLiveData.Send ());
		ASSERT (queryProcedures.Send ());
		ASSERT (queryFunctions.Recv (CRequestBuilder::GetDefaultTimeout ()));
		ASSERT (queryLiveData.Recv (CRequestBuilder::GetDefaultTimeout ()));
		ASSERT (queryProcedures.Recv (CRequestBuilder::GetDefaultTimeout ()));
		tOverlapped += GetTickCount ();
		LOGDEBUG (TEXT ("Overlapped QueryAvailable = ") << (tOverlapped / (double)(i + 1)) << TEXT ("ms"));
	}
	tOverlapped /= (double)RETRIES;
	LOGINFO (TEXT ("Overlapped QueryAvailable = ") << tOverlapped << TEXT ("ms"));
	for (i = 0; i < RETRIES; i++) {
		tInline -= GetTickCount ();
		CFunctionQueryAvailable queryFunctions (g_poConnector);
		ASSERT (queryFunctions.Send ());
		ASSERT (queryFunctions.Recv (CRequestBuilder::GetDefaultTimeout ()));
		CLiveDataQueryAvailable queryLiveData (g_poConnector);
		ASSERT (queryLiveData.Send ());
		ASSERT (queryLiveData.Recv (CRequestBuilder::GetDefaultTimeout ()));
		CProcedureQueryAvailable queryProcedures (g_poConnector);
		ASSERT (queryProcedures.Send ());
		ASSERT (queryProcedures.Recv (CRequestBuilder::GetDefaultTimeout ()));
		tInline += GetTickCount ();
		LOGDEBUG (TEXT ("Inline QueryAvailable = ") << (tInline / (double)(i + 1)) << TEXT ("ms"));
	}
	tInline /= (double)RETRIES;
	LOGINFO (TEXT ("Inline QueryAvailable = ") << tInline << TEXT ("ms"));
	ASSERT (tOverlapped <= tInline);
}

BEGIN_TESTS(OverlappedTest)
	TEST (QueryAvailable)
	BEFORE_TEST (StartConnector)
	AFTER_TEST (StopConnector)
END_TESTS
