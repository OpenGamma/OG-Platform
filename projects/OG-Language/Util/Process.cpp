/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

#include "Logging.h"
#include "Process.h"
#include "String.h"
#include "Thread.h"
#ifdef _WIN32
#include "DllVersion.h"
#endif /* ifdef _WIN32 */

LOGGING (com.opengamma.language.util.Process);

/// Destory a process instance. This only closes the underlying reference, the process is not affected.
CProcess::~CProcess () {
#ifdef _WIN32
	CloseHandle (m_process);
#else
	// Just an ID; don't need to do anything
#endif
}

/// Obtains the EXE, DLL or DSO containing this static code.
///
/// @param[out] pszBuffer buffer to receive name of the module
/// @param[in] cchBuffer size of the buffer in characters
/// @return true if the buffer was populated, false otherwise
bool CProcess::GetCurrentModule (TCHAR *pszBuffer, size_t cchBuffer) {
#ifdef _WIN32
	HMODULE hModule = CDllVersion::GetCurrentModule ();
#ifdef _WIN64
	if (cchBuffer > MAXDWORD) cchBuffer = MAXDWORD;
#endif /* ifdef _WIN64 */
	return hModule && GetModuleFileName (hModule, pszBuffer, (DWORD)cchBuffer);
#else /* ifdef _WIN32 */
	// TODO: this is flawed and probably not portable; we probably want the .so file and not the executing image
	return readlink ("/proc/self/exe", pszBuffer, cchBuffer) > 0;
#endif /* ifdef _WIN32 */
}

