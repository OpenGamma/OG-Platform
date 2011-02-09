/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Manages and communicates with the Java client

#include "Alert.h"
#include "Client.h"
#include "Settings.h"

LOGGING (com.opengamma.language.connector.Client);

class CClientService::CRunnerThread : public CThread {
private:
	CClientService *m_poService;
	bool SendHeartbeat (bool bWithStash) {
		TODO (TEXT ("Copy code from OG-Excel"));
		return false;
	}
	bool WaitForHeartbeatResponse () {
		TODO (TEXT ("Copy code from OG-Excel"));
		return false;
	}
protected:
	~CRunnerThread () {
		LOGDEBUG (TEXT ("Runner thread destroyed"));
	}
public:
	CRunnerThread (CClientService *poService) : CThread () {
		LOGDEBUG (TEXT ("Runner thread created"));
		m_poService = poService;
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
			if (!SendHeartbeat (TRUE) || !WaitForHeartbeatResponse ()) {
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
				CAlert::Bad (TEXT ("Service error"));
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
			TODO (TEXT ("Message dispatch loop"));
			if (!m_poService->SetState (POISONED)) break;
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

CClientService::CClientService () {
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
	m_oState.Enter ();
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
	m_oState.Leave ();
	return bResult;
}

bool CClientService::Stop () {
	LOGINFO (TEXT ("Stopping"));
	// Exclude concurrent calls to Stop.
	m_oStop.Enter ();
	m_oState.Enter ();
	CThread *poRunner;
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
	// Release the state lock before blocking
	m_oState.Leave ();
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
	m_oStop.Leave ();
	return bResult;
}

ClientServiceState CClientService::GetState () {
	m_oState.Enter ();
	ClientServiceState eState = m_eState;
	m_oState.Leave ();
	return eState;
}

bool CClientService::Send (FudgeMsg msg) {
	TODO (__FUNCTION__);
	return false;
}

void CClientService::SetStateChangeCallback (CStateChange *poCallback) {
	m_oStateChange.Enter ();
	m_poStateChangeCallback = poCallback;
	m_oStateChange.Leave ();
}

void CClientService::SetMessageReceivedCallback (CMessageReceived *poCallback) {
	m_oMessageReceived.Enter ();
	m_poMessageReceivedCallback = poCallback;
	m_oMessageReceived.Leave ();
}

bool CClientService::ClosePipes () {
	LOGINFO (TEXT ("Closing pipes"));
	if (m_poPipes) {
		delete m_poPipes;
		m_poPipes = NULL;
		return true;
	} else {
		LOGWARN (TEXT ("Pipes not created"));
		return false;
	}
}

bool CClientService::ConnectPipes () {
	LOGINFO (TEXT ("Connecting pipes to JVM"));
	if (!m_poPipes) {
		LOGFATAL (TEXT ("Pipes not created"));
		assert (0);
		return false;
	}
	if (!m_poJVM) {
		LOGFATAL (TEXT ("JVM not created"));
		assert (0);
		return false;
	}
	CSettings oSettings;
	const TCHAR *pszPipeName = oSettings.GetConnectionPipe ();
	LOGDEBUG (TEXT ("Connecting to ") << pszPipeName);
	unsigned long lTimeout = m_poJVM->FirstConnection () ? oSettings.GetStartTimeout () : oSettings.GetConnectTimeout ();
	m_lSendTimeout = lTimeout;
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
					LOGWARN (TEXT ("Timeout waiting for JVM service to open ") << pszPipeName);
					return false;
				} else {
					if (m_poJVM->IsAlive ()) {
						LOGDEBUG (TEXT ("Waiting for JVM service to open pipe"));
						CThread::Sleep (oSettings.GetServicePoll ());
					} else {
						LOGWARN (TEXT ("JVM service terminated before opening ") << pszPipeName);
						return false;
					}
				}
			} else {
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
	return bResult;
}

bool CClientService::CreatePipes () {
	LOGINFO (TEXT ("Creating pipes"));
	if (m_poPipes) {
		LOGFATAL (TEXT ("Pipes already created"));
		assert (0);
		return false;
	}
	m_poPipes = CClientPipes::Create ();
	if (m_poPipes) {
		return true;
	} else {
		LOGWARN (TEXT ("Couldn't create pipes, error ") << GetLastError ());
		return false;
	}
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
	m_oState.Enter ();
	if ((m_eState == POISONED) && (eNewState != STOPPED) && (eNewState != ERRORED) && (eNewState != POISONED)) {
		LOGDEBUG (TEXT ("Currently in poison state, not changing"));
		bResult = false;
	} else {
		eOriginalState = m_eState;
		m_eState = eNewState;
		bResult = true;
	}
	m_oState.Leave ();
	if (bResult) {
		m_oStateChange.Enter ();
		if (m_poStateChangeCallback) {
			LOGDEBUG (TEXT ("State changed from ") << eOriginalState << TEXT (" to ") << eNewState);
			m_poStateChangeCallback->OnStateChange (eOriginalState, eNewState);
		}
		m_oStateChange.Leave ();
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
