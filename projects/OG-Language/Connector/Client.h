/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_client_h
#define __inc_og_language_connector_client_h

// Manages and communicates with the Java client

#include "JVM.h"
#include "Pipes.h"

enum ClientServiceState {
	STARTING,
	RUNNING,
	STOPPING,
	POISONED,
	ERRORED,
	STOPPED
};

class CClientService {
public:
	// Callback for state changes (e.g. to be notified when fully connected). The callback is part of the
	// main service event thread so should not block or call back to other blocking operations (like
	// sending a message).
	class CStateChange {
	protected:
		friend class CClientService;
		virtual void OnStateChange (ClientServiceState ePreviousState, ClientServiceState eNewState) = 0;
	};
	// Callback for messages received. The callback is part of the main service event thread so should not
	// block or call back to other blocking operations (like sending a message).
	class CMessageReceived {
	protected:
		friend class CClientService;
		virtual void OnMessageReceived (FudgeMsg msg) = 0;
	};
private:
	class CRunnerThread;
	// Attributes
	CAtomicInt m_oRefCount;
	CMutex m_oStateMutex;
	CMutex m_oStopMutex;
	ClientServiceState m_eState;
	CMutex m_oStateChangeMutex;
	CStateChange *m_poStateChangeCallback;
	CMutex m_oMessageReceivedMutex;
	CMessageReceived *m_poMessageReceivedCallback;
	CThread *m_poRunner;
	CSemaphore m_oPipesSemaphore;
	CClientPipes *m_poPipes;
	CClientJVM *m_poJVM;
	unsigned long m_lSendTimeout;
	unsigned long m_lShortTimeout;
	// Private constructor - stops stack allocation
	CClientService ();
	~CClientService ();
	// Thread runner callbacks
	bool ClosePipes ();
	bool ConnectPipes ();
	bool CreatePipes ();
	bool DispatchAndRelease (FudgeMsgEnvelope env);
	bool IsFirstConnection () { return m_lShortTimeout != 0; }
	void FirstConnectionOk () { m_lSendTimeout = m_lShortTimeout; m_lShortTimeout = 0; }
	bool HeartbeatNeeded (unsigned long lTimeout) { return GetTickCount () - m_poPipes->GetLastWrite () >= lTimeout; }
	FudgeMsgEnvelope Recv (unsigned long lTimeout);
	bool Send (int cProcessingDirectives, FudgeMsg msg);
	bool SendPoison ();
	bool SetState (ClientServiceState eNewState);
	bool StartJVM ();
	bool StopJVM ();
public:
	// Creation
	static CClientService *Create () { return new CClientService (); }
	void Retain () { m_oRefCount.IncrementAndGet (); }
	static void Release (CClientService *poClientService) { if (!poClientService->m_oRefCount.DecrementAndGet ()) delete poClientService; }
	// Control
	bool Stop ();
	bool Start ();
	ClientServiceState GetState ();
	bool Send (FudgeMsg msg);
	void SetStateChangeCallback (CStateChange *poCallback);
	void SetMessageReceivedCallback (CMessageReceived *poCallback);
};

#endif /* ifndef __inc_og_language_connect_client_h */