/// Finds a process instance that is running the named image.
///
/// @param[in] pszExecutable image to search for
/// @return process instance or NULL if not found
CProcess *CProcess::FindByName (const TCHAR *pszExecutable) {
#ifdef _WIN32
	DWORD cdwProcesses = 512;
	DWORD cbProcesses;
	PDWORD pdwProcesses = NULL;
	do {
		if (pdwProcesses) {
			delete pdwProcesses;
		}
		cdwProcesses <<= 1;
		pdwProcesses = new DWORD[cdwProcesses];
		if (!pdwProcesses) {
			LOGFATAL (TEXT ("Out of memory"));
			return NULL;
		}
		LOGDEBUG (TEXT ("Calling EnumProcesses with ") << cdwProcesses << TEXT (" slots"));
		if (!EnumProcesses (pdwProcesses, cdwProcesses * sizeof (DWORD), &cbProcesses)) {
			LOGWARN (TEXT ("Couldn't enumerate active processes, error ") << GetLastError ());
			delete pdwProcesses;
			return NULL;
		}
	} while (cbProcesses == cdwProcesses * sizeof (DWORD));
	cdwProcesses = cbProcesses / sizeof (DWORD);
	LOGDEBUG (TEXT ("Checking ") << cdwProcesses << TEXT (" processes"));
	HANDLE hProcess = NULL;
	// Lookup correct rights mask - PROCESS_QUERY_INFORMATION on XP/W2K, PROCESS_QUERY_LIMITED_INFORMATION on newer
	OSVERSIONINFO version;
	ZeroMemory (&version, sizeof (version));
	version.dwOSVersionInfoSize = sizeof (version);
	GetVersionEx (&version);
	DWORD dwRights = ((version.dwMajorVersion < 6) ? PROCESS_QUERY_INFORMATION : PROCESS_QUERY_LIMITED_INFORMATION) | PROCESS_TERMINATE | SYNCHRONIZE;
	while (cdwProcesses) {
		DWORD dwProcessID = pdwProcesses[--cdwProcesses];
		if (!dwProcessID) continue;
		hProcess  = OpenProcess (dwRights, FALSE, dwProcessID);
		if (hProcess) {
			TCHAR sz[MAX_PATH];
			DWORD cch = MAX_PATH;
			if (QueryFullProcessImageName (hProcess, 0, sz, &cch)) {
				LOGDEBUG (TEXT ("Checking process ") << dwProcessID << TEXT (", executable ") << sz);
				if (!_tcsicmp (pszExecutable, sz)) {
					DWORD dwExitCode;
					if (GetExitCodeProcess (hProcess, &dwExitCode)) {
						if (dwExitCode == STILL_ACTIVE) {
							LOGINFO (TEXT ("Found process ") << dwProcessID << TEXT (" with executable ") << sz);
							break;
						} else {
							LOGINFO (TEXT ("Found terminated process ") << dwProcessID << TEXT (" with executable ") << sz);
						}
					} else {
						LOGWARN (TEXT ("Couldn't get exit code for process ") << dwProcessID << TEXT (", error ") << GetLastError ());
					}
				}
			} else {
				DWORD dwError = GetLastError ();
				// Note: sometimes get ec 6 for the process we've just launched; should we pause and retry?
				if (dwError == ERROR_PARTIAL_COPY) {
					LOGDEBUG (TEXT ("Partial copy only of process ") << dwProcessID);
				} else {
					LOGWARN (TEXT ("Couldn't get filename for process ") << dwProcessID << TEXT (", error ") << dwError);
				}
			}
			CloseHandle (hProcess);
			hProcess = NULL;
		} else {
			DWORD dwError = GetLastError ();
			if (dwError == ERROR_ACCESS_DENIED) {
				LOGDEBUG (TEXT ("Access denied to process ") << dwProcessID);
			} else {
				LOGWARN (TEXT ("Error opening process ") << dwProcessID << TEXT (", error ") << dwError);
			}
		}
	}
	delete pdwProcesses;
	return hProcess ? new CProcess (hProcess) : NULL;
#else /* ifdef _WIN32 */
	DIR *dir = opendir ("/proc");
	if (!dir) {
		LOGWARN (TEXT ("Couldn't open /proc, error ") << GetLastError ());
		return NULL;
	}
	struct dirent *dp;
	while ((dp = readdir (dir)) != NULL) {
		if (dp->d_name[0] == '.') {
			continue;
		}
		if (dp->d_type & DT_DIR) {
			int pid = atoi (dp->d_name);
			if (pid > 0) {
				TCHAR sz[16], szExecutable[256];
				StringCbPrintf (sz, sizeof (sz), TEXT ("/proc/%d/exe"), pid);
				int cb = readlink (sz, szExecutable, (sizeof (szExecutable) / sizeof (TCHAR)) - 1);
				if (cb > 0) {
					szExecutable[cb] = 0;
					LOGDEBUG (TEXT ("Checking ") << pid << TEXT (" with executable ") << szExecutable);
					if (!_tcscmp (szExecutable, pszExecutable)) {
						LOGINFO (TEXT ("Found process ") << pid << TEXT (" with executable ") << szExecutable);
						closedir (dir);
						return new CProcess (pid);
					}
				} else {
					LOGDEBUG (TEXT ("Couldn't read process ") << pid << TEXT (" info, error ") << GetLastError ());
				}
			}
		}
	}
	closedir (dir);
	return NULL;
#endif /* ifdef _WIN32 */
}

#ifndef _WIN32
/// Tests if a process is still running.
///
/// @param[in] pid process identifier
/// @return true if still running, false otherwise
static bool _IsRunning (pid_t pid) {
	TCHAR sz[32], szExecutable[256];
	StringCbPrintf (sz, sizeof (sz), TEXT ("/proc/%d/exe"), pid);
	if (readlink (sz, szExecutable, sizeof (szExecutable) / sizeof (TCHAR)) > 0) {
		return true;
	}
	int ec = GetLastError ();
	if (ec == ENOENT) {
		LOGDEBUG (TEXT ("Process ") << pid << TEXT (" no longer valid"));
		return false;
	}
	if (ec == EACCES) {
		// To have the PID we either started the process, or spawned it (or the PID has been recycled)
		LOGDEBUG (TEXT ("Process ") << pid << TEXT (" running, but access denied"));
		int status;
		waitpid (pid, &status, WNOHANG);
		return true;
	}
	LOGWARN (TEXT ("Process ") << pid << TEXT (", error ") << ec);
	return false;
}
#endif

