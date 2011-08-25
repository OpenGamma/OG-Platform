/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Connector.h"
#define FUDGE_NO_NAMESPACE
#include "com_opengamma_language_connector_UserMessage.h"
#include <Util/Error.h>

LOGGING (com.opengamma.language.connector.Connector);

/// Asynchronous operation to send a received Fudge message to the user callback.
class CConnectorMessageDispatch : public CAsynchronous::COperation {
private:

	/// Callback entry to pass the message to.
	CConnector::CCallbackEntry *m_poCallback;

	/// Message to pass.
	FudgeMsg m_msg;

public:

	/// Creates a new operation for the callback entry and message.
	///
	/// @param[in] poCallback callback entry, never NULL
	/// @param[in] msg Fudge message
	CConnectorMessageDispatch (CConnector::CCallbackEntry *poCallback, FudgeMsg msg)
	: COperation () {
		poCallback->Retain ();
		m_poCallback = poCallback;
		FudgeMsg_retain (msg);
		m_msg = msg;
	}

	/// Destroys the operation, releasing the entry and message.
	~CConnectorMessageDispatch () {
		CConnector::CCallbackEntry::Release (m_poCallback);
		FudgeMsg_release (m_msg);
	}

	/// Invokes OnMessage on the callback entry with the message.
	void Run () {
		m_poCallback->OnMessage (m_msg);
	}

};

/// Asynchronous operation to notify a user callback of asynchronous thread disconnection.
/// The single notification from CAsynchronous is propogated to all of the registered
/// event handlers as this "vital" operation (see the documentation for CAsynchronous).
class CConnectorThreadDisconnectDispatch : public CAsynchronous::COperation {
private:

	/// Callback entry to notify.
	CConnector::CCallbackEntry *m_poCallback;

public:

	/// Creates a new notification for the callback entry.
	///
	/// @param[in] poCallback callback entry, never NULL
	CConnectorThreadDisconnectDispatch (CConnector::CCallbackEntry *poCallback)
	: COperation (true) {
		poCallback->Retain ();
		m_poCallback = poCallback;
	}

	/// Destroys the operation, releasing the entry
	~CConnectorThreadDisconnectDispatch () {
		CConnector::CCallbackEntry::Release (m_poCallback);
	}

	/// Invokes OnThreadDisconnect on the callback entry.
	void Run () {
		m_poCallback->OnThreadDisconnect ();
	}

};

/// Helper for dispatching messages (and thread disconnection operations) into the 
/// user callbacks.
class CConnectorDispatcher : public CAsynchronous {
private:

	/// Associated CConnector instance.
	CConnector *m_poConnector;

	/// Creates a new dispatcher for the connector.
	///
	/// @param[in] poConnector associated connector instance, never NULL
	CConnectorDispatcher (CConnector *poConnector) : CAsynchronous () {
		poConnector->Retain ();
		m_poConnector = poConnector;
	}

	/// Destroys the dispatcher.
	~CConnectorDispatcher () {
		CConnector::Release (m_poConnector);
	}
protected:

	/// Propogates the thread disconnect message to the connector which will in turn
	/// pass it to the registered user callbacks.
	void OnThreadExit () {
		CAsynchronous::OnThreadExit ();
		m_poConnector->OnDispatchThreadDisconnect ();
	}

public:

	/// Creates a new dispatcher.
	///
	/// @param[in] poConnector associated connector instance, never NULL
	/// @return the new dispatcher
	static CConnectorDispatcher *Create (CConnector *poConnector) {
		return new CConnectorDispatcher (poConnector);
	}
};

/// Propogates the received message to the user callback. If the entry has been unregistered
/// (i.e. the underlying class matching string released) the message is discarded.
///
/// @param[in] msgPayload message to send
void CConnector::CCallbackEntry::OnMessage (FudgeMsg msgPayload) {
	if (m_strClass) {
		m_poCallback->OnMessage (msgPayload);
	} else {
		LOGDEBUG (TEXT ("Callback object unregistered, discarding message payload"));
	}
}

