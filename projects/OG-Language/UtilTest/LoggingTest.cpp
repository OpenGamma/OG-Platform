/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Util/Logging.cpp

LOGGING (com.opengamma.language.util.LoggingTest);

class CLogSettings : public CAbstractSettings {
private:
	const TCHAR *m_pszLogConfiguration;
public:
	CLogSettings (const TCHAR *pszLogConfiguration) {
		m_pszLogConfiguration = pszLogConfiguration;
	}
	const TCHAR *GetLogConfiguration () {
		return m_pszLogConfiguration;
	}
};

static void InitialisationFromPath () {
	CLogSettings settings (TEXT ("/test/with/a/path/name"));
	LoggingInit (&settings);
}

static void DefaultInitialisation () {
	CLogSettings settings (NULL);
	LoggingInit (&settings);
}

#define IGNORE_TEST // Comment out this line to run these tests, they will propbably screw up the logging subsystem, so you don't want to run them as part of a batch

#ifndef IGNORE_TEST
BEGIN_TESTS (LoggingTest)
	TEST (InitialisationFromPath)
	TEST (DefaultInitialisation)
END_TESTS
#endif /* ifdef IGNORE_TEST */