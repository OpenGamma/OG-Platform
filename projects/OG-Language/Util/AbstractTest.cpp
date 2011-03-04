/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Generic testing abstraction

#include "AbstractTest.h"
#include <log4cxx/propertyconfigurator.h>
#include <log4cxx/basicconfigurator.h>

LOGGING (com.opengamma.language.util.AbstractTest);

#ifndef __cplusplus_cli

#define MAX_TESTS	50

static int g_nTests = 0;
static int g_nSuccessfulTests = 0;
static CAbstractTest *g_poTests[MAX_TESTS];

CAbstractTest::CAbstractTest () {
	ASSERT (g_nTests < MAX_TESTS);
	g_poTests[g_nTests++] = this;
}

CAbstractTest::~CAbstractTest () {
}

void CAbstractTest::Main () {
	int nTest;
	InitialiseLogs ();
	for (nTest = 0; nTest < g_nTests; nTest++) {
		LOGINFO (TEXT ("Running test ") << (nTest + 1));
		g_poTests[nTest]->BeforeAll ();
		g_poTests[nTest]->Run ();
		g_poTests[nTest]->AfterAll ();
	}
	LOGINFO (TEXT ("Successfully executed ") << g_nSuccessfulTests << TEXT (" in ") << g_nTests << TEXT (" components"));
	LOGDEBUG (TEXT ("Exiting with error code 0"));
	exit (0);
}

void CAbstractTest::After () {
	g_nSuccessfulTests++;
}

#endif /* ifndef __cplusplus_cli */

void CAbstractTest::Fail () {
#ifdef __cplusplus_cli
	LOGDEBUG (TEXT ("Calling Assert::Fail"));
	Assert::Fail ();
#else
	LOGDEBUG (TEXT ("Exiting with error code 1"));
	exit (1);
#endif /* ifdef __cplusplus_cli */
}

void CAbstractTest::InitialiseLogs () {
	static bool bFirst = true;
	if (bFirst) {
		bFirst = false;
	} else {
		return;
	}
#ifdef _WIN32
	TCHAR szConfigurationFile[MAX_PATH];
	if (GetEnvironmentVariable (TEXT ("LOG4CXX_CONFIGURATION"), szConfigurationFile, MAX_PATH) != 0) {
#define pszConfigurationFile szConfigurationFile
#else
	char *pszConfigurationFile = getenv ("LOG4CXX_CONFIGURATION");
	if (pszConfigurationFile) {
#endif
		::log4cxx::PropertyConfigurator::configure (pszConfigurationFile);
		LOGDEBUG (TEXT ("Logging initialised from ") << pszConfigurationFile);
	} else {
		::log4cxx::BasicConfigurator::configure ();
		LOGDEBUG (TEXT ("Logging using default configuration"));
	}
}
