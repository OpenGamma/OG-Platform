/*
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_abstracttest_h
#define __inc_og_language_util_abstracttest_h

// Generic testing abstraction

#include "Logging.h"

#ifdef __cplusplus_cli
using namespace Microsoft::VisualStudio::TestTools::UnitTesting;
#endif /* ifdef __cplusplus_cli */

/// Generic testing abstraction. When built with CLI for the .NET Testing Framework this class
/// contains only a couple of static utility methods. When built with a standard C++ code
/// generator, this acts as a base class for each testing scenario. The CAbstractTest::Main
/// entry point will invoke each testing scenario in turn.
///
/// Testing scenarios should be created using the macros defined in this header file. They
/// will create CLI classes for the .NET Testing Framework, or a subclass of CAbstractTest
/// otherwise. Subclasses are then instantiated statically and registered globally so that
/// they can be lauched from the entry point function.
class CAbstractTest {
#ifndef __cplusplus_cli
private:

	/// Name of the test class for logging purposes and selecting tests to run manually.
	const TCHAR *m_pszName;

	/// Whether to run the test automatically, or manually only.
	bool m_bAutomatic;

#endif /* ifndef __cplusplus_cli */
public:
#ifndef __cplusplus_cli
	CAbstractTest (bool bAutomatic, const TCHAR *pszName);
	~CAbstractTest ();
	static void Main (int argc, TCHAR **argv);
	virtual void After ();

	/// Implementation of the test class, to call all of the test methods in turn.
	virtual void Run () = 0;

	/// Called before Run and any of the test methods. A subclass may override this to perform
	/// custom setup for the whole testing scenario.
	virtual void BeforeAll () { }

	/// Called by Run before each test method. A subclass may override this to perform custom
	/// setup for the test method.
	virtual void Before () { }

	/// Called after Run and all of the test methods.
	virtual void AfterAll () { }

#endif /* ifndef __cplusplus_cli */

	/// Implementation of a failed assertion. A message should be written to the log explaining
	/// cause of the failure at FATAL level. This delegates to the .NET Testing Framework's
	/// Assert::Fail method if built under CLI, otherwise exits the testing process.
	static void Fail () {
#ifdef __cplusplus_cli
    	Assert::Fail ();
#else
    	exit (1);
#endif /* ifdef __cplusplus_cli */
	}

	static void InitialiseLogs ();
};

/// Asserts the truth of an expression. This is the fundamental component of testing, with all
/// test methods under a scenario creating such assertions to validate the results of executed
/// code. If the assertial fails, the location is written to the log and CAbstractTest::Fail
/// called.
///
/// @param[in] _expr_ the expression to test
#define ASSERT(_expr_) \
	if (!(_expr_)) { \
		LOGFATAL (TEXT ("Assertion ") << __LINE__ << TEXT (" failed")); \
		CAbstractTest::Fail (); \
	}

#ifdef __cplusplus_cli

// Marks the beginning of a testing scenario. For internal use only.
//
// @param[in] automatic true if the test is to run automatically, false for manual selection only
// @param[in] label the name of the test
#define BEGIN_TESTS_(automatic, label) \
	static bool s_bAutomatic = automatic; \
	[TestClass] \
	public ref class C##label { \
	public:

// Marks the beginning of a testing scenario. The test will run whenever the full test suite is
// selected.
//
// @param[in] label the name of the test
#define BEGIN_TESTS(label) BEGIN_TESTS_(true, label)

// Marks the beginning of a testing scenario for manual runs only. The test will not normally run.
//
// @param[in] label the name of the test
#define MANUAL_TESTS(label) BEGIN_TESTS_(false, label)

