/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_jvm_h
#define __inc_og_language_connector_jvm_h

#include <Util/Process.h>

/// JVM host process management. The hosted JVM will run the client Java stack.
class CClientJVM {
private:

	/// Flag to indicate if this is the first connection to a fresh process, or a
	/// connection to an existing process.
	bool m_bFirstConnection;

	/// O/S handle to the host process.
	CProcess *m_poProcess;

#ifdef _WIN32
	/// Service handle if the JVM host is installed as such.
	SC_HANDLE m_hService;

	CClientJVM (SC_HANDLE hService, bool bFirstConnection);
#endif /* ifdef _WIN32 */

	CClientJVM (CProcess *poProcess, bool bFirstConnection);
	static CClientJVM *StartExecutable (const TCHAR *pszExecutable, unsigned long lStartTimeout);
#ifdef _WIN32
	static CClientJVM *StartService (const TCHAR *pszServiceName, const TCHAR *pszExecutable, unsigned long lPollTimeout, unsigned long lStartTimeout, unsigned long lStopTimeout);
#endif /* ifdef _WIN32 */
public:
	~CClientJVM ();
	static CClientJVM *Start ();
	bool Stop ();
	bool FirstConnection ();
	bool IsAlive () const;
};

#endif /* ifndef __inc_og_language_connector_jvm_h */
