/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Main connector API

#include "Public.h"

LOGGING (com.opengamma.language.connector.Connector);

#define ASSERT_CORRECT_LOGIC	// Comment this out to ignore message delivery faults in DEBUG mode

#ifdef ASSERT_CORRECT_LOGIC
#define LOGICAL_FAULT(_msg_) { LOGFATAL(_msg_); assert (0); }
#else
#define LOGICAL_FAULT(_msg_) LOGERROR(_msg_)
#endif

void CConnector::OnStateChange (ClientServiceState ePreviousState, ClientServiceState eNewState) {
	LOGDEBUG (TEXT ("State changed from ") << ePreviousState << TEXT (" to ") << eNewState);
	if (eNewState == RUNNING) {
		LOGINFO (TEXT ("Entered running state"));
		// Make sure all of the semaphores for synchronous calls are "unsignalled" (all get signalled when the client stops)
		m_oSynchronousCalls.ClearAllSemaphores ();
		// If in "startup" mode then signal the startup semaphore to release any waiting threads
		CSemaphore *poSemaphore = (CSemaphore*)m_oStartupSemaphorePtr.GetAndSet (NULL);
		if (poSemaphore) {
			poSemaphore->Signal ();
			m_oStartupSemaphorePtr.Set (poSemaphore);
		}
	} else if (ePreviousState == RUNNING) {
		LOGINFO (TEXT ("Left running state"));
		// No longer running, so signal any message semaphores
		m_oSynchronousCalls.SignalAllSemaphores ();
	} else if ((eNewState == STOPPED ) || (eNewState == ERRORED)) {
		LOGINFO (TEXT ("Entered stable non-running state"));
		// If in "startup" mode then signal the startup semaphore to release any waiting threads
		CSemaphore *poSemaphore = (CSemaphore*)m_oStartupSemaphorePtr.GetAndSet (NULL);
		if (poSemaphore) {
			poSemaphore->Signal ();
			m_oStartupSemaphorePtr.Set (poSemaphore);
		}
	}
}

void CConnector::OnMessageReceived (FudgeMsg msg) {
	FudgeField field;
	if ((FudgeMsg_getFieldByOrdinal (&field, msg, 0) != FUDGE_OK)
		|| (field.type != FUDGE_TYPE_STRING)) {
		LOGWARN (TEXT ("Message didn't have class string at ordinal 0"));
		return;
	}
	m_oControlMutex.Enter ();
	struct _callbackEntry *pCallback = m_pCallbacks;
	while (pCallback) {
		if (!FudgeString_compare (pCallback->strClass, field.data.string)) {
			TODO (TEXT ("Dispatch message to user callback"));
		}
		pCallback = pCallback->pNext;
	}
	m_oControlMutex.Leave ();
}

CConnector::CCall::CCall (CSynchronousCallSlot *poSlot) {
	m_poSlot = poSlot;
}

CConnector::CCall::~CCall () {
	if (m_poSlot) {
		m_poSlot->Release ();
	}
}

// TODO: if the slot is released in Cancel or WaitForResult, set it to null!

bool CConnector::CCall::Cancel () {
	TODO (__FUNCTION__);
	return false;
}

bool CConnector::CCall::WaitForResult (FudgeMsg *pmsgResponse, unsigned long lTimeout) {
	TODO (__FUNCTION__);
	return false;
}

CConnector::CConnector (CClientService *poClient)
: m_oRefCount (1) {
	LOGINFO (TEXT ("Connector instance created"));
	m_poClient = poClient;
	poClient->SetMessageReceivedCallback (this);
	poClient->SetStateChangeCallback (this);
	m_pCallbacks = NULL;
}

CConnector::~CConnector () {
	LOGINFO (TEXT ("Connector instance destroyed"));
	assert (!m_oRefCount.Get ());
	LOGDEBUG (TEXT ("Unregistering callback handlers"));
	m_poClient->SetMessageReceivedCallback (NULL);
	m_poClient->SetStateChangeCallback (NULL);
	assert (!m_oStartupSemaphorePtr.Get ());
	while (m_pCallbacks) {
		struct _callbackEntry *pCallback = m_pCallbacks;
		m_pCallbacks = pCallback->pNext;
		FudgeString_release (pCallback->strClass);
		delete pCallback;
	}
	LOGDEBUG (TEXT ("Releasing client"));
	CClientService::Release (m_poClient);
}

CConnector *CConnector::Start () {
	CClientService *poClient = CClientService::Create ();
	if (!poClient) {
		LOGERROR (TEXT ("Couldn't create client"));
		return NULL;
	}
	CConnector *poConnector = new CConnector (poClient);
	if (poClient->Start ()) {
		return poConnector;
	} else {
		LOGERROR (TEXT ("Couldn't start client service, error ") << GetLastError ());
		CConnector::Release (poConnector);
		return NULL;
	}
}

bool CConnector::Stop () {
	m_oControlMutex.Enter ();
	// TODO: Release any other resources
	bool bResult = m_poClient->Stop ();
	m_oControlMutex.Leave ();
	return bResult;
}

