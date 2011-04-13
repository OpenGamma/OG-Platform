/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Threads using either Win32 or APR

#include "Logging.h"
#include "Thread.h"

LOGGING (com.opengamma.language.util.Thread);

#ifndef _WIN32
CAtomicInt CThread::s_oNextThreadId;
#endif /* ifndef _WIN32 */

#ifdef _WIN32
DWORD CThread::StartProc (void *pObject) {
#else
void *CThread::StartProc (apr_thread_t *handle, void *pObject) {
#endif
	CThread *poThread = (CThread*)pObject;
	poThread->Run ();
#ifndef _WIN32
	poThread->m_oTerminate.Signal ();
#else /* ifndef _WIN32 */
	HMODULE hModule = poThread->m_hModule;
	poThread->m_hModule = NULL;
#endif /* ifndef _WIN32 */
	CThread::Release (poThread);
#ifdef _WIN32
	if (hModule) {
		FreeLibraryAndExitThread (hModule, 0);
	}
#endif /* ifndef _WIN32 */
	return 0;
}

bool CThread::Start () {
#ifdef _WIN32
	assert (!m_hThread);
	if (!GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS, (LPCTSTR)CThread::StartProc, &m_hModule)) {
		return false;
	}
	Retain ();
	m_hThread = CreateThread (NULL, 0, StartProc, this, 0, (PDWORD)&m_nThreadId);
	if (!m_hThread) {
		FreeLibrary (m_hModule);
		m_hModule = NULL;
		Release (this);
		return false;
	}
#else
	assert (!m_pThread);
	apr_threadattr_t *pAttr;
	if (!PosixLastError (apr_threadattr_create (&pAttr, m_oPool))) return false;
	Retain ();
	if (!PosixLastError (apr_thread_create (&m_pThread, pAttr, StartProc, this, m_oPool))) {
		Release (this);
		return false;
	}
	m_nThreadId = s_oNextThreadId.IncrementAndGet ();
#endif
	return true;
}

#ifndef _WIN32
static void _IgnoreSignal (int signal) {
	LOGDEBUG (TEXT ("Signal ") << signal << TEXT (" ignored"));
}
class CSuppressSignals {
public:
	CSuppressSignals () {
		sigset (SIGALRM, _IgnoreSignal); // Used to interrupt blocked threads
		sigset (SIGPIPE, _IgnoreSignal); // Used by NamedPipe transport
	}
};
CSuppressSignals g_oSuppressSignals;
#endif
