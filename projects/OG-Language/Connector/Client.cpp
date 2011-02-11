/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Manages and communicates with the Java client

#include "Alert.h"
#include "Client.h"
#include "MessageDirectives.h"
#include "Settings.h"
#define FUDGE_NO_NAMESPACE
#include "com_opengamma_language_connector_ConnectorMessage.h"

LOGGING (com.opengamma.language.connector.Client);

static CFudgeInitialiser g_oInitialiseFudge;

class CClientService::CRunnerThread : public CThread {
private:
	FudgeMsg m_msgStash;
	CClientService *m_poService;
	unsigned long m_lHeartbeatTimeout;
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
		if (bWithStash && m_msgStash) {
			if ((status = ConnectorMessage_setStash (msg, m_msgStash)) != FUDGE_OK) {
				LOGFATAL (TEXT ("Couldn't create message, status ") << status);
				assert (0);
				status = FUDGE_OK;
				// Not good, but can carry on
			}
		}
		bool bResult = m_poService->Send (MESSAGE_DIRECTIVES_CLIENT, msg);
		FudgeMsg_release (msg);
		return bResult;
	}
	void DispatchAndRelease (FudgeMsgEnvelope env) {
		switch (FudgeMsgEnvelope_getDirectives (env)) {
		case MESSAGE_DIRECTIVES_CLIENT :
			TODO (TEXT ("Client message"));
			break;
		case MESSAGE_DIRECTIVES_USER :
			TODO (TEXT ("User message"));
			break;
		default :
			LOGWARN (TEXT ("Unknown message delivery directive ") << FudgeMsgEnvelope_getDirectives (env));
			break;
		}
		FudgeMsgEnvelope_release (env);
	}
	bool WaitForHeartbeatResponse () {
		LOGDEBUG (TEXT ("Waiting for heartbeat response"));
		FudgeMsgEnvelope env = m_poService->Recv (m_lHeartbeatTimeout);
		if (env) {
			DispatchAndRelease (env);
			m_poService->FirstConnectionOk ();
			return true;
		} else {
			LOGWARN (TEXT ("Heartbeat timeout exceeded"));
			return false;
		}
	}
protected:
	~CRunnerThread () {
		LOGDEBUG (TEXT ("Runner thread destroyed"));
	}
public:
	CRunnerThread (CClientService *poService) : CThread () {
		LOGDEBUG (TEXT ("Runner thread created"));
		CSettings oSettings;
		m_poService = poService;
		m_msgStash = NULL;
		m_lHeartbeatTimeout = oSettings.GetHeartbeatTimeout ();
	}
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
			if (!m_poService->StartJVM () || !m_poService->ConnectPipes ()) {
				if (m_poService->ClosePipes ()) {
					if (bRetry) {
						LOGWARN (TEXT ("Unable to connect to the Java framework"));
						CAlert::Bad (TEXT ("Restarting service"));
						bRetry = false;
						continue;
					}
				}
				LOGERROR (TEXT ("Unable to connect to Java framework"));
				CAlert::Bad (TEXT ("Unable to start service"));
				bStatus = false;
				break;
			}
			if (!SendHeartbeat (true) || !WaitForHeartbeatResponse ()) {
				if (bRetry) {
					LOGWARN (TEXT ("Java framework is not responding"));
					if (m_poService->ClosePipes ()) {
						CAlert::Bad (TEXT ("Restarting service"));
						m_poService->StopJVM ();
						bRetry = false;
						continue;
					}
				}
				LOGERROR (TEXT ("Java framework is not responding"));
				CAlert::Bad (TEXT ("Service is not responding"));
				// TODO: [XLS-43] (needs moving to PLAT) Can we get information from the log to pop-up if the user cloicks on the bubble?
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
				TODO (TEXT ("Handle message"));
				FudgeMsgEnvelope_release (env);
				LOGDEBUG (TEXT ("Waiting for message"));
				env = m_poService->Recv (m_lHeartbeatTimeout);
			}
			do {
				int ec = GetLastError ();
				if (ec == ETIMEDOUT) {
					if (!SendHeartbeat (false)) {
						LOGWARN (TEXT ("Couldn't send heartbeat, error ") << ec);
						break;
					}
				} else {
					LOGWARN (TEXT ("Couldn't read message, error ") << ec);
					break;
				}
				LOGDEBUG (TEXT ("Waiting for critical message"));
				env = m_poService->Recv (m_lHeartbeatTimeout);
				if (!env) {
					LOGWARN (TEXT ("Heartbeat missed"));
					break;
				}
				do {
					TODO (TEXT ("Handle message"));
					FudgeMsgEnvelope_release (env);
					LOGDEBUG (TEXT ("Waiting for message"));
					env = m_poService->Recv (m_lHeartbeatTimeout);
				} while (env);
			} while (m_poService->GetState () == RUNNING);
			if (!m_poService->SetState (STOPPING)) break;
			LOGINFO (TEXT ("Restarting Java framework"));
			CAlert::Bad (TEXT ("Reconnecting to service"));
			m_poService->ClosePipes ();
		} while (true);
		if (m_poService->GetState () != POISONED) {
			LOGINFO (TEXT ("Poisoning Java framework"));
			m_poService->SetState (POISONED);
			bStatus &= m_poService->SendPoison ();
		}
		bStatus &= m_poService->ClosePipes ();
		m_poService->SetState (bStatus ? STOPPED : ERRORED);
		LOGINFO (TEXT ("Runner thread stopped"));
	}
};

