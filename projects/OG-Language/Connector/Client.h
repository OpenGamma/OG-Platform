/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_client_h
#define __inc_og_language_connector_client_h

#include "JVM.h"
#include "Pipes.h"
#include <Util/Mutex.h>
#include <Util/Semaphore.h>
#include <Util/Thread.h>

/// States the client execution stack can be in. This is a subset of the states that a CConnector
/// instance can be in - as that allows an additional "no current client" state.
enum ClientServiceState {
	/// The JVM host process is starting up, or is not yet accepting connections.
	STARTING,
	/// The JVM host process is running and the Java stack is accepting connections.
	RUNNING,
	/// The Java stack has stopped accepting connections and the JVM host process is in the process of
	/// being terminated.
	STOPPING,
	/// The Java stack should not be available, and the state is heading towards ERRORED or STOPPED.
	POISONED,
	/// A problem has occurred, the Java stack is not available and the JVM host process is not running.
	ERRORED,
	/// The JVM host process is not running after a controlled shutdown.
	STOPPED
};

/// The client connection service. An instance manages a JVM host process (or connects to a shared one),
/// and coordinates the pipes used to communicate with it. The client provides basic bi-directional
/// communication only, the CConnector wrapper builds a more robust message sending framework on top.
///
/// The "const" modifier applies to the start/stop state. A "const" client can send and receive messages.
///
/// This is a reference counted object using the Retain and Release methods.
class CClientService {
public:

	/// Callback for state changes (e.g. to be notified when fully connected).
	class CStateChange {
	protected:
		friend class CClientService;

		/// Called when the client state has changed.
		///
		/// The callback is part of the main service event thread so should not block or call back to other
		/// blocking operations (like sending a message), or ones that could cause a further state change
		/// and potentially an infinite recursion.
		///
		/// @param[in] ePreviousState the state the client has changed from
		/// @param[in] eNewState the state the client has changed to
		virtual void OnStateChange (ClientServiceState ePreviousState, ClientServiceState eNewState) = 0;

	};

	/// Callback for messages received.
	class CMessageReceived {
	protected:
		friend class CClientService;

		/// Called when a message has been received from the Java stack.
		///
		/// The callback is part of the main service event thread so should not block or call back to
		/// other blocking operations (like sending a message).
		///
		/// @param[in] msg the message received, never NULL
		virtual void OnMessageReceived (FudgeMsg msg) = 0;

	};
private:
	class CRunnerThread;

	/// Client language ID. This will be sent to the Java stack and may influence its configuration
	/// settings.
	TCHAR *m_pszLanguageID;

	/// Reference count.
	mutable CAtomicInt m_oRefCount;

	/// Critical section to protect the state; on entry and exit the state of member variables must
	/// be consistent with that expected of the m_eState state.
	mutable CMutex m_oStateMutex;

	/// Critical section to prevent concurrent calls to Stop from interferring with each other.
	CMutex m_oStopMutex;

	/// Current state.
	ClientServiceState m_eState;

	/// Critical section to protect the state change callback.
	CMutex m_oStateChangeMutex;

	/// State change user callback, NULL if none has been requested.
	CStateChange *m_poStateChangeCallback;

	/// Critical section to protect the message received callback.
	CMutex m_oMessageReceivedMutex;

	/// Message received callback, NULL if none has been requested.
	CMessageReceived *m_poMessageReceivedCallback;

	/// Event thread. This thread will be responsible for starting the JVM, restarting it on failure
	/// and blocking on read requests to receive incoming messages from the Java stack.
	CRunnerThread *m_poRunner;

	/// Semaphore to protect access to the pipes. A critical section is not appropriate as it would
	/// not allow a timeout on acquiring the pipes (e.g. waiting for them to be connected).
	mutable CSemaphore m_oPipesSemaphore;

	/// Current pipe pair, or NULL if there is no Java stack or it is not connected.
	CClientPipes *m_poPipes;

	/// JVM hosting the Java stack.
	CClientJVM *m_poJVM;

	/// Timeout to use for sending messages, or waiting for the resources needed to send a message.
	unsigned long m_lSendTimeout;

	/// The "short" timeout normally used on messages. When a connection is first established, the
	/// message send timeout is significantly longer to allow for JVM startup time and Java stack
	/// initialisation.
	unsigned long m_lShortTimeout;

	/// Tests if this is the first connection. If it is the first connection, the "short" timeout is
	/// available. After the first connection is made, the "short" timeout is reset.
	///
	/// @return TRUE if this is the first connection, FALSE if the first connection has already been
	///         reported ok.
	bool IsFirstConnection () const { return m_lShortTimeout != 0; }

	/// Reports the first connection as successful. This sets the message timeout to the normal "short"
	/// value from the longer one used to establish the initial connection. This must not be called
	/// multiple times without resetting the current and "short" timeout variables. After being called,
	/// if the variables haven't been reset, IsFirstConnection will return FALSE.
	void FirstConnectionOk () { m_lSendTimeout = m_lShortTimeout; m_lShortTimeout = 0; }

	/// Tests whether a heartbeat signal should be sent based on the last message write time.
	///
	/// @param[in] lTimeout maximum time to allow between messages in milliseconds
	/// @return TRUE if no messages have been sent for the timeout period, FALSE if at least one was
	///         written.
	bool HeartbeatNeeded (unsigned long lTimeout) const { return GetTickCount () - m_poPipes->GetLastWrite () >= lTimeout; }

	CClientService (const TCHAR *pszLanguageID);
	~CClientService ();
	void ClosePipes ();
	bool ConnectPipes ();
	bool CreatePipes ();
	bool DispatchAndRelease (FudgeMsgEnvelope env);
	FudgeMsgEnvelope Recv (unsigned long lTimeout);
	bool Send (int cProcessingDirectives, FudgeMsg msg) const;
	bool SendPoison ();
	bool SetState (ClientServiceState eNewState);
	bool StartJVM ();
	bool StopJVM ();
public:

	/// Creates a new service object.
	///
	/// @param[in] pszLanguageID the language identifier to be sent to the Java stack
	/// @return the service object, or NULL if there was a problem
	static CClientService *Create (const TCHAR *pszLanguageID) { return new CClientService (pszLanguageID); }

	/// Increments the reference count.
	void Retain () const { m_oRefCount.IncrementAndGet (); }

	/// Decrements the reference count, deleting the object when it reaches zero.
	///
	/// @param[in] poClientService object to release, never NULL
	static void Release (const CClientService *poClientService) { if (!poClientService->m_oRefCount.DecrementAndGet ()) delete poClientService; }

	bool Stop ();
	bool Start ();
	ClientServiceState GetState () const;
	bool Send (FudgeMsg msg) const;
	void SetStateChangeCallback (CStateChange *poCallback);
	void SetMessageReceivedCallback (CMessageReceived *poCallback);
};

#endif /* ifndef __inc_og_language_connect_client_h */
