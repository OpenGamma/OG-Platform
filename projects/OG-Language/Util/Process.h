/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_process_h
#define __inc_og_language_util_process_h

#include "Unicode.h"

#ifdef _WIN32
typedef HANDLE PROCESS_REFERENCE;
#else
#include <signal.h>
typedef pid_t PROCESS_REFERENCE;
#endif

/// Abstraction of an operating system process.
class CProcess {
private:

	/// O/S handle to a process
	PROCESS_REFERENCE m_process;

	/// Creates a new process instance from an underlying handle.
	///
	/// @param[in] process O/S handle
	CProcess (PROCESS_REFERENCE process) { m_process = process; }

public:
	~CProcess ();
	static bool GetCurrentModule (TCHAR *pszBuffer, size_t cchBuffer);
	static CProcess *FindByName (const TCHAR *pszExecutable);
	bool Wait (unsigned long lTimeout) const;
	bool IsAlive () const;
	static CProcess *Start (const TCHAR *pszExecutable, const TCHAR *pszParam1 = NULL, const TCHAR *pszParam2 = NULL);

	/// Returns the identifier of the process.
	///
	/// @return process identifier
	int GetProcessId () const {
#ifdef _WIN32
		return ::GetProcessId (m_process);
#else
		return m_process;
#endif
	}

	/// Attempts to terminate the process.
	///
	/// @return true if terminated, false otherwise.
	bool Terminate () {
#ifdef _WIN32
		return TerminateProcess (m_process, 0) != 0;
#else
		return kill (m_process, SIGKILL) == 0;
#endif
	}

};

#endif /* ifndef __inc_og_language_util_process_h */
