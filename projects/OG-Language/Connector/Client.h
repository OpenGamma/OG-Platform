/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_client_h
#define __inc_og_language_connector_client_h

// Manages and communicates with the Java client

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
	class CStateChange {
	protected:
		friend class CClientService;
		virtual void OnStateChange (ClientServiceState ePreviousState, ClientServiceState eNewState) = 0;
	};
	class CMessageReceived {
	protected:
		friend class CClientService;
		virtual void OnMessageReceived (FudgeMsg msg) = 0;
	};
private:
	class CRunnerThread;
	// Attributes
	CAtomicInt m_oRefCount;
	CMutex m_oState;
	CMutex m_oStop;
	ClientServiceState m_eState;
	CStateChange *m_poStateChangeCallback;
	CMessageReceived *m_poMessageReceivedCallback;
	CThread *m_poRunner;
	// Private constructor - stops stack allocation
	CClientService ();
	// Thread runner callbacks
	bool ClosePipes ();
	bool ConnectPipes ();
	bool CreatePipes ();
	bool SendPoison ();
	bool SetState (ClientServiceState eNewState);
	bool StartJVM ();
	bool StopJVM ();
public:
	// Creation
	static CClientService *Create () { return new CClientService (); }
	~CClientService ();
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