/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Alert.h"
#include "Client.h"
#include "MessageDirectives.h"
#include "Settings.h"
#define FUDGE_NO_NAMESPACE
#include "com_opengamma_language_connector_ConnectorMessage.h"
#include <Util/Error.h>

LOGGING (com.opengamma.language.connector.Client);

/// Initialise the Fudge library.
static CFudgeInitialiser g_oInitialiseFudge;

/// Event thread. This thread is responsible for starting the JVM, restarting it on failure
/// and blocking on read requests to receive incoming messages from the Java stack. Major
/// transitions will result in a call back to the parent CClientService object.
class CClientService::CRunnerThread : public CThread {
private:

	/// Critical section to protect the stash message.
	CMutex m_oStashMutex;

	/// Stash message. The stash message is stored by the Java stack on successful startup.
	/// If the Java stack is restarted, for example on a heartbeat failure, it is sent the
	/// stash message so that it can recover previous state. NULL if no stash message has
	/// been set.
	FudgeMsg m_msgStash;

	/// Parent service object.
	CClientService *m_poService;

	/// Heartbeat timeout (taken from the CSettings instance).
	unsigned long m_lHeartbeatTimeout;

	/// Send a heartbeat message, optionally including the stash message.
	///
	/// @param[in] bWithStash TRUE to include the stash message (if there is one), FALSE not to
	/// @return TRUE if the message was sent, FALSE if there was a problem
	bool SendHeartbeat (bool bWithStash) {
		LOGDEBUG (TEXT ("Sending heartbeat message"));
		FudgeStatus status;
		FudgeMsg msg;
		if ((status = FudgeMsg_create (&msg)) != FUDGE_OK) {
			LOGFATAL (TEXT ("Couldn't create message, status ") << status);
			assert (0);
			return false;
		}
		if ((status = ConnectorMessage_setOperation (msg, HEARTBEAT)) != FUDGE_OK) {
			FudgeMsg_release (msg);
			LOGFATAL (TEXT ("Couldn't create message, status ") << status);
			assert (0);
			return false;
		}
		if (bWithStash) {
			m_oStashMutex.Enter ();
			if (m_msgStash) {
				if ((status = ConnectorMessage_setStash (msg, m_msgStash)) != FUDGE_OK) {
					LOGFATAL (TEXT ("Couldn't create message, status ") << status);
					assert (0);
					status = FUDGE_OK;
					// Not good, but can carry on
				}
			}
			m_oStashMutex.Leave ();
		}
		bool bResult = m_poService->Send (MESSAGE_DIRECTIVES_CLIENT, msg);
		FudgeMsg_release (msg);
		return bResult;
	}

	/// Wait for the response to the first heartbeat message. This may be a heartbeat message generated
	/// by the Java stack (if it had nothing else to send) or any other valid message.
	///
	/// @return TRUE if a response was received, FALSE if the timeout elapsed
	bool WaitForHeartbeatResponse () {
		LOGDEBUG (TEXT ("Waiting for heartbeat response"));
		FudgeMsgEnvelope env = m_poService->Recv (m_lHeartbeatTimeout);
		if (env) {
			m_poService->DispatchAndRelease (env);
			m_poService->FirstConnectionOk ();
			return true;
		} else {
			LOGWARN (TEXT ("Heartbeat timeout exceeded"));
			return false;
		}
	}

protected:

	/// Destroy the thread object, releasing any resources still held.
	~CRunnerThread () {
		LOGDEBUG (TEXT ("Runner thread destroyed"));
		if (m_msgStash) {
			FudgeMsg_release (m_msgStash);
		}
	}

public:

	/// Create a new thread.
	///
	/// @param[in] poService the parent service object, never NULL
	CRunnerThread (CClientService *poService) : CThread () {
		LOGDEBUG (TEXT ("Runner thread created"));
		CSettings oSettings;
		m_poService = poService;
		m_msgStash = NULL;
		m_lHeartbeatTimeout = oSettings.GetHeartbeatTimeout ();
	}

