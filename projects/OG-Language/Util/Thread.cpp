/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Threads using either Win32 or APR

#include "Thread.h"

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
	return 0;
}