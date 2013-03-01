/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_service_jvm_h
#define __inc_og_language_service_jvm_h

#include <Util/Library.h>
#include <Util/Mutex.h>
#include <Util/Thread.h>
#include "Settings.h"
#include "ErrorFeedback.h"

/// Maintains an embedded JVM and calls methods on the "Main" class to activate the contained Java stack.
class CJVM {
private:

	/// Mutex guarding the other variables.
	mutable CMutex m_oMutex;

	/// The JVM library.
	CLibrary *m_poModule;

	/// The JVM instance. See the Java documentation for more information.
	JavaVM *m_pJVM;

	/// The JNI environment. See the Java documentation for more information.
	JNIEnv *m_pEnv;

	/// Operation thread for asynchronous Start and Stop calls.
	mutable CThread *m_poBusyTask;

	/// TRUE if the Java stack is running, FALSE if it has indicated termination. Note that this is not
	/// the same as JVM termination; the JVM may still be running but the OpenGamma stack may wish to
	/// terminate with one or more non-daemon threads still running elsewhere in the system.
	bool m_bRunning;

	/// Helper class to set system properties on the JVM
	class CProperties : public CAbstractSettings::CEnumerator {
	private:
		JNIEnv *m_pEnv;
	public:
		CProperties (JNIEnv *pEnv) { m_pEnv = pEnv; }
		void SetProperties (const CSettings *poSettings) const;
		void Setting (const TCHAR *pszKey, const TCHAR *pszValue) const;
	};

	CJVM (CLibrary *hModule, JavaVM *pJVM, JNIEnv *pEnv);
	static bool InvokeBool (JNIEnv *pEnv, const char *pszMethod, const char *pszSignature, ...);
	static TCHAR *InvokeString (JNIEnv *pEnv, const char *pszMethod, const char *pszSignature, ...);
	bool InvokeBool (const char *pszMethod);
	TCHAR *InvokeString (const char *pszMethod);
public:
	~CJVM ();
	static CJVM *Create (CErrorFeedback *poFeedback);
	void Start (CErrorFeedback *poFeedback, bool bAsync = true);
	void Stop (bool bAsync = true);
	bool IsBusy (unsigned long dwTimeout) const;
	bool IsRunning () const;
	bool IsStopped () const;
	void UserConnection (const TCHAR *pszUserName, const TCHAR *pszInputPipe, const TCHAR *pszOutputPipe, const TCHAR *pszLanguageID);
	bool Configure ();
};

#endif /* ifndef __inc_og_language_service_jvm_h */