	/// Thread execution. The JVM is started, pipes connected, and then a message receive loop begins.
	/// If the heartbeat timeout passes, a heartbeat message is sent. If the heartbeat fails the
	/// JVM is reset. If the reset happens twice in quick succession, an ERRORED state is assumed
	/// and the thread terminates. The thread will also terminate if a POISONED state is requested.
	void Run () {
		LOGINFO (TEXT ("Runner thread started"));
		bool bRetry = true;
		bool bStatus = true;
		do {
			if (!m_poService->SetState (STARTING)) break;
			LOGINFO (TEXT ("Starting Java framework"));
			if (!m_poService->CreatePipes ()) {
				LOGERROR (TEXT ("Unable to open pipes to Java framework"));
				CAlert::Bad (TEXT ("Unable to connect to service"));
				bStatus = false;
				break;
			}
			if (!m_poService->StartJVM ()) {
				if (bRetry) {
					LOGWARN (TEXT ("Unable to initiate Java framework"));
					m_poService->ClosePipes ();
					CAlert::Bad (TEXT ("Restarting service"));
					bRetry = false;
					continue;
				}
				LOGERROR (TEXT ("Unable to initiate Java framework"));
				CAlert::Bad (TEXT ("Unable to start service"));
				bStatus = false;
				break;
			}
			if (!m_poService->ConnectPipes ()) {
				if (bRetry) {
					LOGWARN (TEXT ("Unable to connect to Java framework"));
					m_poService->ClosePipes ();
					CAlert::Bad (TEXT ("Restarting service"));
					m_poService->StopJVM ();
					bRetry = false;
					continue;
				}
				LOGERROR (TEXT ("Unable to connect to Java framework"));
				CAlert::Bad (TEXT ("Unable to start service"));
				bStatus = false;
				break;
			}
			if (!SendHeartbeat (true) || !WaitForHeartbeatResponse ()) {
				if (bRetry) {
					LOGWARN (TEXT ("Java framework is not responding"));
					m_poService->ClosePipes ();
					CAlert::Bad (TEXT ("Restarting service"));
					m_poService->StopJVM ();
					bRetry = false;
					continue;
				}
				LOGERROR (TEXT ("Java framework is not responding"));
				CAlert::Bad (TEXT ("Service is not responding"));
				// TODO: [XLS-43] (needs moving to PLAT) Can we get information from the log to pop-up if the user clicks on the bubble?
				// TODO: [XLS-43] (needs a hook to implement) What about a "retry" button to force Excel to unload and re-load the plugin
				bStatus = false;
				break;
			}
			bRetry = true;
			if (!m_poService->SetState (RUNNING)) break;
			LOGINFO (TEXT ("Java framework started and connected"));
			// TODO: [XLS-43] (needs moving back to XLS project) Display messages a bit more sensibly - e.g. route them in from UDF when everything's registered
			CAlert::Good (TEXT ("Connected to service"));
			LOGDEBUG (TEXT ("Waiting for first message"));
			FudgeMsgEnvelope env = m_poService->Recv (m_lHeartbeatTimeout);
			while (env) {
				m_poService->DispatchAndRelease (env);
				if (m_poService->HeartbeatNeeded (m_lHeartbeatTimeout) && !SendHeartbeat (false)) break;
				LOGDEBUG (TEXT ("Waiting for message"));
				env = m_poService->Recv (m_lHeartbeatTimeout);
			}
			do {
				int ec = GetLastError ();
				if (ec == ETIMEDOUT) {
					if (!SendHeartbeat (false)) goto endMessageLoop;
				} else {
					LOGWARN (TEXT ("Couldn't read message, error ") << ec);
					goto endMessageLoop;
				}
				LOGDEBUG (TEXT ("Waiting for critical message"));
				env = m_poService->Recv (m_lHeartbeatTimeout);
				if (!env) {
					LOGWARN (TEXT ("Heartbeat missed"));
					goto endMessageLoop;
				}
				do {
					m_poService->DispatchAndRelease (env);
					if (m_poService->HeartbeatNeeded (m_lHeartbeatTimeout) && !SendHeartbeat (false)) goto endMessageLoop;
					LOGDEBUG (TEXT ("Waiting for message"));
					env = m_poService->Recv (m_lHeartbeatTimeout);
				} while (env);
			} while (m_poService->GetState () == RUNNING);
endMessageLoop:
			m_poService->ClosePipes ();
			if (!m_poService->SetState (STOPPING)) break;
			LOGINFO (TEXT ("Restarting Java framework"));
			CAlert::Bad (TEXT ("Reconnecting to service"));
		} while (true);
		if (m_poService->GetState () != POISONED) {
			LOGINFO (TEXT ("Poisoning Java framework"));
			m_poService->SetState (POISONED);
			bStatus &= m_poService->SendPoison ();
		}
		m_poService->ClosePipes ();
		if (bStatus) {
			CAlert::Good (TEXT ("Disconnected from service"));
			m_poService->SetState (STOPPED);
		} else {
			// A bad alert was already flagged when bStatus was set to FALSE
			m_poService->SetState (ERRORED);
		}
		LOGINFO (TEXT ("Runner thread stopped"));
	}

