/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_service_jvm_h
#define __inc_og_language_service_jvm_h

// Start up an embedded JVM, and call methods on the "Main" class

class CJVM {
private:
	CRITICAL_SECTION m_cs;
	LIBRARY_HANDLE m_hModule;
	JavaVM *m_pJVM;
	JNIEnv *m_pEnv;
	THREAD_HANDLE m_hBusyTask;
	bool m_bRunning;
	CJVM (LIBRARY_HANDLE hModule, JavaVM *pJVM, JNIEnv *pEnv);
	static bool Invoke (JNIEnv *pEnv, const char *pszMethod, const char *pszSignature, ...);
	bool Invoke (const char *pszMethod);
	static THREADPROC_RETURN THREADPROC_DECLTYPE StartProc (void *pObject);
	static THREADPROC_RETURN THREADPROC_DECLTYPE StopProc (void *pObject);
public:
	~CJVM ();
	static CJVM *Create ();
	void Start ();
	void Stop ();
	bool IsBusy (unsigned long dwTimeout);
	bool IsRunning ();
	bool IsStopped ();
	void UserConnection (const TCHAR *pszUserName, const TCHAR *pszInputPipe, const TCHAR *pszOutputPipe);
};

#endif /* ifndef __inc_og_language_service_jvm_h */