CClientService::CClientService ()
: m_oPipesSemaphore (1, 1) {
	m_poStateChangeCallback = NULL;
	m_poMessageReceivedCallback = NULL;
	m_eState = STOPPED;
	m_poRunner = NULL;
	m_poPipes = NULL;
	m_poJVM = NULL;
}

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
}

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

ClientServiceState CClientService::GetState () {
	m_oStateMutex.Enter ();
	ClientServiceState eState = m_eState;
	m_oStateMutex.Leave ();
	return eState;
}

bool CClientService::Send (FudgeMsg msg) {
	return Send (MESSAGE_DIRECTIVES_USER, msg);
}

void CClientService::SetStateChangeCallback (CStateChange *poCallback) {
	m_oStateChangeMutex.Enter ();
	m_poStateChangeCallback = poCallback;
	m_oStateChangeMutex.Leave ();
}

void CClientService::SetMessageReceivedCallback (CMessageReceived *poCallback) {
	m_oMessageReceivedMutex.Enter ();
	m_poMessageReceivedCallback = poCallback;
	m_oMessageReceivedMutex.Leave ();
}

bool CClientService::ClosePipes () {
	LOGINFO (TEXT ("Closing pipes"));
	m_oPipesSemaphore.Wait ();
	if (m_poPipes) {
		delete m_poPipes;
		m_poPipes = NULL;
		m_oPipesSemaphore.Signal ();
		return true;
	} else {
		m_oPipesSemaphore.Signal ();
		LOGWARN (TEXT ("Pipes not created"));
		return false;
	}
}

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
	bool bResult = m_poPipes->Connect (poPipe, lTimeout);
	if (!bResult) {
		LOGWARN (TEXT ("Couldn't connect to JVM service, error ") << GetLastError ());
	}
	delete poPipe;
	m_oPipesSemaphore.Signal ();
	return bResult;
}

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

// This must only be called from the thread that creates and connects the pipes. This then
// doesn't need to acquire the pipe semaphore as the object won't be modified concurrently.
// Another thread might be sending, but that's it.
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

bool CClientService::Send (int cProcessingDirectives, FudgeMsg msg) {
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
	m_oPipesSemaphore.Wait (m_lSendTimeout);
	if (m_poPipes) {
		int nPoll = 0;
		long lStartTime = GetTickCount ();
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
#endif
					LOGWARN (TEXT ("Couldn't write message, error ") << ec);
				}
			} else {
				LOGWARN (TEXT ("Couldn't write message, error ") << ec);
			}
			SetLastError (ec);
			bResult = false;
		}
	} else {
		LOGWARN (TEXT ("Pipes not available for message"));
		bResult = false;
		SetLastError (ENOTCONN);
	}
	m_oPipesSemaphore.Signal ();
	free (ptrBuffer);
	return bResult;
}

bool CClientService::SendPoison () {
	LOGINFO (TEXT ("Sending poison to JVM"));
	TODO (__FUNCTION__);
	return false;
}

// Returns false if the state change wasn't allowed because a poison was in progress
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
	if (bResult) {
		m_oStateChangeMutex.Enter ();
		if (m_poStateChangeCallback) {
			LOGDEBUG (TEXT ("State changed from ") << eOriginalState << TEXT (" to ") << eNewState);
			m_poStateChangeCallback->OnStateChange (eOriginalState, eNewState);
		}
		m_oStateChangeMutex.Leave ();
	}
	return bResult;
}

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

bool CClientService::StopJVM () {
	LOGINFO (TEXT ("Stopping JVM"));
	if (!m_poJVM) {
		LOGDEBUG (TEXT ("JVM not running"));
		return true;
	}
	if (m_poJVM->Stop ()) {
		delete m_poJVM;
		m_poJVM = NULL;
		return true;
	} else {
		LOGWARN (TEXT ("Couldn't stop JVM, error ") << GetLastError ());
		return false;
	}
}
