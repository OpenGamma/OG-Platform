/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Threads using either Win32 or APR

#include "Logging.h"
#include "Thread.h"

LOGGING (com.opengamma.language.util.Thread);

#ifdef _WIN32
DWORD CThread::StartProc (void *pObject) {
#else
void *CThread::StartProc (apr_thread_t *handle, void *pObject) {
#endif
	CThread *poThread = (CThread*)pObject;
	poThread->Run ();
#ifndef _WIN32
	poThread->m_oTerminate.Signal ();
#endif
	CThread::Release (poThread);
// TODO: need to support a FreeLibraryAndExitThread in Win32
	return 0;
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