	/// Sets the stash message to send to the Java stack if it restarts.
	///
	/// @param[in] msgStash the message to stash, NULL for none
	void SetStash (FudgeMsg msgStash) {
		m_oStashMutex.Enter ();
		if (m_msgStash) {
			LOGDEBUG (TEXT ("Discarding old stash message"));
			FudgeMsg_release (m_msgStash);
		}
		FudgeMsg_retain (msgStash);
		m_msgStash = msgStash;
		m_oStashMutex.Leave ();
	}

};

/// Creates a new service object.
///
/// @param[in] pszLanguageID the language identifier to be sent to the Java stack
CClientService::CClientService (const TCHAR *pszLanguageID)
: m_oPipesSemaphore (1, 1) {
	m_poStateChangeCallback = NULL;
	m_poMessageReceivedCallback = NULL;
	m_eState = STOPPED;
	m_poRunner = NULL;
	m_poPipes = NULL;
	m_poJVM = NULL;
	m_pszLanguageID = _tcsdup (pszLanguageID);
}

/// Destroys the object, releasing any resources. If not already stopped, an attempt will
/// be made to stop the service. Assertions are made that JVM connection pipes and the
/// event thread have terminated.
CClientService::~CClientService () {
	if (!Stop ()) {
		LOGFATAL (TEXT ("Couldn't stop running service"));
		assert (0);
	}
	if (m_poPipes) {
		LOGFATAL (TEXT ("Pipes exist at shutdown"));
		delete m_poPipes;
		assert (0);
	}
	if (m_poRunner) {
		LOGFATAL (TEXT ("Runner thread exists at shutdown"));
		CThread::Release (m_poRunner);
		assert (0);
	}
	if (m_poJVM) {
		delete m_poJVM;
	}
	delete (m_pszLanguageID);
}

/// Attempts to start the service. The service will enter the STARTING state and an event thread
/// created to coordinate the startup.
///
/// @return TRUE if the service entered the STARTING state, FALSE if there was a problem
///         such as the service already running or in the process of shutting down.
bool CClientService::Start () {
	LOGINFO (TEXT ("Starting"));
	m_oStateMutex.Enter ();
	bool bResult;
	if ((m_eState == ERRORED) || (m_eState == STOPPED)) {
		if (m_poRunner) {
			LOGDEBUG (TEXT ("Closing previous runner thread"));
			CThread::Release (m_poRunner);
		}
		m_poRunner = new CRunnerThread (this);
		if (m_poRunner->Start ()) {
			LOGINFO (TEXT ("Runner thread started, ") << m_poRunner->GetThreadId ());
			m_eState = STARTING;
			bResult = true;
		} else {
			LOGERROR (TEXT ("Unable to start runner thread, error ") << GetLastError ());
			CThread::Release (m_poRunner);
			m_poRunner = NULL;
			bResult = false;
		}
	} else {
		LOGWARN (TEXT ("Service already running"));
		SetLastError (EALREADY);
		bResult = false;
	}
	m_oStateMutex.Leave ();
	return bResult;
}