// Marks a function (in global scope) as a test method for execution within this scenario.
//
// @param[in] proc the test method
#define TEST(proc) \
		[TestMethod] \
		void Test##proc () { \
			CAbstractTest::InitialiseLogs (); \
			if (!s_bAutomatic) { \
				LOGINFO (TEXT ("Skipping test ") << TEXT (#proc)); \
				return; \
			} \
			LOGINFO (TEXT ("Running test ") << TEXT (#proc)); \
			::proc (); \
			LOGINFO (TEXT ("Test ") << TEXT (#proc) << TEXT (" complete")); \
		}

// Marks a function (in global scope) for execution before each test method. This may only be used
// once within the testing scenario and should be placed after the test methods.
//
// @param[in] proc the per-test method initialisation procedure.
#define BEFORE_TEST(proc) \
		[TestInitialize] \
		void Before##proc () { \
			CAbstractTest::InitialiseLogs (); \
			if (!s_bAutomatic) { \
				LOGINFO (TEXT ("Skipping pre-test ") << TEXT (#proc)); \
				return; \
			} \
			LOGINFO (TEXT ("Starting pre-test ") << TEXT (#proc)); \
			::proc (); \
			LOGINFO (TEXT ("Pre-test ") << TEXT (#proc) << TEXT (" complete")); \
		}

// Marks a function (in global scope) for execution after each test method. This may only be used
// once within the testing scenario and should be placed after the test methods.
//
// @param[in] proc the per-test method clean up procedure.
#define AFTER_TEST(proc) \
		[TestCleanup] \
		void After##proc () { \
			if (!s_bAutomatic) { \
				LOGINFO (TEXT ("Skipping post-test ") << TEXT (#proc)); \
				return; \
			} \
			LOGINFO (TEXT ("Starting post-test ") << TEXT (#proc)); \
			::proc (); \
			LOGINFO (TEXT ("Post-test ") << TEXT (#proc) << TEXT (" complete")); \
		}

// Marks a function (in global scope) for execution before any of the test methods in the testing
// scenario. This may only be used once within the testing scenario and should be placed after the
// test methods.
//
// @param[in] proc the initialisation procedure.
#define BEFORE_ALL_TESTS(proc) \
		[ClassInitialize] \
		static void BeforeAll##proc () { \
			CAbstractTest::InitialiseLogs (); \
			if (!s_bAutomatic) { \
				LOGINFO (TEXT ("Skipping before-all ") << TEXT (#proc)); \
				return; \
			} \
			LOGINFO (TEXT ("Starting before-all ") << TEXT (#proc)); \
			::proc (); \
			LOGINFO (TEXT ("Before-all ") << TEXT (#proc) << TEXT (" complete")); \
		}
	
// Marks a function (in global scope) for execution after all of the test methods in the testing
// scenario. This may only be used once within the testing scenario and should be placed after the
// test methods.
//
// @param[in] proc the clean up procedure.
#define AFTER_ALL_TESTS(proc) \
		[ClassCleanup] \
		static void AfterAll##proc () { \
			if (!s_bAutomatic) { \
				LOGINFO (TEXT ("Skipping after-all ") << TEXT (#proc)); \
				return; \
			} \
			LOGINFO (TEXT ("Starting after-all ") << TEXT (#proc)); \
			::proc (); \
			LOGINFO (TEXT ("After-all ") << TEXT (#proc) << TEXT (" complete")); \
		}

// Marks the end of a testing scenario.
#define END_TESTS \
	};

#else /* ifdef __cplusplus_cli */

// Marks the beginning of a testing scenario. For internal use only.
//
// @param[in] automatic true if the test is to run automatically, false for manual selection only
// @param[in] label the name of the test
#define BEGIN_TESTS_(automatic, label) \
	static class C##label : public CAbstractTest { \
	public: \
		C##label () : CAbstractTest (automatic, TEXT (#label)) { } \
		void Run () { \
			LOGINFO (TEXT ("Beginning ") << TEXT (#label));

// Marks the beginning of a testing scenario. The test will run whenever the full test suite is
// selected.
//
// @param[in] label the name of the test
#define BEGIN_TESTS(label) BEGIN_TESTS_(true, label)

// Marks the beginning of a testing scenario for manual runs only. The test will not normally run.
//
// @param[in] label the name of the test
#define MANUAL_TESTS(label) BEGIN_TESTS_(false, label)

// Marks a function (in global scope) as a test method for execution within this scenario.
//
// @param[in] proc the test method
#define TEST(proc) \
			LOGINFO (TEXT ("Running test ") << TEXT (#proc)); \
			Before (); \
			::proc (); \
			After (); \
			LOGINFO (TEXT ("Test ") << TEXT (#proc) << TEXT (" complete"));

// Marks a function (in global scope) for execution before each test method. This may only be used
// once within the testing scenario and must be placed after the test methods.
//
// @param[in] proc the per-test method initialisation procedure
#define BEFORE_TEST(proc) \
		} \
		void Before () { \
			LOGDEBUG (TEXT ("Starting pre-test ") << TEXT (#proc)); \
			::proc (); \
			LOGDEBUG (TEXT ("Pre-test ") << TEXT (#proc) << TEXT (" complete"));

// Marks a function (in global scope) for execution after each test method. This may only be used
// once within the testing scenario and must be placed after the test methods.
//
// @param[in] proc the per-test method clean up procedure
#define AFTER_TEST(proc) \
		} \
		void After () { \
			LOGDEBUG (TEXT ("Starting post-test ") << TEXT (#proc)); \
			::proc (); \
			LOGDEBUG (TEXT ("Post-test ") << TEXT (#proc) << TEXT (" complete")); \
			CAbstractTest::After ();

// Marks a function (in global scope) for execution before any of the test methods in the testing
// scenario. This may only be used once within the testing scenario and must be placed after the
// test methods.
//
// @param[in] proc the initialisation procedure
#define BEFORE_ALL_TESTS(proc) \
		} \
		void BeforeAll () { \
			LOGDEBUG (TEXT ("Starting before-all ") << TEXT (#proc)); \
			::proc (); \
			LOGDEBUG (TEXT ("Before-all ") << TEXT (#proc) << TEXT (" complete"));

// Marks a function (in global scope) for execution after all of the test methods in the testing
// scenario. This may only be used once within the testing scenario and must be placed after the
// test methods.
//
// @param[in] proc the clean u pprocedure
#define AFTER_ALL_TESTS(proc) \
		} \
		void AfterAll () { \
			LOGDEBUG (TEXT ("Starting after-all ") << TEXT (#proc)); \
			::proc (); \
			LOGDEBUG (TEXT ("After-all ") << TEXT (#proc) << TEXT (" complete"));

// Marks the end of a testing scenario
#define END_TESTS \
		} \
	} g_o##__LINE__;

#endif /* ifdef __cplusplus_cli */

#endif /* ifndef __inc_og_language_util_abstracttest_h */
