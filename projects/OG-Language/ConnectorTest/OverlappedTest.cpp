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
#define SAMPLE_TIME			5000

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
	long tStart = GetTickCount (), tNow;
	int i = 0;
	do {
		long tQuery = GetTickCount ();
		CFunctionQueryAvailable queryFunctions (g_poConnector);
		CLiveDataQueryAvailable queryLiveData (g_poConnector);
		CProcedureQueryAvailable queryProcedures (g_poConnector);
		ASSERT (queryFunctions.Send ());
		ASSERT (queryLiveData.Send ());
		ASSERT (queryProcedures.Send ());
		ASSERT (queryFunctions.Recv (CRequestBuilder::GetDefaultTimeout ()));
		ASSERT (queryLiveData.Recv (CRequestBuilder::GetDefaultTimeout ()));
		ASSERT (queryProcedures.Recv (CRequestBuilder::GetDefaultTimeout ()));
		tNow = GetTickCount ();
		tQuery = tNow - tQuery;
		LOGDEBUG (TEXT ("Overlapped QueryAvailable ~ ") << tQuery << TEXT ("ms"));
		i++;
	} while (tNow - tStart < SAMPLE_TIME);
	g_tOverlappedQuery = (double)(tNow - tStart) / (double)i;
	LOGINFO (TEXT ("Overlapped QueryAvailable = ") << g_tOverlappedQuery << TEXT ("ms (from ") << i << TEXT (")"));
}

static void DoQueryAvailableOverlapped () {
	QueryAvailableOverlapped ();
	QueryAvailableOverlapped ();
}

static void QueryAvailableInline () {
	long tStart = GetTickCount (), tNow;
	int i = 0;
	do {
		long tQuery = GetTickCount ();
		CFunctionQueryAvailable queryFunctions (g_poConnector);
		ASSERT (queryFunctions.Send ());
		ASSERT (queryFunctions.Recv (CRequestBuilder::GetDefaultTimeout ()));
		CLiveDataQueryAvailable queryLiveData (g_poConnector);
		ASSERT (queryLiveData.Send ());
		ASSERT (queryLiveData.Recv (CRequestBuilder::GetDefaultTimeout ()));
		CProcedureQueryAvailable queryProcedures (g_poConnector);
		ASSERT (queryProcedures.Send ());
		ASSERT (queryProcedures.Recv (CRequestBuilder::GetDefaultTimeout ()));
		tNow = GetTickCount ();
		tQuery = tNow - tQuery;
		LOGDEBUG (TEXT ("Inline QueryAvailable ~ ") << tQuery << TEXT ("ms"));
		i++;
	} while (tNow - tStart < SAMPLE_TIME);
	g_tInlineQuery = (double)(tNow - tStart) / (double)i;
	LOGINFO (TEXT ("Inline QueryAvailable = ") << g_tInlineQuery << TEXT ("ms (from ") << i << TEXT (")"));
}

static void DoQueryAvailableInline () {
	QueryAvailableInline ();
	QueryAvailableInline ();
	LOGINFO (TEXT ("ASSERT ") << g_tOverlappedQuery << TEXT (" <= ") << g_tInlineQuery);
	ASSERT (g_tOverlappedQuery <= g_tInlineQuery);
}

//#define RUN_TESTS				// These tests are timing based and need to run on a "quiet" system to be reliable.

#ifndef RUN_TESTS
#undef BEGIN_TESTS
#define BEGIN_TESTS MANUAL_TESTS
#endif

BEGIN_TESTS(OverlappedTest)
	TEST (DoQueryAvailableOverlapped)
	TEST (DoQueryAvailableInline)
	BEFORE_TEST (StartConnector)
	AFTER_TEST (StopConnector)
END_TESTS