/// Attempts to stop the service. The service will enter the POISONED state signalling the
/// event thread to shut down. The caller is then blocked until the revent thread terminates.
///
/// @return TRUE if the service stopped and the thread terminated. FALSE if there was a
///         problem
bool CClientService::Stop () {
	LOGINFO (TEXT ("Stopping"));
	// Exclude concurrent calls to Stop.
	m_oStopMutex.Enter ();
	m_oStateMutex.Enter ();
	CThread *poRunner;
	ClientServiceState ePreviousState = m_eState;
	if ((m_eState == ERRORED) || (m_eState == STOPPED)) {
		LOGWARN (TEXT ("Runner thread already stopped"));
		// Already stopped, so close thread handle if still open
		if (m_poRunner) {
			CThread::Release (m_poRunner);
			m_poRunner = NULL;
		}
		poRunner = NULL;
	} else {
		// Running, so mark as poisoned and grab the handle ready to block
		LOGDEBUG (TEXT ("Poisoning runner thread"));
		m_eState = POISONED;
		poRunner = m_poRunner;
		m_poRunner = NULL;
	}
	ClientServiceState eNewState = m_eState;
	// Release the state lock before blocking
	m_oStateMutex.Leave ();
	// Notify listener of the state change
	if (ePreviousState != eNewState) {
		m_oStateChangeMutex.Enter ();
		if (m_poStateChangeCallback) {
			m_poStateChangeCallback->OnStateChange (ePreviousState, eNewState);
		}
		m_oStateChangeMutex.Leave ();
	}
	bool bResult;
	// We issued a poison request so wait for and close the slave thread handle to synchronise
	if (poRunner) {
		// Send poison to the client (this is faster than relying on the heartbeat)
		if (!SendPoison ()) {
			LOGWARN (TEXT ("Unable to send poison message to Java framework"));
		}
		LOGDEBUG (TEXT ("Waiting for runner thread to terminate"));
		bResult = CThread::WaitAndRelease (poRunner);
	} else {
		bResult = true;
	}
	m_oStopMutex.Leave ();
	return bResult;
}

/// Returns the current state of the service.
///
/// @return the current state
ClientServiceState CClientService::GetState () const {
	m_oStateMutex.Enter ();
	ClientServiceState eState = m_eState;
	m_oStateMutex.Leave ();
	return eState;
}

/// Sends a user message to the Java stack for dispatch to the handling code.
///
/// @param[in] msg message to send
/// @return TRUE if the message was sent, FALSE if there was a problem
bool CClientService::Send (FudgeMsg msg) const {
	return Send (MESSAGE_DIRECTIVES_USER, msg);
}

/// Sets the state change callback object. The pointer passed must be valid
/// for the lifetime of the CClientService instance, or until a different
/// callback is set.
///
/// @param[in] poCallback the callback instance, or NULL for none
void CClientService::SetStateChangeCallback (CStateChange *poCallback) {
	m_oStateChangeMutex.Enter ();
	m_poStateChangeCallback = poCallback;
	m_oStateChangeMutex.Leave ();
}

/// Sets the message received callback object. The pointer passed must be
/// valid for the lifetime of the CClientService instance, or until a different
/// callback is set.
///
/// @param[in] poCallback the callback instance, or NULL for none
void CClientService::SetMessageReceivedCallback (CMessageReceived *poCallback) {
	m_oMessageReceivedMutex.Enter ();
	m_poMessageReceivedCallback = poCallback;
	m_oMessageReceivedMutex.Leave ();
}

