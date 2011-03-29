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
#define RETRIES				20

static CConnector *g_poConnector;
static double g_tOverlappedQuery = 0;
static double g_tInlineQuery = 0;

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

static void QueryAvailableOverlapped () {
	g_tOverlappedQuery = 0;
	int i;
	for (i = 0; i < RETRIES; i++) {
		g_tOverlappedQuery -= GetTickCount ();
		CFunctionQueryAvailable queryFunctions (g_poConnector);
		CLiveDataQueryAvailable queryLiveData (g_poConnector);
		CProcedureQueryAvailable queryProcedures (g_poConnector);
		ASSERT (queryFunctions.Send ());
		ASSERT (queryLiveData.Send ());
		ASSERT (queryProcedures.Send ());
		ASSERT (queryFunctions.Recv (CRequestBuilder::GetDefaultTimeout ()));
		ASSERT (queryLiveData.Recv (CRequestBuilder::GetDefaultTimeout ()));
		ASSERT (queryProcedures.Recv (CRequestBuilder::GetDefaultTimeout ()));
		g_tOverlappedQuery += GetTickCount ();
		LOGDEBUG (TEXT ("Overlapped QueryAvailable = ") << (g_tOverlappedQuery / (double)(i + 1)) << TEXT ("ms"));
	}
	g_tOverlappedQuery /= (double)RETRIES;
	LOGINFO (TEXT ("Overlapped QueryAvailable = ") << g_tOverlappedQuery << TEXT ("ms"));
}

static void DoQueryAvailableOverlapped () {
	QueryAvailableOverlapped ();
	QueryAvailableOverlapped ();
}

static void QueryAvailableInline () {
	g_tInlineQuery = 0;
	int i;
	for (i = 0; i < RETRIES; i++) {
		g_tInlineQuery -= GetTickCount ();
		CFunctionQueryAvailable queryFunctions (g_poConnector);
		ASSERT (queryFunctions.Send ());
		ASSERT (queryFunctions.Recv (CRequestBuilder::GetDefaultTimeout ()));
		CLiveDataQueryAvailable queryLiveData (g_poConnector);
		ASSERT (queryLiveData.Send ());
		ASSERT (queryLiveData.Recv (CRequestBuilder::GetDefaultTimeout ()));
		CProcedureQueryAvailable queryProcedures (g_poConnector);
		ASSERT (queryProcedures.Send ());
		ASSERT (queryProcedures.Recv (CRequestBuilder::GetDefaultTimeout ()));
		g_tInlineQuery += GetTickCount ();
		LOGDEBUG (TEXT ("Inline QueryAvailable = ") << (g_tInlineQuery / (double)(i + 1)) << TEXT ("ms"));
	}
	g_tInlineQuery /= (double)RETRIES;
	LOGINFO (TEXT ("Inline QueryAvailable = ") << g_tInlineQuery << TEXT ("ms"));
}

static void DoQueryAvailableInline () {
	QueryAvailableInline ();
	QueryAvailableInline ();
	ASSERT (g_tOverlappedQuery < g_tInlineQuery);
}

BEGIN_TESTS(OverlappedTest)
	TEST (DoQueryAvailableOverlapped)
	TEST (DoQueryAvailableInline)
	BEFORE_TEST (StartConnector)
	AFTER_TEST (StopConnector)
END_TESTS
