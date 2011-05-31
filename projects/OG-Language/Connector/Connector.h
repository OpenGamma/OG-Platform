/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_connector_h
#define __inc_og_language_connector_connector_h

// Public definitions for the main interface to the library

#include <assert.h>
#include <Util/Asynchronous.h>
#include <Util/Fudge.h>
#include "Client.h"
#include "SynchronousCalls.h"
#ifdef _WIN32
#include <Util/Library.h>
#endif /* ifdef _WIN32 */

// Note the "const" behaviour applies to the "start"/"stop" state, and not the internals needed
// for message I/O. Thus a "const" connector can be used to send/receive messages but can't be
// configured.
class CConnector : public CClientService::CStateChange, public CClientService::CMessageReceived {
public:
	class CCallback {
	private:
		friend class CConnector;
		mutable CAtomicInt m_oRefCount;
#ifdef _WIN32
		CLibraryLock *m_poModuleLock;
#endif /* ifdef _WIN32 */
	protected:
#ifdef _WIN32
		void LockModule (const void *pAddressInModule) {
			assert (!m_poModuleLock);
			m_poModuleLock = CLibraryLock::CreateFromAddress (pAddressInModule);
		}
#endif /* ifdef _WIN32 */
		virtual void OnMessage (FudgeMsg msgPayload) = 0;
		virtual void OnThreadDisconnect () { }
		virtual ~CCallback () {
			assert (!m_oRefCount.Get ());
#ifdef _WIN32
			CLibraryLock::UnlockAndDelete (m_poModuleLock);
#endif /* ifdef _WIN32 */
		}
	public:
		CCallback () : m_oRefCount (1) {
#ifdef _WIN32
			m_poModuleLock = NULL;
#endif /* ifdef _WIN32 */
		}
		void Retain () const { m_oRefCount.IncrementAndGet (); }
		static void Release (const CCallback *poCallback) { if (!poCallback->m_oRefCount.DecrementAndGet ()) delete poCallback; }
	};
private:
	mutable CAtomicInt m_oRefCount;
	CClientService *m_poClient;
	mutable CMutex m_oMutex;
	mutable CAtomicPointer<CSemaphore*> m_oStartupSemaphorePtr;
	class CCallbackEntry {
	private:
		mutable CAtomicInt m_oRefCount;
		FudgeString m_strClass;
		CCallback *m_poCallback;
	public:
		CCallbackEntry *m_poNext;
		CCallbackEntry (FudgeString strClass, CCallback *poCallback, CCallbackEntry *poNext)
			: m_oRefCount (1) {
			m_strClass = strClass;
			poCallback->Retain ();
			m_poCallback = poCallback;
			m_poNext = poNext;
		}
		~CCallbackEntry () {
			assert (m_oRefCount.Get () == 0);
			if (m_strClass) FudgeString_release (m_strClass);
			CCallback::Release (m_poCallback);
		}
		void Retain () const { m_oRefCount.IncrementAndGet (); }
		static void Release (const CCallbackEntry *poEntry) { if (!poEntry->m_oRefCount.DecrementAndGet ()) delete poEntry; }
		bool IsClass (FudgeString strClass) const { return !FudgeString_compare (m_strClass, strClass); }
		bool IsCallback (const CCallback *poCallback) const { return m_poCallback == poCallback; }
		void FreeString () { FudgeString_release (m_strClass); m_strClass = NULL; }
		void OnMessage (FudgeMsg msgPayload);
		void OnThreadDisconnect () { m_poCallback->OnThreadDisconnect (); }
	};
	class CCallbackEntry *m_poCallbacks;
	mutable CSynchronousCalls m_oSynchronousCalls;
	CAsynchronous *m_poDispatch;
	CAtomicPointer<IRunnable*> m_oOnEnterRunningState;
	CAtomicPointer<IRunnable*> m_oOnExitRunningState;
	CAtomicPointer<IRunnable*> m_oOnEnterStableNonRunningState;
	CConnector (CClientService *poClient);
	void OnEnterRunningState ();
	void OnExitRunningState ();
	void OnEnterStableNonRunningState ();
	friend class CConnectorMessageDispatch;
	friend class CConnectorThreadDisconnectDispatch;
	friend class CConnectorDispatcher;
protected:
	void OnStateChange (ClientServiceState ePreviousState, ClientServiceState eNewState);
	void OnMessageReceived (FudgeMsg msg);
	void OnDispatchThreadDisconnect ();
public:
	class CCall {
	private:
		friend class CConnector;
		CSynchronousCallSlot *m_poSlot;
		CCall (CSynchronousCallSlot *poSlot);
	public:
		~CCall ();
		bool Cancel ();
		bool WaitForResult (FudgeMsg *pmsgResponse, unsigned long lTimeout);
	};
	~CConnector ();
	static CConnector *Start (const TCHAR *pszLanguageID);
	bool Stop ();
	bool WaitForStartup (unsigned long lTimeout) const;
	void Retain () const { m_oRefCount.IncrementAndGet (); }
	static void Release (const CConnector *poConnector) { if (!poConnector->m_oRefCount.DecrementAndGet ()) delete poConnector; }
	bool Call (FudgeMsg msgPayload, FudgeMsg *pmsgResponse, unsigned long lTimeout) const;
	CCall *Call (FudgeMsg msgPayload) const;
	bool Send (FudgeMsg msgPayload) const;
	bool AddCallback (const TCHAR *pszClass, CCallback *poCallback);
	bool RemoveCallback (const CCallback *poCallback);
	bool RecycleDispatchThread ();
	// The CConnector will take ownership of these references and delete them when done (or another callback set)
	void OnEnterRunningState (IRunnable *poRunnable);
	void OnExitRunningState (IRunnable *poRunnable);
	void OnEnterStableNonRunningState (IRunnable *poRunnable);
};

#endif /* ifndef __inc_og_language_connector_connector_h */
