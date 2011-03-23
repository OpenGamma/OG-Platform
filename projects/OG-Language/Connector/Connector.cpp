/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Main connector API

#include "Connector.h"
#define FUDGE_NO_NAMESPACE
#include "com_opengamma_language_connector_UserMessage.h"
#include <Util/Error.h>

LOGGING (com.opengamma.language.connector.Connector);

class CConnectorMessageDispatch : public CAsynchronous::COperation {
private:
	CConnector::CCallbackEntry *m_poCallback;
	FudgeMsg m_msg;
public:
	CConnectorMessageDispatch (CConnector::CCallbackEntry *poCallback, FudgeMsg msg)
	: COperation () {
		poCallback->Retain ();
		m_poCallback = poCallback;
		FudgeMsg_retain (msg);
		m_msg = msg;
	}
	~CConnectorMessageDispatch () {
		CConnector::CCallbackEntry::Release (m_poCallback);
		FudgeMsg_release (m_msg);
	}
	void Run () {
		m_poCallback->OnMessage (m_msg);
	}
};

class CConnectorThreadDisconnectDispatch : public CAsynchronous::COperation {
private:
	CConnector::CCallbackEntry *m_poCallback;
public:
	CConnectorThreadDisconnectDispatch (CConnector::CCallbackEntry *poCallback)
	: COperation (true) {
		poCallback->Retain ();
		m_poCallback = poCallback;
	}
	~CConnectorThreadDisconnectDispatch () {
		CConnector::CCallbackEntry::Release (m_poCallback);
	}
	void Run () {
		m_poCallback->OnThreadDisconnect ();
	}
};

class CConnectorDispatcher : public CAsynchronous {
private:
	CConnector *m_poConnector;
	CConnectorDispatcher (CConnector *poConnector) : CAsynchronous () {
		poConnector->Retain ();
		m_poConnector = poConnector;
	}
	~CConnectorDispatcher () {
		CConnector::Release (m_poConnector);
	}
protected:
	void OnThreadExit () {
		m_poConnector->OnDispatchThreadDisconnect ();
	}
public:
	static CConnectorDispatcher *Create (CConnector *poConnector) {
		return new CConnectorDispatcher (poConnector);
	}
};

void CConnector::CCallbackEntry::OnMessage (FudgeMsg msgPayload) {
	if (m_strClass) {
		m_poCallback->OnMessage (msgPayload);
	} else {
		LOGDEBUG (TEXT ("Callback object unregistered, discarding message payload"));
	}
}

void CConnector::CCallbackEntry::OnThreadDisconnect () {
	m_poCallback->OnThreadDisconnect ();
}

void CConnector::OnStateChange (ClientServiceState ePreviousState, ClientServiceState eNewState) {
	LOGDEBUG (TEXT ("State changed from ") << ePreviousState << TEXT (" to ") << eNewState);
	if (eNewState == RUNNING) {
		LOGINFO (TEXT ("Entered running state"));
		// Make sure all of the semaphores for synchronous calls are "unsignalled" (all get signalled when the client stops)
		m_oSynchronousCalls.ClearAllSemaphores ();
		// If in "startup" mode then signal the startup semaphore to release any waiting threads
		CSemaphore *poSemaphore = m_oStartupSemaphorePtr.GetAndSet (NULL);
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
		CSemaphore *poSemaphore = m_oStartupSemaphorePtr.GetAndSet (NULL);
		if (poSemaphore) {
			poSemaphore->Signal ();
			m_oStartupSemaphorePtr.Set (poSemaphore);
		}
	}
}

void CConnector::OnMessageReceived (FudgeMsg msg) {
	fudge_i32 handle;
	FudgeMsg msgPayload;
	if (UserMessage_getFudgeMsgPayload (msg, &msgPayload) != FUDGE_OK) {
		LOGWARN (TEXT ("Message didn't contain a payload"));
		return;
	}
	if (UserMessage_getHandle (msg, &handle) == FUDGE_OK) {
		m_oSynchronousCalls.PostAndRelease (handle, msgPayload);
	} else {
		FudgeField field;
		if ((FudgeMsg_getFieldByOrdinal (&field, msgPayload, 0) == FUDGE_OK) && (field.type == FUDGE_TYPE_STRING)) {
			m_oControlMutex.Enter ();
			CCallbackEntry *poCallback = m_poCallbacks;
			while (poCallback) {
				if (poCallback->IsClass (field.data.string)) {
					LOGDEBUG (TEXT ("Dispatching message to user callback"));
					CAsynchronous::COperation *poDispatch = new CConnectorMessageDispatch (poCallback, msgPayload);
					if (poDispatch) {
						poCallback->m_bUsed = true;
						if (!m_poDispatch->Run (poDispatch)) {
							delete poDispatch;
							LOGWARN (TEXT ("Couldn't dispatch message to user callback"));
						}
					} else {
						LOGFATAL (TEXT ("Out of memory"));
					}
					// Stop on first matching callback -- is it ever useful to register multiple callbacks for the same message class?
					break;
				}
				poCallback = poCallback->m_poNext;
			}
			m_oControlMutex.Leave ();
		} else {
			LOGWARN (TEXT ("Message didn't have class string at ordinal 0"));
		}
		FudgeMsg_release (msgPayload);
	}
}

