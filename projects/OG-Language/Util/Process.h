/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_process_h
#define __inc_og_language_util_process_h

// Process abstraction

#include "Unicode.h"

#ifdef _WIN32
#define PROCESS_REFERENCE	HANDLE
#else
#define PROCESS_REFERENCE	pid_t
#endif

class CProcess {
private:
	PROCESS_REFERENCE	m_process;
	CProcess (PROCESS_REFERENCE process) { m_process = process; }
public:
	~CProcess ();
	static bool GetCurrentModule (TCHAR *pszBuffer, size_t cchBuffer);
	static CProcess *FindByName (const TCHAR *pszExecutable);
	int GetProcessId () {
#ifdef _WIN32
		return ::GetProcessId (m_process);
#else
		return m_process;
#endif
	}
	bool Terminate () {
#ifdef _WIN32
		return TerminateProcess (m_process, 0) != 0;
#else
		return kill (m_process, SIGKILL) == 0;
#endif
	}
	bool Wait (unsigned long lTimeout);
	bool IsAlive ();
	static CProcess *Start (const TCHAR *pszExecutable, const TCHAR *pszParameters);
};

#endif /* ifndef __inc_og_language_util_process_h */