/// Closes the pipes connected to the JVM and frees the underlying resources.
void CClientService::ClosePipes () {
	LOGINFO (TEXT ("Closing pipes"));
	m_oPipesSemaphore.Wait ();
	if (m_poPipes) {
		delete m_poPipes;
		m_poPipes = NULL;
	}
	m_oPipesSemaphore.Signal ();
}

/// Connects the pipes to the JVM instance, blocking until connected, the timeout
/// from CSettings has elapsed, or the JVM abends.
///
/// @return TRUE if connected, FALSE if there was a problem
bool CClientService::ConnectPipes () {
	LOGINFO (TEXT ("Connecting pipes to JVM"));
	m_oPipesSemaphore.Wait ();
	if (!m_poPipes) {
		m_oPipesSemaphore.Signal ();
		LOGFATAL (TEXT ("Pipes not created"));
		assert (0);
		return false;
	}
	if (!m_poJVM) {
		m_oPipesSemaphore.Signal ();
		LOGFATAL (TEXT ("JVM not created"));
		assert (0);
		return false;
	}
	CSettings oSettings;
	const TCHAR *pszPipeName = oSettings.GetConnectionPipe ();
	LOGDEBUG (TEXT ("Connecting to ") << pszPipeName);
	unsigned long lTimeout = m_poJVM->FirstConnection () ? oSettings.GetStartTimeout () : oSettings.GetConnectTimeout ();
	m_lSendTimeout = lTimeout;
	m_lShortTimeout = oSettings.GetSendTimeout ();
	unsigned long lTime = GetTickCount ();
	CNamedPipe *poPipe;
	do {
		poPipe = CNamedPipe::ClientWrite (pszPipeName);
		if (poPipe) {
			break;
		} else {
			int ec = GetLastError ();
			if (ec == ENOENT) {
				if (GetTickCount () - lTime > lTimeout) {
					m_oPipesSemaphore.Signal ();
					LOGWARN (TEXT ("Timeout waiting for JVM service to open ") << pszPipeName);
					return false;
				} else {
					if (m_poJVM->IsAlive ()) {
						LOGDEBUG (TEXT ("Waiting for JVM service to open pipe"));
						CThread::Sleep (oSettings.GetServicePoll ());
					} else {
						m_oPipesSemaphore.Signal ();
						LOGWARN (TEXT ("JVM service terminated before opening ") << pszPipeName);
						return false;
					}
				}
			} else {
				m_oPipesSemaphore.Signal ();
				LOGWARN (TEXT ("Couldn't connect to ") << pszPipeName << TEXT (", error ") << ec);
				return false;
			}
		}
	} while (true);
	LOGDEBUG (TEXT ("Connected to service"));
	bool bResult = m_poPipes->Connect (m_pszLanguageID, poPipe, lTimeout);
	if (!bResult) {
		LOGWARN (TEXT ("Couldn't connect to JVM service, error ") << GetLastError ());
	}
	delete poPipe;
	m_oPipesSemaphore.Signal ();
	return bResult;
}

/// Creates the pipe pair to be used for communication with the JVM host process.
///
/// @return TRUE if the pipes were created, FALSE if there was a problem
bool CClientService::CreatePipes () {
	LOGINFO (TEXT ("Creating pipes"));
	m_oPipesSemaphore.Wait ();
	if (m_poPipes) {
		m_oPipesSemaphore.Signal ();
		LOGFATAL (TEXT ("Pipes already created"));
		assert (0);
		return false;
	}
	m_poPipes = CClientPipes::Create ();
	if (m_poPipes) {
		m_oPipesSemaphore.Signal ();
		return true;
	} else {
		m_oPipesSemaphore.Signal ();
		LOGWARN (TEXT ("Couldn't create pipes, error ") << GetLastError ());
		return false;
	}
}

