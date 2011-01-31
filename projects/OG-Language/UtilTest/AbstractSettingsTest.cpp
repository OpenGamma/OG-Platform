/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Util/AbstractSettings.cpp

#include "Util/AbstractSettings.h"

LOGGING (com.opengamma.language.util.AbstractSettingsTest);

static void Location () {
	TCHAR szBuffer[256];
	ASSERT (CAbstractSettings::GetSettingsLocation (szBuffer, sizeof (szBuffer)));
	LOGINFO (TEXT ("Settings location = ") << szBuffer);
}

class CCachingTest : public CAbstractSettings {
public:
	void Run () {
		const TCHAR *pszFoo = CachePut (TEXT ("Foo"), TEXT ("1"));
		ASSERT (pszFoo);
		ASSERT (!_tcscmp (pszFoo, TEXT ("1")));
		const TCHAR *pszBar = CachePut (TEXT ("Bar"), TEXT ("2"));
		ASSERT (pszBar);
		ASSERT (!_tcscmp (pszBar, TEXT ("2")));
		ASSERT (CacheGet (TEXT ("Foo")) == pszFoo);
		ASSERT (CacheGet (TEXT ("Bar")) == pszBar);
		ASSERT (!CacheGet (TEXT ("Missing")));
	}
	const TCHAR *GetLogConfiguration () {
		return TEXT ("");
	}
};

static void Caching () {
	CCachingTest settings;
	settings.Run ();
}

BEGIN_TESTS (AbstractSettingsTest)
	TEST (Location)
	TEST (Caching)
END_TESTS