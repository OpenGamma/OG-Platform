/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Util/AbstractSettings.h"

LOGGING (com.opengamma.language.util.AbstractSettingsTest);

/// Tests and reports the CAbstractSettings::GetSettingsLocation returns a value.
static void Location () {
	TCHAR szBuffer[256];
	ASSERT (CAbstractSettings::GetSettingsLocation (szBuffer, sizeof (szBuffer)));
	LOGINFO (TEXT ("Settings location = ") << szBuffer);
}

/// Tests the CAbstractSettings::CacheGet and CAbstractSettings::CachePut functions.
/// These are protected functions, so cannot be tested from global scope requiring
/// a test class to extend the CAbstractSettings class.
class CCachingTest : public CAbstractSettings {
public:

	/// Tests the CAbstractSettings::CacheGet and CAbstractSettings::CachePut functions.
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

	/// Dummy implementation.
	const TCHAR *GetLogConfiguration () const {
		// Shouldn't be called
		ASSERT (0);
		return TEXT ("");
	}
};

/// Tests the CAbstractSettings::CacheGet and CAbstractSettings::CachePut functions
static void Caching () {
	CCachingTest settings;
	settings.Run ();
}

/// Tests the CAbstractSettingProvider interface
class CTestSettingProvider : public CAbstractSettingProvider {
protected:

	/// Return the string "Foo"
	///
	/// @param[in] poSettings the settings object
	/// @return the default string
	TCHAR *CalculateString (const CAbstractSettings *poSettings) const {
		return _tcsdup (TEXT ("Foo"));
	}

};

/// Tests the CAbstractSettingProvider interface
static CTestSettingProvider g_oTest;

/// Tests the CAbstractSettingProvider::GetString implementation
static void Provider () {
	const TCHAR *pszTestValue = g_oTest.GetString (NULL);
	ASSERT (pszTestValue);
	ASSERT (!_tcscmp (pszTestValue, TEXT ("Foo")));
}

/// Tests the default values passed to CAbstractSettings::Get methods
class CGetTest : public CAbstractSettings {
private:
	const TCHAR *GetTest (const TCHAR *pszDefault) { return Get (TEXT ("test"), pszDefault); }
	const TCHAR *GetTest (const CTestSettingProvider *poDefault) { return Get (TEXT ("test"), poDefault); }
	int GetTest (int nDefault) { return Get (TEXT ("test"), nDefault); }
public:

	/// Call with a string literal default value ("Bar")
	const TCHAR *GetTest1 () {
		return GetTest (TEXT ("Bar"));
	}

	/// Call with a value from the g_oTest provider ("Foo")
	const TCHAR *GetTest2 () {
		return GetTest (&g_oTest);
	}

	/// Call with an integer literal value (42)
	int GetTest3 () {
		return GetTest (42);
	}

	/// Dummy implementation.
	const TCHAR *GetLogConfiguration () const {
		// Should never be called
		ASSERT (0);
		return TEXT ("");
	}

};

/// Tests the default values passed to CAbstractSettings::Get methods
static void DefaultSetting () {
	CGetTest oSettings;
	ASSERT (!_tcscmp (oSettings.GetTest1 (), TEXT ("Bar")));
	ASSERT (!_tcscmp (oSettings.GetTest2 (), TEXT ("Foo")));
	ASSERT (oSettings.GetTest3 () == 42);
}

/// Tests the enumeration of prefixed keys
class CEnumerateTestHelper : public CAbstractSettings::CEnumerator {
private:
	mutable int m_nSettings;
public:
	CEnumerateTestHelper () {
		m_nSettings = 0;
	}
	void Setting (const TCHAR *pszKey, const TCHAR *pszValue) const {
		LOGDEBUG (TEXT ("Key=") << pszKey << TEXT (", Value=") << pszValue);
		m_nSettings++;
	}
	int GetSettingCount () {
		LOGINFO (TEXT ("Enumerated settings = ") << m_nSettings);
		return m_nSettings;
	}
};

/// Tests the enumeration of prefixed keys
class CEnumerateTest : public CAbstractSettings {
public:

	/// Lookup keys starting jvmProperty. (e.g. jvmProperty.opengamma.configuration.url)
	bool GetJvmProperty () {
		CEnumerateTestHelper oCallback;
		Enumerate (TEXT ("jvmProperty."), &oCallback);
		return oCallback.GetSettingCount () != 0;
	}

	/// Dummy implementation.
	const TCHAR *GetLogConfiguration () const {
		// Should never be called
		ASSERT (0);
		return TEXT ("");
	}

};

/// Tests the search for prefixed keys
static void Enumerate () {
	CEnumerateTest oSettings;
	ASSERT (oSettings.GetJvmProperty ());
}

/// Tests the functions and objects in Util/AbstractSettings.cpp
BEGIN_TESTS (AbstractSettingsTest)
	TEST (Location)
	TEST (Caching)
	TEST (Provider)
	TEST (DefaultSetting)
	TEST (Enumerate)
END_TESTS