void CConnector::OnDispatchThreadDisconnect () {
	LOGINFO (TEXT ("Dispatcher thread disconnected"));
	m_oControlMutex.Enter ();
	CCallbackEntry *poCallback = m_poCallbacks;
	while (poCallback) {
		poCallback->OnThreadDisconnect ();
		poCallback = poCallback->m_poNext;
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

bool CConnector::CCall::Cancel () {
	if (!m_poSlot) {
		SetLastError (EALREADY);
		return false;
	}
	m_poSlot->Release ();
	m_poSlot = NULL;
	return true;
}

bool CConnector::CCall::WaitForResult (FudgeMsg *pmsgResponse, unsigned long lTimeout) {
	if (!m_poSlot) {
		SetLastError (EALREADY);
		return false;
	}
	FudgeMsg msg = m_poSlot->GetMessage (lTimeout);
	m_poSlot->Release ();
	m_poSlot = NULL;
	if (msg) {
		*pmsgResponse = msg;
		return true;
	} else {
		return false;
	}
}

CConnector::CConnector (CClientService *poClient)
: m_oRefCount (1) {
	LOGINFO (TEXT ("Connector instance created"));
	m_poClient = poClient;
	poClient->SetMessageReceivedCallback (this);
	poClient->SetStateChangeCallback (this);
	m_poCallbacks = NULL;
	m_poDispatch = CConnectorDispatcher::Create (this);
}

CConnector::~CConnector () {
	LOGINFO (TEXT ("Connector instance destroyed"));
	assert (!m_oRefCount.Get ());
	LOGDEBUG (TEXT ("Unregistering callback handlers"));
	m_poClient->SetMessageReceivedCallback (NULL);
	m_poClient->SetStateChangeCallback (NULL);
	assert (!m_oStartupSemaphorePtr.Get ());
	while (m_poCallbacks) {
		CCallbackEntry *poCallback = m_poCallbacks;
		m_poCallbacks = poCallback->m_poNext;
		CCallbackEntry::Release (poCallback);
	}
	if (m_poDispatch) {
		LOGDEBUG (TEXT ("Poisoning asynchronous dispatch"));
		CAsynchronous::PoisonAndRelease (m_poDispatch);
	}
	LOGDEBUG (TEXT ("Releasing client"));
	CClientService::Release (m_poClient);
}

CConnector *CConnector::Start (const TCHAR *pszLanguage) {
	CClientService *poClient = CClientService::Create (pszLanguage);
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
	bool bResult = m_poClient->Stop ();
	if (m_poDispatch) {
		LOGDEBUG (TEXT ("Poisoning asynchronous dispatch"));
		CAsynchronous::PoisonAndRelease (m_poDispatch);
		m_poDispatch = NULL;
	}
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
	CSemaphore *poStartupSemaphore = m_oStartupSemaphorePtr.GetAndSet (NULL);
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
		if (error == ENOTCONN) {
			LOGDEBUG (TEXT ("Not connected; waiting for startup (or restart) to complete"));
			if (WaitForStartup (lTimeout)) {
				poOverlapped = Call (msgPayload);
				if (!poOverlapped) {
					error = GetLastError ();
					LOGWARN (TEXT ("Couldn't initiate call, error ") << error);
					SetLastError (error);
					return false;
				}
			} else {
				LOGWARN (TEXT ("Couldn't initiate call - not connected"));
				SetLastError (error);
				return false;
			}
		} else {
			LOGWARN (TEXT ("Couldn't initiate call, error ") << error);
			SetLastError (error);
			return false;
		}
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
	FudgeMsg msg;
	if (FudgeMsg_create (&msg) != FUDGE_OK) {
		SetLastError (ENOMEM);
		return false;
	}
	if ((handle && (UserMessage_setHandle (msg, handle) != FUDGE_OK)) || (UserMessage_setFudgeMsgPayload (msg, msgPayload) != FUDGE_OK)) {
		FudgeMsg_release (msg);
		SetLastError (ENOMEM);
		return false;
	}
	bool bResult = poClient->Send (msg);
	FudgeMsg_release (msg);
	return bResult;
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
	if (_SendMessage (m_poClient, handle, msgPayload)) {
		LOGDEBUG (TEXT ("Message sent on slot ") << poSlot->GetIdentifier () << TEXT (" with handle ") << handle);
		return new CCall (poSlot);
	} else {
		int error = GetLastError ();
		LOGWARN (TEXT ("Couldn't send message on slot ") << poSlot->GetIdentifier () << TEXT (", error ") << error);
		poSlot->Release ();
		SetLastError (error);
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
	m_oControlMutex.Enter ();
	m_poCallbacks = new CCallbackEntry (strClass, poCallback, m_poCallbacks);
	m_oControlMutex.Leave ();
	return true;
}

// After the callback is removed, there may still be entries in the queue for it. The reference to the callback
// will only be discarded after a OnThreadDisconnect has been sent to it.
bool CConnector::RemoveCallback (CCallback *poCallback) {
	if (!poCallback) {
		LOGWARN (TEXT ("Null callback object"));
		return false;
	}
	bool bFound = false;
	m_oControlMutex.Enter ();
	CCallbackEntry **ppoPrevious = &m_poCallbacks;
	CCallbackEntry *poEntry = m_poCallbacks;
	while (poEntry) {
		if (poEntry->IsCallback (poCallback)) {
			*ppoPrevious = poEntry->m_poNext;
			poEntry->FreeString ();
			if (poEntry->m_bUsed) {
				CAsynchronous::COperation *poDispatch = new CConnectorThreadDisconnectDispatch (poEntry);
				if (poDispatch) {
					if (!m_poDispatch->Run (poDispatch)) {
						delete poDispatch;
						LOGWARN (TEXT ("Couldn't dispatch disconnect message"));
					}
				} else {
					LOGFATAL (TEXT ("Out of memory"));
				}
			}
			CCallbackEntry::Release (poEntry);
			bFound = true;
			break;
		}
		ppoPrevious = &poEntry->m_poNext;
		poEntry = poEntry->m_poNext;
	}
	m_oControlMutex.Leave ();
	return bFound;
}