/// Wait for a process to terminate.
///
/// @param[in] lTimeout maximum time to wait in milliseconds
/// @return true if the process terminated, false otherwise
bool CProcess::Wait (unsigned long lTimeout) const {
#ifdef _WIN32
	switch (WaitForSingleObject (m_process, lTimeout)) {
	case WAIT_ABANDONED :
		SetLastError (ERROR_ABANDONED_WAIT_0);
		return false;
	case WAIT_OBJECT_0 :
		return true;
	case WAIT_TIMEOUT :
		SetLastError (ETIMEDOUT);
		return false;
	default :
		return false;
	}
#else
#define POLL_TIMEOUT	1000
	unsigned long n;
	for (n = 0; _IsRunning (m_process) && (n < lTimeout / POLL_TIMEOUT); n++) {
		CThread::Sleep (POLL_TIMEOUT);
	}
	return !_IsRunning (m_process);
#endif
}

/// Starts a new process from the given image and command line parameters
///
/// @param[in] pszExecutable image to execute
/// @param[in] pszParam1 first parameter or NULL for none
/// @param[in] pszParam2 second parameter or NULL for none
/// @return the process instance, or NULL if there was a problem
CProcess *CProcess::Start (const TCHAR *pszExecutable, const TCHAR *pszParam1, const TCHAR *pszParam2) {
	LOGINFO (TEXT ("Running ") << pszExecutable);
#ifdef _WIN32
	STARTUPINFO si;
	ZeroMemory (&si, sizeof (si));
	si.cb = sizeof (si);
	PROCESS_INFORMATION pi;
	DWORD dwFlags = 0;
#ifdef _DEBUG
	dwFlags |= CREATE_NEW_CONSOLE;
#else /* ifdef _DEBUG */
	dwFlags |= CREATE_NO_WINDOW;
#endif /* ifdef _DEBUG */
	// TODO: escape the parameters
	size_t cchArgString = 2;
	if (pszParam1) cchArgString += 1 + _tcslen (pszParam1);
	if (pszParam2) cchArgString += 1 + _tcslen (pszParam2);
	TCHAR *pszArgString = new TCHAR[cchArgString];
	if (!pszArgString) {
		LOGFATAL (TEXT ("Out of memory"));
		assert (0);
		return NULL;
	}
	if (pszParam1) {
		if (pszParam2) {
			StringCchPrintf (pszArgString, cchArgString, TEXT ("0 %s %s"), pszParam1, pszParam2);
		} else {
			StringCchPrintf (pszArgString, cchArgString, TEXT ("0 %s"), pszParam1);
		}
	} else {
		StringCchPrintf (pszArgString, cchArgString, TEXT ("0"));
	}
	BOOL bResult = CreateProcess (pszExecutable, pszArgString, NULL, NULL, FALSE, dwFlags, NULL, NULL, &si, &pi);
	delete pszArgString;
	if (!bResult) {
		LOGWARN (TEXT ("Couldn't start ") << pszExecutable << TEXT (", error ") << GetLastError ());
		return NULL;
	}
	LOGINFO (TEXT ("Created process ") << pi.dwProcessId);
	CloseHandle (pi.hThread);
	return new CProcess (pi.hProcess);
#else /* ifdef _WIN32 */
	pid_t pid;
	TCHAR * const argv[4] = { (TCHAR*)pszExecutable, (TCHAR*)pszParam1, (TCHAR*)pszParam2, NULL };
	if (!PosixLastError (posix_spawn (&pid, pszExecutable, NULL, NULL, argv, environ))) {
		LOGWARN (TEXT ("Couldn't start ") << pszExecutable << TEXT (", error ") << GetLastError ());
		return NULL;
	}
	LOGINFO (TEXT ("Created process ") << pid);
	return new CProcess (pid);
#endif /* ifdef _WIN32 */
}

/// Tests if the process identified by this handle is still executing.
///
/// @return true if still executing, false otherwise
bool CProcess::IsAlive () const {
#ifdef _WIN32
	DWORD dwExitCode;
	if (!GetExitCodeProcess (m_process, &dwExitCode)) {
		LOGWARN (TEXT ("Couldn't query exit code of JVM process, error ") << GetLastError ());
		return false;
	}
	if (dwExitCode == STILL_ACTIVE) {
		return true;
	} else {
		LOGWARN (TEXT ("Process has terminated, exit code ") << dwExitCode);
		return false;
	}
#else /* ifdef _WIN32 */
	return _IsRunning (m_process);
#endif /* ifdef _WIN32 */
}
