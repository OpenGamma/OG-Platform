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
		CAsynchronous::OnThreadExit ();
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

static void _GetRunAndRestore (CAtomicPointer<IRunnable*> *poPtr) {
	IRunnable *poRunnable = poPtr->GetAndSet (NULL);
	if (poRunnable) {
		LOGDEBUG (TEXT ("Calling user extension"));
		poRunnable->Run ();
		if (poPtr->CompareAndSet (poRunnable, NULL) != NULL) {
			LOGDEBUG (TEXT ("Deleting replaced user extension"));
			delete poRunnable;
		}
	}
}

void CConnector::OnEnterRunningState () {
	LOGINFO (TEXT ("Entered running state"));
	// Make sure all of the semaphores for synchronous calls are "unsignalled" (all get signalled when the client stops)
	m_oSynchronousCalls.ClearAllSemaphores ();
	// If in "startup" mode then signal the startup semaphore to release any waiting threads
	CSemaphore *poSemaphore = m_oStartupSemaphorePtr.GetAndSet (NULL);
	if (poSemaphore) {
		poSemaphore->Signal ();
		m_oStartupSemaphorePtr.Set (poSemaphore);
	}
	_GetRunAndRestore (&m_oOnEnterRunningState);
}

void CConnector::OnExitRunningState () {
	LOGINFO (TEXT ("Left running state"));
	// No longer running, so signal any message semaphores
	m_oSynchronousCalls.SignalAllSemaphores ();
	_GetRunAndRestore (&m_oOnExitRunningState);
}

void CConnector::OnEnterStableNonRunningState () {
	LOGINFO (TEXT ("Entered stable non-running state"));
	// If in "startup" mode then signal the startup semaphore to release any waiting threads
	CSemaphore *poSemaphore = m_oStartupSemaphorePtr.GetAndSet (NULL);
	if (poSemaphore) {
		poSemaphore->Signal ();
		m_oStartupSemaphorePtr.Set (poSemaphore);
	}
	_GetRunAndRestore (&m_oOnEnterStableNonRunningState);
}

void CConnector::OnStateChange (ClientServiceState ePreviousState, ClientServiceState eNewState) {
	LOGDEBUG (TEXT ("State changed from ") << ePreviousState << TEXT (" to ") << eNewState);
	if (eNewState == RUNNING) {
		OnEnterRunningState ();
	} else if (ePreviousState == RUNNING) {
		// NOTE: there are no transitions from RUNNING to STOPPED or ERRORED; must go via POISONED or STOPPING
		OnExitRunningState ();
	} else if ((eNewState == STOPPED ) || (eNewState == ERRORED)) {
		OnEnterStableNonRunningState ();
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
		int nFields = FudgeMsg_numFields (msgPayload);
		FudgeField field[8];
		FudgeField *pField;
		if (nFields <= (sizeof (field) / sizeof (FudgeField))) {
			pField = field;
		} else {
			LOGDEBUG (TEXT ("Allocating buffer for ") << nFields << TEXT (" fields"));
			pField = (FudgeField*)malloc (sizeof (FudgeField) * nFields);
			if (!pField) {
				LOGFATAL (TEXT ("Out of memory"));
				FudgeMsg_release (msgPayload);
				return;
			}
		}
		if (FudgeMsg_getFields (pField, nFields, msgPayload) > 0) {
			int i;
			m_oMutex.Enter ();
			for (i = 0; i < nFields; i++) {
				if ((pField[i].flags & FUDGE_FIELD_HAS_ORDINAL) && (pField[i].ordinal == 0) && (pField[i].type == FUDGE_TYPE_STRING)) {
					CCallbackEntry *poCallback = m_poCallbacks;
					while (poCallback) {
						if (poCallback->IsClass (pField[i].data.string)) {
							LOGDEBUG (TEXT ("Dispatching message to user callback"));
							CAsynchronous::COperation *poDispatch = new CConnectorMessageDispatch (poCallback, msgPayload);
							if (poDispatch) {
								if (!m_poDispatch->Run (poDispatch)) {
									delete poDispatch;
									LOGWARN (TEXT ("Couldn't dispatch message to user callback"));
								}
							} else {
								LOGFATAL (TEXT ("Out of memory"));
							}
							// Stop of first matching callback
							goto dispatched;
						}
						poCallback = poCallback->m_poNext;
					}
				}
			}
			LOGWARN (TEXT ("Ignoring message"));
dispatched:
			m_oMutex.Leave ();
		} else {
			LOGWARN (TEXT ("Couldn't fetch fields from message payload"));
		}
		if (pField != field) {
			free (pField);
		}
		FudgeMsg_release (msgPayload);
	}
}