/// Executes the runnable held in the pointer. The full sequence of events is to take
/// ownership of the runnable (by swapping it with NULL), executing that, and then
/// restoring ownership back into the atomic pointer unless a new callback has since
/// been registered.
///
/// @param[in,out] poPtr pointer to the runnable. During execution may contain NULL.
///                After execution will contain either the original value, or whatever
///                another thread passed into it. If another thread updates the
///                pointer, the original runnable is destroyed before exit.
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

/// Signals the startup semaphore if one has been set and executes the user's callback
/// for entering the running state if one has been requested.
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

/// Signals all outstanding message semaphores and executes the user's callback for exiting
/// the running state if one has been requested.
void CConnector::OnExitRunningState () {
	LOGINFO (TEXT ("Left running state"));
	// No longer running, so signal any message semaphores
	m_oSynchronousCalls.SignalAllSemaphores ();
	_GetRunAndRestore (&m_oOnExitRunningState);
}

/// Signals the startup semaphore if one has been set and executes the user's callback
/// for entering a non-running state if one has been requested.
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

/// Handles a state change from the underlying client service. The three important transitions
/// are handled by OnEnterRunningState, OnExitRunningState and OnEnterStableNonRunningState.
///
/// @param[in] ePreviousState state changing from
/// @param[in] eNewState state changing to
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

