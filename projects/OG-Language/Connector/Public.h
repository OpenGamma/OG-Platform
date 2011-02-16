/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_public_h
#define __inc_og_language_connector_public_h

// Public definitions for the main interface to the library

#include <Util/Fudge.h>
#include <Util/Atomic.h>
#include "Client.h"
#include "SynchronousCalls.h"

class CConnector : public CClientService::CStateChange, public CClientService::CMessageReceived {
public:
	class CCallback;
private:
	CAtomicInt m_oRefCount;
	CClientService *m_poClient;
	CMutex m_oControlMutex;
	CAtomicPointer<CSemaphore*> m_oStartupSemaphorePtr;
	struct _callbackEntry {
		FudgeString strClass;
		CCallback *poCallback;
		struct _callbackEntry *pNext;
	};
	struct _callbackEntry *m_pCallbacks;
	CSynchronousCalls m_oSynchronousCalls;
	CConnector (CClientService *poClient);
protected:
	void OnStateChange (ClientServiceState ePreviousState, ClientServiceState eNewState);
	void OnMessageReceived (FudgeMsg msg);
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
	class CCallback {
	private:
		friend class CConnector;
		CAtomicInt m_oRefCount;
	protected:
		virtual void OnMessage (FudgeMsg msgPayload) = 0;
		virtual void OnThreadDisconnect () { }
	public:
		CCallback () : m_oRefCount (1) { }
		virtual ~CCallback () { assert (!m_oRefCount.Get ()); }
		void Retain () { m_oRefCount.IncrementAndGet (); }
		static void Release (CCallback *poCallback) { if (!poCallback->m_oRefCount.DecrementAndGet ()) delete poCallback; }
	};
	~CConnector ();
	static CConnector *Start ();
	bool Stop ();
	bool WaitForStartup (unsigned long lTimeout);
	void Retain () { m_oRefCount.IncrementAndGet (); }
	static void Release (CConnector *poConnector) { if (!poConnector->m_oRefCount.DecrementAndGet ()) delete poConnector; }
	bool Call (FudgeMsg msgPayload, FudgeMsg *pmsgResponse, unsigned long lTimeout);
	CCall *Call (FudgeMsg msgPayload);
	bool Send (FudgeMsg msgPayload);
	bool AddCallback (const TCHAR *pszClass, CCallback *poCallback);
	bool RemoveCallback (CCallback *poCallback);
};

#endif /* ifndef __inc_og_language_connector_public_h */