void CConnector::OnDispatchThreadDisconnect () {
	LOGINFO (TEXT ("Dispatcher thread disconnected"));
	m_oMutex.Enter ();
	if (m_poDispatch) {
		CCallbackEntry *poCallback = m_poCallbacks;
		while (poCallback) {
			poCallback->OnThreadDisconnect ();
			poCallback = poCallback->m_poNext;
		}
	} else {
		LOGDEBUG (TEXT ("Thread disconnect messages already sent at stop"));
	}
	m_oMutex.Leave ();
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
		// Note: If there is still a m_poDispatch we could issue a disconnect. However if the
		// user was too naughty not to call Stop() or remove the callbacks before deleting
		// then they don't really deserve the notifications. More specifically if the sequence
		// of execution breaks to that point then the notifications probably aren't going to
		// help the recovery and are probably best not sent.
		m_poCallbacks = poCallback->m_poNext;
		CCallbackEntry::Release (poCallback);
	}
	if (m_poDispatch) {
		LOGDEBUG (TEXT ("Poisoning asynchronous dispatch"));
		CAsynchronous::PoisonAndRelease (m_poDispatch);
	}
	LOGDEBUG (TEXT ("Releasing client"));
	CClientService::Release (m_poClient);
	OnEnterRunningState (NULL);
	OnExitRunningState (NULL);
	OnEnterStableNonRunningState (NULL);
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
	m_oMutex.Enter ();
	bool bResult = m_poClient->Stop ();
	if (bResult && m_poDispatch) {
		// The dispatch will later call back to OnThreadDisconnect, but this may be too late if there
		// are callbacks removed before then. Setting m_poDispatch to NULL will suppress the calls
		// made from there, and also from the RemoveCallback method. Instead the disconnects are
		// injected before we submit the poison.
		LOGDEBUG (TEXT ("Scheduling disconnect messages to callbacks"));
		CCallbackEntry *poEntry = m_poCallbacks;
		while (poEntry) {
			CAsynchronous::COperation *poDispatch = new CConnectorThreadDisconnectDispatch (poEntry);
			if (poDispatch) {
				if (!m_poDispatch->Run (poDispatch)) {
					delete poDispatch;
					LOGWARN (TEXT ("Couldn't dispatch disconnect message"));
				}
			} else {
				LOGFATAL (TEXT ("Out of memory"));
			}
			poEntry = poEntry->m_poNext;
		}
		LOGDEBUG (TEXT ("Poisoning asynchronous dispatch"));
		CAsynchronous::PoisonAndRelease (m_poDispatch);
		m_poDispatch = NULL;
	}
	m_oMutex.Leave ();
	return bResult;
}

bool CConnector::WaitForStartup (unsigned long lTimeout) {
	CSemaphore oStartupSemaphore (0, 1);
	m_oMutex.Enter ();
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
	m_oMutex.Leave ();
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
	m_oMutex.Enter ();
	m_poCallbacks = new CCallbackEntry (strClass, poCallback, m_poCallbacks);
	m_oMutex.Leave ();
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
	m_oMutex.Enter ();
	CCallbackEntry **ppoPrevious = &m_poCallbacks;
	CCallbackEntry *poEntry = m_poCallbacks;
	while (poEntry) {
		if (poEntry->IsCallback (poCallback)) {
			*ppoPrevious = poEntry->m_poNext;
			poEntry->FreeString ();
			// If there is no dispatcher the disconnects will have already been sent, or will shortly
			// be sent by the thread's shutdown process.
			if (m_poDispatch) {
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
	m_oMutex.Leave ();
	return bFound;
}

bool CConnector::RecycleDispatchThread () {
	m_oMutex.Enter ();
	bool bResult = m_poDispatch ? m_poDispatch->RecycleThread () : false;
	m_oMutex.Leave ();
	return bResult;
}

static void _Replace (CAtomicPointer<IRunnable*> *poPtr, IRunnable *poNewValue) {
	IRunnable *poPrevious = poPtr->GetAndSet (poNewValue);
	if (poPrevious) {
		LOGDEBUG (TEXT ("Deleting previous callback"));
		delete poPrevious;
	}
}

void CConnector::OnEnterRunningState (IRunnable *poOnEnterRunningState) {
	_Replace (&m_oOnEnterRunningState, poOnEnterRunningState);
}

void CConnector::OnExitRunningState (IRunnable *poOnExitRunningState) {
	_Replace (&m_oOnExitRunningState, poOnExitRunningState);
}

void CConnector::OnEnterStableNonRunningState (IRunnable *poOnEnterStableNonRunningState) {
	_Replace (&m_oOnEnterStableNonRunningState, poOnEnterStableNonRunningState);
}