/// Handles an incoming message from the client service. A response to a synchronous message is
/// paired with its call object and released to its caller (see CSynchronousCalls). An asynchronous
/// message is matched against registered callback entries based on the class names embedded within
/// it. If a match is found, it is dispatched asynchronously.
///
/// @param[in] msg message to dispatch
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
		if (nFields <= (int)(sizeof (field) / sizeof (FudgeField))) {
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
							// Stop at first matching callback
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

// Propogates the termination of the thread used for user message dispatches to the user
// message handlers.
void CConnector::OnDispatchThreadDisconnect () {
	LOGINFO (TEXT ("Dispatcher thread disconnected"));
	int nCallbacks = 0, i;
	CCallbackEntry **apoCallback = NULL;
	m_oMutex.Enter ();
	if (m_poDispatch) {
		CCallbackEntry *poCallback = m_poCallbacks;
		while (poCallback) {
			nCallbacks++;
			poCallback = poCallback->m_poNext;
		}
		apoCallback = new CCallbackEntry*[nCallbacks];
		if (apoCallback) {
			i = 0;
			poCallback = m_poCallbacks;
			while (poCallback) {
				assert (i < nCallbacks);
				poCallback->Retain ();
				apoCallback[i++] = poCallback;
				poCallback = poCallback->m_poNext;
			}
		} else {
			LOGFATAL (TEXT ("Out of memory"));
		}
	} else {
		LOGDEBUG (TEXT ("Thread disconnect messages already sent at stop"));
	}
	m_oMutex.Leave ();
	if (nCallbacks && apoCallback) {
		LOGDEBUG (TEXT ("Calling OnThreadDisconnect on ") << nCallbacks << TEXT (" callbacks"));
		for (i = 0; i < nCallbacks; i++) {
			apoCallback[i]->OnThreadDisconnect ();
			CCallbackEntry::Release (apoCallback[i]);
		}
		delete apoCallback;
	}
}

/// Creates a new call object using the given slot for tracking the response.
///
/// @param[in] poSlot response tracking slot
CConnector::CCall::CCall (CSynchronousCallSlot *poSlot) {
	m_poSlot = poSlot;
}

/// Destroys the call object, releasing the underlying resources.
CConnector::CCall::~CCall () {
	if (m_poSlot) {
		m_poSlot->Release ();
	}
}

/// Cancels an outstanding call. The synchronous message slot is released so that the response
/// will be discarded if/when it arrives. It is not possible to cancel execution or delivery
/// of the orginal message to the Java stack.
///
/// @return TRUE if the call was cancelled, FALSE if there was a problem (e.g. the call was
///         already cancelled)
bool CConnector::CCall::Cancel () {
	if (!m_poSlot) {
		SetLastError (EALREADY);
		return false;
	}
	m_poSlot->Release ();
	m_poSlot = NULL;
	return true;
}

/// Waits for the corresponding response message to be received for a call. If a response
/// is received, it is returned to the caller and the messaging slot released.
///
/// @param[out] pmsgResponse message received (only valid if method returns TRUE)
/// @param[in] lTimeout maximum time to wait for a response message in milliseconds
/// @return TRUE if a response was received, FALSE if there was a problem or the timeout
///         elapsed.
bool CConnector::CCall::WaitForResult (FudgeMsg *pmsgResponse, unsigned long lTimeout) {
	if (!m_poSlot) {
		SetLastError (EALREADY);
		return false;
	}
	FudgeMsg msg = m_poSlot->GetMessage (lTimeout);
	if (msg) {
		m_poSlot->Release ();
		m_poSlot = NULL;
		*pmsgResponse = msg;
		return true;
	} else {
		return false;
	}
}

/// Creates a new connector based on the underlying client service.
///
/// @param[in] poClient underlying client, never NULL
CConnector::CConnector (CClientService *poClient)
: m_oRefCount (1) {
	LOGINFO (TEXT ("Connector instance created"));
	m_poClient = poClient;
	poClient->SetMessageReceivedCallback (this);
	poClient->SetStateChangeCallback (this);
	m_poCallbacks = NULL;
	m_poDispatch = CConnectorDispatcher::Create (this);
}

/// Destroys the connector instance.
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

/// Creates a connector and underlying client service, starting them.
///
/// @param[in] pszLanguage language ID to send to the Java stack
/// @return the started connector or NULL if there was a problem
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

/// Attempts to stop the connector, underlying client, and threads used for dispatching
/// messages to user callbacks.
///
/// @return TRUE if the stop attempt was successful (although it may not have propogated
///         to all components), FALSE if there was a problem.
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

/// Waits for the connector to start up - i.e. for the Java stack to respond to connections
/// and the underlying client to connect to it.
///
/// @param[in] lTimeout maximum time to wait for the startup in milliseconds
/// @return TRUE if the client is now started, FALSE if there was a problem
bool CConnector::WaitForStartup (unsigned long lTimeout) const {
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

/// Sends a message to the Java stack and waits for it to respond. This is a pure synchronous
/// call that blocks the caller. This is built around the overlapping call functions that work
/// with the CCall object.
///
/// @param[in] msgPayload user message payload
/// @param[out] pmsgResponse message received, only valid if method returns TRUE
/// @param[in] lTimeout maximum time to wait for the response in milliseconds
/// @return TRUE if a message was received, FALSE if there was a problem or the timeout elapsed
bool CConnector::Call (FudgeMsg msgPayload, FudgeMsg *pmsgResponse, unsigned long lTimeout) const {
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
	unsigned long lStartTime = GetTickCount ();
retryCall:
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
		} else if (error == ETIMEDOUT) {
			if (GetTickCount () - lStartTime < lTimeout) {
				LOGINFO (TEXT ("Retrying call initiation after intermediate timeout"));
				goto retryCall;
			} else {
				LOGWARN (TEXT ("Timeout elapsed during call initiation"));
				SetLastError (ETIMEDOUT);
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

/// Composes a message payload with a synchronous sending handle to create a user message to
/// send using the underlying client.
///
/// @param[in] poClient underlying client, never NULL
/// @param[in] handle handle allocated from the CSynchronousCalls service
/// @param[in] msgPayload payload message
/// @return TRUE if the message was send, FALSE otherwise
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

/// Initiates an overlapped call. The user message is sent the Java stack and a handle returned
/// that can be used to wait for a result (or abandon the result). Using the overlapped call
/// mechanism a thread can issue multiple calls and then block on completion of the first, or
/// it may perform other actions while the Java stack executes the message.
///
/// @param[in] msgPayload payload message
/// @return call handle or NULL if there was a problem
CConnector::CCall *CConnector::Call (FudgeMsg msgPayload) const {
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

/// Sends a user message to the Java stack for which a reply is not expected. The message is sent
/// without a synchronous call handle, so no reply is possible.
///
/// @param[in] msgPayload message to send
/// @return TRUE if the message was sent, FALSE if there was a problem.
bool CConnector::Send (FudgeMsg msgPayload) const {
	if (!msgPayload) {
		LOGWARN (TEXT ("Null payload"));
		return false;
	}
	return _SendMessage (m_poClient, 0, msgPayload);
}

/// Registers a callback for asynchronous messages received by the connector. When an asynchronous
/// message is received (i.e. one not in correlated response to one sent by the connector) a
/// callback matching the class name (ordinal field 0) from the message is used to process it.
///
/// If multiple callbacks are registered for a given class, the last one to be registered is used.
///
/// If a message has multiple class headers, the first to match any of the registered callbacks
/// will be considered and that callback matched.
///
/// @param[in] pszClass class header to match
/// @param[in] poCallback user callback handler
/// @return TRUE if the callback was registered, FALSE if there was a problem
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

/// Unregisters a user callback for notification of asynchronous messages. Note that after the callback
/// is removed from the list, there may still be pending entries in the queue for it that have not
/// been dispatched so these will still be received. For this reason, the callback is a reference
/// counted object so that it remains live in the heap until all references have been released.
///
/// @param[in] poCallback user callback to remove
/// @return TRUE if the callback was found and removed, FALSE if there was a problem (e.g. not found)
bool CConnector::RemoveCallback (const CCallback *poCallback) {
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

/// Attempts to recycle the dispatch threads used for asynchronous messaging. See CAsynchronous
/// for more details.
///
/// @return TRUE if the thread was recycled, FALSE if there was a problem
bool CConnector::RecycleDispatchThread () {
	m_oMutex.Enter ();
	bool bResult = m_poDispatch ? m_poDispatch->RecycleThread () : false;
	m_oMutex.Leave ();
	return bResult;
}

/// Replaces the value held in an atomic reference, deleting the old value if there was one.
///
/// @param[in,out] poPtr atomic reference
/// @param[in] poNewValue value to set
static void _Replace (CAtomicPointer<IRunnable*> *poPtr, IRunnable *poNewValue) {
	IRunnable *poPrevious = poPtr->GetAndSet (poNewValue);
	if (poPrevious) {
		LOGDEBUG (TEXT ("Deleting previous callback"));
		delete poPrevious;
	}
}

/// Registers a callback runnable for when the client enters its RUNNING state.
///
/// The connector will take ownership of the pointer and will be responsible for deleting it when
/// it falls out of scope (e.g. another callback is registered, or the connector is destroyed).
/// If the callback is code from a dynamically loaded library, it should use the locking mechanism
/// (CLibraryLock) to ensure that it is not unloaded prematurely.
///
/// @param[in] poOnEnterRunningState the new callback, replacing any previous callback. Use NULL
///            to request no callback and delete any previous one.
void CConnector::OnEnterRunningState (IRunnable *poOnEnterRunningState) {
	_Replace (&m_oOnEnterRunningState, poOnEnterRunningState);
}

/// Registers a callback runnable for when the client leaves its RUNNING state (e.g. a restart
/// under error, or a complete halt as part of a shutdown or more serious error condition).
/// After leaving the RUNNING state, it is likely that it will either re-enter the RUNNING state
/// or a "stable non-running" state.
///
/// The connector will take ownership of the pointer and will be responsible for deleting it when
/// it falls out of scope (e.g. another callback is registered, or the connector is destroyed).
/// If the callback is code from a dynamically loaded library, it should use the locking mechanism
/// (CLibraryLock) to ensure that it is not unloaded prematurely.
///
/// @param[in] poOnExitRunningState the new callback, replacing any previous callback. Use NULL
///            to request no callback and delete any previous one
void CConnector::OnExitRunningState (IRunnable *poOnExitRunningState) {
	_Replace (&m_oOnExitRunningState, poOnExitRunningState);
}

/// Registers a callback runnable for when the client enters a "stable non-running state" that
/// isn't RUNNING but is not a transition state, for example STOPPED or ERRORED.
///
/// The connector will take ownership of the pointer and will be responsible for deleting it when
/// it falls out of scope (e.g. another callback is registered, or the connector is destroyed).
/// If the callback is code from a dynamically loaded library, it should use the locking mechanism
/// (CLibraryLock) to ensure that it is not unloaded prematurely.
///
/// @param[in] poOnEnterStableNonRunningState the new callback, replacing any previous callback.
///            Use NULL to request no callback and delete any previous one.
void CConnector::OnEnterStableNonRunningState (IRunnable *poOnEnterStableNonRunningState) {
	_Replace (&m_oOnEnterStableNonRunningState, poOnEnterStableNonRunningState);
}
