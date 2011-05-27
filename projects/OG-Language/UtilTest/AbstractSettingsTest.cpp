/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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
	const TCHAR *GetLogConfiguration () const {
		return TEXT ("");
	}
};

static void Caching () {
	CCachingTest settings;
	settings.Run ();
}

class CTestSettingProvider : public CAbstractSettingProvider {
protected:
	TCHAR *CalculateString () const {
		return _tcsdup (TEXT ("Foo"));
	}
};

static CTestSettingProvider g_oTest;

static void Provider () {
	const TCHAR *pszTestValue = g_oTest.GetString ();
	ASSERT (pszTestValue);
	ASSERT (!_tcscmp (pszTestValue, TEXT ("Foo")));
}

class CTestSettings : public CAbstractSettings {
private:
	const TCHAR *GetTest (const TCHAR *pszDefault) { return Get (TEXT ("test"), pszDefault); }
	const TCHAR *GetTest (const CTestSettingProvider *poDefault) { return Get (TEXT ("test"), poDefault); }
	int GetTest (int nDefault) { return Get (TEXT ("test"), nDefault); }
public:
	const TCHAR *GetTest1 () {
		return GetTest (TEXT ("Bar"));
	}
	const TCHAR *GetTest2 () {
		return GetTest (&g_oTest);
	}
	int GetTest3 () {
		return GetTest (42);
	}
	const TCHAR *GetLogConfiguration () const {
		return TEXT ("");
	}
};

static void DefaultSetting () {
	CTestSettings oSettings;
	ASSERT (!_tcscmp (oSettings.GetTest1 (), TEXT ("Bar")));
	ASSERT (!_tcscmp (oSettings.GetTest2 (), TEXT ("Foo")));
	ASSERT (oSettings.GetTest3 () == 42);
}

BEGIN_TESTS (AbstractSettingsTest)
	TEST (Location)
	TEST (Caching)
	TEST (Provider)
	TEST (DefaultSetting)
END_TESTS