/// Dispatches a received message to user code (or handles internally) and releases the
/// containing envelope.
///
/// This must only be called from the thread that creates and connects the pipes (i.e. the
/// event thread). This then doesn't need to acquire the pipe semaphore as the object won't
/// be modified concurrently. Another thread may still then send. This also means a deadlock
/// won't occur if the user message callback attempts to send a message although that should
/// be discouraged.
///
/// @param[in] env message to be dispatched, will then be released
/// @return TRUE if the pipes to the JVM are still connected
bool CClientService::DispatchAndRelease (FudgeMsgEnvelope env) {
	FudgeMsg msg = FudgeMsgEnvelope_getMessage (env);
	switch (FudgeMsgEnvelope_getDirectives (env)) {
	case MESSAGE_DIRECTIVES_CLIENT : {
		Operation op;
		if (ConnectorMessage_getOperation (msg, &op) == FUDGE_OK) {
			switch (op) {
			case HEARTBEAT :
				LOGDEBUG (TEXT ("Heartbeat received"));
				break;
			case POISON :
				// This shouldn't be sent by the Java stack
				LOGFATAL (TEXT ("Received poison from Java framework"));
				assert (0);
				break;
			case STASH : {
				FudgeMsg msgStash;
				if (ConnectorMessage_getStash (msg, &msgStash) == FUDGE_OK) {
					LOGDEBUG (TEXT ("Storing stash message"));
					m_oStateMutex.Enter ();
					if (m_poRunner) {
						m_poRunner->SetStash (msgStash);
					} else {
						LOGWARN (TEXT ("No runner thread"));
					}
					m_oStateMutex.Leave ();
					FudgeMsg_release (msgStash);
				} else {
					LOGWARN (TEXT ("No stash message attached"));
				}
				break;
						 }
			default :
				LOGWARN (TEXT ("Invalid client message - operation ") << op);
				break;
			}
		} else {
			LOGWARN (TEXT ("Invalid client message"));
		}
		break;
										}
	case MESSAGE_DIRECTIVES_USER :
		m_oMessageReceivedMutex.Enter ();
		if (m_poMessageReceivedCallback) {
			m_poMessageReceivedCallback->OnMessageReceived (msg);
		}
		m_oMessageReceivedMutex.Leave ();
		break;
	default :
		LOGWARN (TEXT ("Unknown message delivery directive ") << FudgeMsgEnvelope_getDirectives (env));
		break;
	}
	FudgeMsgEnvelope_release (env);
	return m_poPipes->IsConnected ();
}

/// Receives a message from the Java stack.
///
/// This must only be called from the thread that creates and connects the pipes (i.e. the event
/// thread). This then doesn't need to acquire the pipe semaphore as the object won't be modified
/// concurrently. Another thread may still then send. This also means a deadlock won't occur if
/// the user message callback attempts to send a message although that should be discouraged.
///
/// @param[in] lTimeout maximum time to wait for a message, in milliseconds
/// @return the received message or NULL if there was an error or the timeout elapsed
FudgeMsgEnvelope CClientService::Recv (unsigned long lTimeout) {
	FudgeStatus status;
	FudgeMsgHeader header;
	fudge_byte *ptr = (fudge_byte*)m_poPipes->PeekInput (8, lTimeout); // Fudge headers are 8-bytes long
	if (!ptr) {
		int ec = GetLastError ();
		if (ec == ETIMEDOUT) {
			LOGDEBUG (TEXT ("Timeout reading envelope header"));
		} else {
			LOGWARN (TEXT ("Couldn't read Fudge envelope header, error ") << ec);
		}
		SetLastError (ec);
		return NULL;
	}
	if ((status = FudgeHeader_decodeMsgHeader (&header, ptr, 8)) != FUDGE_OK) {
		LOGERROR (TEXT ("Couldn't decode Fudge envelope header, status ") << status);
		SetLastError (EIO_READ);
		return NULL;
	}
	ptr = (fudge_byte*)m_poPipes->PeekInput (header.numbytes, lTimeout);
	if (!ptr) {
		int ec = GetLastError ();
		LOGWARN (TEXT ("Couldn't read full Fudge message (") << header.numbytes << TEXT (" bytes, error ") << ec);
		SetLastError (ec);
		return NULL;
	}
	FudgeMsgEnvelope env;
	status = FudgeCodec_decodeMsg (&env, ptr, header.numbytes);
	m_poPipes->DiscardInput (header.numbytes);
	if (status == FUDGE_OK) {
		return env;
	} else {
		LOGERROR (TEXT ("Couldn't decode Fudge message, status ") << status);
		SetLastError (EIO_READ);
		return NULL;
	}
}

