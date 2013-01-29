/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the livedata methods

#include "Connector/LiveData.h"

LOGGING (com.opengamma.language.connector.LiveDataTest);

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
	CLiveDataQueryAvailable query (g_poConnector);
	ASSERT (query.Send ());
	com_opengamma_language_livedata_Available *pAvailable = query.Recv (nTimeout);
	ASSERT (pAvailable);
	LOGINFO (TEXT ("Received ") << pAvailable->fudgeCountLiveData << TEXT (" definitions"));
	ASSERT (pAvailable->fudgeCountLiveData > 0);
	int i;
	for (i = 0; i < pAvailable->fudgeCountLiveData; i++) {
		LOGDEBUG (TEXT ("Live data ") << i << TEXT (": ") << pAvailable->_liveData[i]->_definition->fudgeParent._name << TEXT (" (") << pAvailable->_liveData[i]->_identifier << TEXT (")"));
	}
}

static void QueryAvailableFirst () {
	QueryAvailable (TIMEOUT_STARTUP);
}

static void QueryAvailableSecond () {
	QueryAvailable (CRequestBuilder::GetDefaultTimeout () * 2);
}

static void ConnectInvalid () {
	CLiveDataConnect connect (g_poConnector);
	connect.SetComponentId (INT_MAX);
	connect.SetConnectionId (1);
	ASSERT (connect.Send ());
	com_opengamma_language_livedata_Result *pResult = connect.Recv (CRequestBuilder::GetDefaultTimeout ());
	ASSERT (pResult);
	ASSERT (!pResult->_connection);
	ASSERT (!pResult->_result);
}

BEGIN_TESTS(LiveDataTest)
	TEST (QueryAvailableFirst)
	TEST (ConnectInvalid)
	TEST (QueryAvailableSecond)
	BEFORE_TEST (StartConnector)
	AFTER_TEST (StopConnector)
END_TESTS
