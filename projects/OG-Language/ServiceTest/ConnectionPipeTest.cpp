/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Service/ConnectionPipe.cpp

#include "Service/ConnectionPipe.h"
#include "Service/Settings.h"

LOGGING (com.opengamma.language.service.ConnectionPipeTest);

#define TIMEOUT_ABORT	3000
#define TIMEOUT_RETRY	(TIMEOUT_ABORT / 30)
#define TIMEOUT_CLOSE	(TIMEOUT_ABORT / 3)
// These timeouts shouldn't be hit, so are longer than the longest (ABORT) expected
#define TIMEOUT_WRITE	(TIMEOUT_ABORT * 2)
#define TIMEOUT_READ	(TIMEOUT_ABORT * 2)
#define TIMEOUT_JOIN	(TIMEOUT_ABORT * 2)

class CWritingThread : public CThread {
private:
	CMutex m_oMutex;
	TCHAR *m_pszPipeName;
	void *m_pData;
	size_t m_cbData;
	int *m_pnWritten;
	CNamedPipe *m_poPipe;
public:
	void Run () {
		m_oMutex.Enter ();
		while (m_pszPipeName) {
			m_poPipe = CNamedPipe::ClientWrite (m_pszPipeName);
			if (m_poPipe) {
				delete m_pszPipeName;
				m_pszPipeName = NULL;
			} else {
				m_oMutex.Leave ();
				Sleep (TIMEOUT_RETRY);
				m_oMutex.Enter ();
			}
		}
		CNamedPipe *poPipe = m_poPipe;
		m_oMutex.Leave ();
		if (poPipe) {
			if (poPipe->Write (m_pData, m_cbData, TIMEOUT_WRITE)) {
				(*m_pnWritten)++;
			}
		}
	}
	CWritingThread (void *pData, size_t cbData, int *pnWritten) {
		m_pData = pData;
		m_cbData = cbData;
		m_pnWritten = pnWritten;
		CSettings settings;
		size_t len = _tcslen (settings.GetConnectionPipe ()) + 5;
		m_pszPipeName = new TCHAR[len];
		StringCbPrintf (m_pszPipeName, len * sizeof (TCHAR), TEXT ("%sTEST"), settings.GetConnectionPipe ());
		m_poPipe = NULL;
		ASSERT (Start ());
	}
	void Stop () {
		m_oMutex.Enter ();
		CNamedPipe *poPipe;
		if (m_poPipe) {
			LOGDEBUG (TEXT ("Invalidating client connection"));
			poPipe = m_poPipe;
			m_poPipe = NULL;
		} else if (m_pszPipeName) {
			LOGDEBUG (TEXT ("Client never connected"));
			delete m_pszPipeName;
			m_pszPipeName = NULL;
		}
		m_oMutex.Leave ();
		LOGDEBUG (TEXT ("Joining client writing thread"));
		ASSERT (Wait (TIMEOUT_JOIN));
		LOGDEBUG (TEXT ("Client writing thread closed"));
		if (poPipe) {
			delete poPipe;
		}
		Release (this);
	}
};

class CAbortingThread : public CThread {
private:
	CConnectionPipe *m_poPipe;
	CSemaphore m_oSemaphore;
public:
	void Run () {
		if (m_oSemaphore.Wait (TIMEOUT_ABORT)) {
			m_poPipe->Close ();
		}
	}
public:
	CAbortingThread (CConnectionPipe *poPipe) {
		m_poPipe = poPipe;
		ASSERT (Start ());
	}
	void Stop () {
		LOGDEBUG (TEXT ("Signalling abort thread"));
		m_oSemaphore.Signal ();
		LOGDEBUG (TEXT ("Waiting for abort thread"));
		Wait (TIMEOUT_JOIN);
		LOGDEBUG (TEXT ("Abort thread closed"));
	}
};

static void CreateDestroy () {
	CConnectionPipe *po = CConnectionPipe::Create (TEXT ("TEST"));
	ASSERT (po);
	delete po;
}

static void UnicodeMismatch () {
	TODO (__FUNCTION__);
}

static void ConnectionTimeout () {
	TODO (__FUNCTION__);
}

static void LazyCloseBeforeRead () {
	TODO (__FUNCTION__);
}

static void LazyCloseDuringRead () {
	TODO (__FUNCTION__);
}

static void LazyCloseWithWriter () {
	// The writer will complete, we therefore cancel the lazy, and so another read will be terminated by the abort thread, not the lazy close timeout
	TODO (__FUNCTION__);
}

BEGIN_TESTS (ConnectionPipeTest)
	TEST (CreateDestroy)
	TEST (UnicodeMismatch)
	TEST (ConnectionTimeout)
	TEST (LazyCloseBeforeRead)
	TEST (LazyCloseDuringRead)
	TEST (LazyCloseWithWriter)
END_TESTS