/// Sends a message to the Java stack. The processing directives control whether the OG-Language client handling code
/// (the Java counterpart of the CConnector class) should process it or whether it should be passed onto the bound
/// language user code.
///
/// @param[in] cProcessingDirectives processing directives - see the Fudge message specification for more details
/// @param[in] msg message to send
/// @return TRUE if the message was sent, FALSE if there was a problem
bool CClientService::Send (int cProcessingDirectives, FudgeMsg msg) const {
	FudgeStatus status;
	FudgeMsgEnvelope env;
	fudge_byte *ptrBuffer;
	fudge_i32 cbBuffer;
	if ((status = FudgeMsgEnvelope_create (&env, cProcessingDirectives, 0, 0, msg)) != FUDGE_OK) {
		LOGWARN (TEXT ("Couldn't create message envelope, status ") << status);
		return false;
	}
	status = FudgeCodec_encodeMsg (env, &ptrBuffer, &cbBuffer);
	FudgeMsgEnvelope_release (env);
	if (status != FUDGE_OK) {
		LOGWARN (TEXT ("Couldn't encode message, status ") << status);
		return false;
	}
	bool bResult;
	if (m_oPipesSemaphore.Wait (m_lSendTimeout)) {
		if (m_poPipes && m_poPipes->IsConnected ()) {
			int nPoll = 0;
			unsigned long lStartTime = GetTickCount ();
retrySend:
			if (m_poPipes->Write (ptrBuffer, cbBuffer, m_lSendTimeout)) {
				bResult = true;
			} else {
				int ec = GetLastError ();
#ifdef _WIN32
				if (ec == ERROR_PIPE_LISTENING) {
					// No process at the other end of the pipe
#else
				if (ec == EPIPE) {
					// Broken pipe -- they start off broken the way we create them
#endif
					if (GetTickCount () - lStartTime >= m_lSendTimeout) {
						LOGWARN (TEXT ("Timeout exceeded waiting for other end of the pipe"));
						ec = ETIMEDOUT;
					} else if (IsFirstConnection ()) {
						LOGDEBUG (TEXT ("No process at the other end of the pipe"));
						if (m_poJVM->IsAlive ()) {
							LOGDEBUG (TEXT ("Waiting for JVM"));
							if (!nPoll) {
								CSettings oSettings;
								nPoll = oSettings.GetServicePoll ();
							}
							CThread::Sleep (nPoll);
							goto retrySend;
						} else {
							LOGERROR (TEXT ("JVM service terminated before connecting to pipes, error ") << ec);
						}
					} else {
#ifdef _WIN32
						LOGFATAL (TEXT ("Not first connection but ERROR_PIPE_LISTENING returned"));
						assert (0);
#endif /* ifdef _WIN32 */
						LOGWARN (TEXT ("Couldn't write message, error ") << ec << TEXT (", rewritten to ENOTCONN"));
						ec = ENOTCONN;
					}
				} else {
					LOGWARN (TEXT ("Couldn't write message, error ") << ec);
				}
				SetLastError (ec);
				m_poPipes->Disconnected ();
				bResult = false;
			}
		} else {
			LOGWARN (TEXT ("Pipes not available for message, not connected"));
			bResult = false;
			SetLastError (ENOTCONN);
		}
		m_oPipesSemaphore.Signal ();
	} else {
		int ec = GetLastError ();
		LOGWARN (TEXT ("Pipes not available for message, error ") << ec);
		SetLastError (ec);
		bResult = false;
	}
	free (ptrBuffer);
	return bResult;
}

/// Sends a poison message to the Java stack to trigger a shutdown of the resources handling this client.
/// It will not poison the whole JVM as the host process may be shared among a number of OG-Language client
/// instances.
///
/// @return TRUE if the poison message was sent, FALSE if there was a problem
bool CClientService::SendPoison () {
	LOGINFO (TEXT ("Sending poison to JVM"));
	FudgeStatus status;
	FudgeMsg msg;
	if ((status = FudgeMsg_create (&msg)) != FUDGE_OK) {
		LOGFATAL (TEXT ("Couldn't create message, status ") << status);
		assert (0);
		return false;
	}
	if ((status = ConnectorMessage_setOperation (msg, POISON)) != FUDGE_OK) {
		FudgeMsg_release (msg);
		LOGFATAL (TEXT ("Couldn't create message, status ") << status);
		assert (0);
		return false;
	}
	bool bResult = Send (MESSAGE_DIRECTIVES_CLIENT, msg);
	FudgeMsg_release (msg);
	return bResult;
}

/// Sets the state of the object, notifying any registered callback. When in the POISONED state, the
/// only permitted changes are to STOPPED or ERRORED.
///
/// @param[in] eNewState state to change to
/// @return TRUE if the state change completed, FALSE if rejected because a poison is in progress
bool CClientService::SetState (ClientServiceState eNewState) {
	LOGDEBUG (TEXT ("Set state ") << eNewState);
	bool bResult;
	ClientServiceState eOriginalState;
	m_oStateMutex.Enter ();
	if ((m_eState == POISONED) && (eNewState != STOPPED) && (eNewState != ERRORED) && (eNewState != POISONED)) {
		LOGDEBUG (TEXT ("Currently in poison state, not changing"));
		bResult = false;
	} else {
		eOriginalState = m_eState;
		m_eState = eNewState;
		bResult = true;
	}
	m_oStateMutex.Leave ();
	if (bResult && (eOriginalState != eNewState)) {
		m_oStateChangeMutex.Enter ();
		if (m_poStateChangeCallback) {
			LOGDEBUG (TEXT ("State changed from ") << eOriginalState << TEXT (" to ") << eNewState);
			m_poStateChangeCallback->OnStateChange (eOriginalState, eNewState);
		}
		m_oStateChangeMutex.Leave ();
	}
	return bResult;
}

/// Attempts to start the JVM host service. See CClientJVM for more information.
///
/// @return TRUE if the startup was attempted or the service is already running, FALSE if there was an error
bool CClientService::StartJVM () {
	LOGINFO (TEXT ("Starting JVM"));
	if (m_poJVM) {
		if (m_poJVM->IsAlive ()) {
			LOGDEBUG (TEXT ("JVM already started"));
			return true;
		} else {
			LOGDEBUG (TEXT ("Closing defunct JVM handle"));
			delete m_poJVM;
		}
	}
	m_poJVM = CClientJVM::Start ();
	if (m_poJVM) {
		return true;
	} else {
		LOGWARN (TEXT ("Couldn't start JVM, error ") << GetLastError ());
		return false;
	}
}

/// Attempts to stop the JVM host service. See CClientJVM for more information.
///
/// @return TRUE if the stop was attempted or the service was not running, FALSE if there was an error
bool CClientService::StopJVM () {
	LOGINFO (TEXT ("Stopping JVM"));
	if (!m_poJVM) {
		LOGDEBUG (TEXT ("JVM not running"));
		return true;
	}
	bool bResult = m_poJVM->Stop ();
	if (!bResult) {
		LOGWARN (TEXT ("Couldn't stop JVM, error ") << GetLastError ());
	}
	delete m_poJVM;
	m_poJVM = NULL;
	return bResult;
}