bool CConnector::WaitForStartup (unsigned long lTimeout) {
	CSemaphore oStartupSemaphore (0, 1);
	m_oControlMutex.Enter ();
	m_oStartupSemaphorePtr.Set (&oStartupSemaphore);
	ClientServiceState eState = m_poClient->GetState ();
	if ((eState != RUNNING) && (eState != STOPPED) && (eState != ERRORED)) {
		LOGINFO (TEXT ("Waiting for client startup"));
		oStartupSemaphore.Wait (lTimeout);
	}
retryLock:
	CSemaphore *poStartupSemaphore = (CSemaphore*)m_oStartupSemaphorePtr.GetAndSet (NULL);
	if (poStartupSemaphore) {
		assert (poStartupSemaphore == &oStartupSemaphore);
	} else {
		// The state change callback has the pointer, wait for it to release it
		LOGDEBUG (TEXT ("Waiting for state change callback to release pointer"));
		CThread::Yield ();
		goto retryLock;
	}
	m_oControlMutex.Leave ();
	eState = m_poClient->GetState ();
	LOGDEBUG (TEXT ("Client is in state ") << eState);
	return eState == RUNNING;
}

bool CConnector::Call (FudgeMsg msgPayload, FudgeMsg *pmsgResponse, unsigned long lTimeout) {
	if (!msgPayload) {
		LOGWARN (TEXT ("Null message payload"));
		SetLastError (EINVAL);
		return false;
	}
	if (!pmsgResponse) {
		LOGWARN (TEXT ("Null message response pointer"));
		SetLastError (EINVAL);
		return false;
	}
	CCall *poOverlapped = Call (msgPayload);
	if (!poOverlapped) {
		int error = GetLastError ();
		LOGWARN (TEXT ("Couldn't initiate call, error ") << error);
		SetLastError (error);
		return false;
	}
	if (poOverlapped->WaitForResult (pmsgResponse, lTimeout)) {
		delete poOverlapped;
		return true;
	} else {
		int error = GetLastError ();
		LOGWARN (TEXT ("Couldn't get result, error ") << error);
		poOverlapped->Cancel ();
		delete poOverlapped;
		SetLastError (error);
		return false;
	}
}

static bool _SendMessage (CClientService *poClient, fudge_i32 handle, FudgeMsg msgPayload) {
	TODO (TEXT ("Send message with handle ") << handle);
	return false;
}

CConnector::CCall *CConnector::Call (FudgeMsg msgPayload) {
	if (!msgPayload) {
		LOGWARN (TEXT ("Null message"));
		SetLastError (EINVAL);
		return NULL;
	}
	CSynchronousCallSlot *poSlot = m_oSynchronousCalls.Acquire ();
	if (!poSlot) {
		int error = GetLastError ();
		LOGWARN (TEXT ("Couldn't acquire call slot, error ") << error);
		SetLastError (error);
		return NULL;
	}
	fudge_i32 handle = poSlot->GetHandle ();
	FudgeMsg msg = poSlot->GetMessage ();
	if (msg) {
		LOGICAL_FAULT (TEXT ("Stale message found in slot ") << poSlot->GetIdentifier ());
		FudgeMsg_release (msg);
	}
	if (_SendMessage (m_poClient, handle, msgPayload)) {
		LOGDEBUG (TEXT ("Message sent on slot ") << poSlot->GetIdentifier () << TEXT (" with handle ") << handle);
		return new CCall (poSlot);
	} else {
		LOGWARN (TEXT ("Couldn't send message on slot ") << poSlot->GetIdentifier ());
		poSlot->Release ();
		return NULL;
	}
}

bool CConnector::Send (FudgeMsg msgPayload) {
	if (!msgPayload) {
		LOGWARN (TEXT ("Null payload"));
		return false;
	}
	return _SendMessage (m_poClient, 0, msgPayload);
}

bool CConnector::AddCallback (const TCHAR *pszClass, CCallback *poCallback) {
	if (!pszClass) {
		LOGWARN (TEXT ("Null class name"));
		return false;
	}
	if (!poCallback) {
		LOGWARN (TEXT ("Null callback object"));
		return false;
	}
	FudgeString strClass;
	if (
#ifdef _UNICODE
	FudgeString_createFromUTF16 (&strClass, (fudge_byte*)pszClass, sizeof (TCHAR) * _tcslen (pszClass))
#else
	FudgeString_createFromASCIIZ (&strClass, pszClass)
#endif
	!= FUDGE_OK) {
		LOGERROR (TEXT ("Couldn't create Fudge string from ") << pszClass);
		return false;
	}
	struct _callbackEntry *pEntry = new struct _callbackEntry;
	pEntry->poCallback = poCallback;
	pEntry->strClass = strClass;
	m_oControlMutex.Enter ();
	pEntry->pNext = m_pCallbacks;
	m_pCallbacks = pEntry;
	m_oControlMutex.Leave ();
	return true;
}

bool CConnector::RemoveCallback (CCallback *poCallback) {
	if (!poCallback) {
		LOGWARN (TEXT ("Null callback object"));
		return false;
	}
	bool bFound = false;
	m_oControlMutex.Enter ();
	struct _callbackEntry **ppPrevious = &m_pCallbacks;
	struct _callbackEntry *pEntry = m_pCallbacks;
	while (pEntry) {
		if (pEntry->poCallback == poCallback) {
			*ppPrevious = pEntry->pNext;
			FudgeString_release (pEntry->strClass);
			delete pEntry;
			bFound = true;
			break;
		}
		ppPrevious = &pEntry->pNext;
		pEntry = pEntry->pNext;
	}
	m_oControlMutex.Leave ();
	return bFound;
}