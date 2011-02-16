/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_synchronouscalls_h
#define __inc_og_language_connector_synchronouscalls_h

// Blocking slots for synchronous calls

class CSynchronousCallSlot {
private:
	AtomicPointer<FudgeMsg> m_msg;
	fudge_i32 m_nIdentifier;
	fudge_i32 m_nSequence;
	friend class CSynchronousCalls;
	CSynchronousCallSlot (fudge_i32 nIdentifier);
	~CSynchronousCallSlot ();
public:
	fudge_i32 GetHandle ();
	fudge_i32 GetIdentifier () { return m_nIdentifier; }
	fudge_i32 GetSequence () { return m_nSequence; }
	FudgeMsg GetMessage ();
	void InvalidateMessage ();
	void Release ();
};

class CSynchronousCalls {
public:
	CSynchronousCalls ();
	~CSynchronousCalls ();
	void ClearAllSemaphores ();
	void SignalAllSemaphores ();
	CSynchronousCallSlot *Acquire ();
};

#endif /* ifndef __inc_og_language_connector_synchronouscalls_h */