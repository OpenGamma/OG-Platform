/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_jvm_h
#define __inc_og_language_connector_jvm_h

// JVM process/service management

#include <Util/Process.h>

class CClientJVM {
private:
	bool m_bFirstConnection;
	CProcess *m_poProcess;
#ifdef _WIN32
	SC_HANDLE m_hService;
	CClientJVM (SC_HANDLE hService, bool bFirstConnection);
#endif /* ifdef _WIN32 */
	CClientJVM (CProcess *poProcess, bool bFirstConnection);
	static CClientJVM *StartExecutable (const TCHAR *pszExecutable, unsigned long lStartTimeout);
	static CClientJVM *StartService (const TCHAR *pszServiceName, const TCHAR *pszExecutable, unsigned long lPollTimeout, unsigned long lStartTimeout, unsigned long lStopTimeout);
public:
	~CClientJVM ();
	static CClientJVM *Start ();
	bool Stop ();
	bool FirstConnection ();
	bool IsAlive ();
};

#endif /* ifndef __inc_og_language_connector_jvm_h */
