/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// JVM process/service management

#include "JVM.h"
#include "Settings.h"
#ifdef _WIN32
#include <Util/DllVersion.h>
#endif
#include <Util/Error.h>

LOGGING (com.opengamma.language.connector.JVM);

#ifdef _WIN32
CClientJVM::CClientJVM (SC_HANDLE hService, bool bFirstConnection) {
	if (bFirstConnection) {
		LOGINFO (TEXT ("Connection to fresh client JVM service"));
	} else {
		LOGINFO (TEXT ("Connection to running client JVM service"));
	}
	m_bFirstConnection = bFirstConnection;
	m_poProcess = NULL;
	m_hService = hService;
}
#endif /* ifdef _WIN32 */

CClientJVM::CClientJVM (CProcess *poProcess, bool bFirstConnection) {
	if (bFirstConnection) {
		LOGINFO (TEXT ("Connection to new client JVM process"));
	} else {
		LOGINFO (TEXT ("Connection to established client JVM process"));
	}
	m_bFirstConnection = bFirstConnection;
	m_poProcess = poProcess;
#ifdef _WIN32
	m_hService = NULL;
#endif /* ifdef _WIN32 */
}

CClientJVM::~CClientJVM () {
	if (m_poProcess) {
		delete m_poProcess;
	}
#ifdef _WIN32
	if (m_hService) {
		CloseServiceHandle (m_hService);
	}
#endif /* ifdef _WIN32 */
}

CClientJVM *CClientJVM::StartExecutable (const TCHAR *pszExecutable, unsigned long lStartTimeout) {
	LOGINFO (TEXT ("Starting executable ") << pszExecutable);
	CProcess *poProcess = CProcess::FindByName (pszExecutable);
	if (poProcess) {
		LOGINFO (TEXT ("JVM executable already running, pid ") << poProcess->GetProcessId ());
		return new CClientJVM (poProcess, false);
	}
	poProcess = CProcess::Start (pszExecutable, TEXT ("run"));
	if (!poProcess) {
		LOGWARN (TEXT ("Couldn't start JVM executable, error ") << GetLastError ());
		return NULL;
	}
	return new CClientJVM (poProcess, true);
}

static bool _KillJVMProcess (CProcess *poProcess, unsigned long lTimeout) {
	LOGINFO (TEXT ("Killing JVM service process ") << poProcess->GetProcessId ());
	bool bResult = true;
	int nProcessId = poProcess->GetProcessId ();
	if (!poProcess->Terminate ()) {
		LOGWARN (TEXT ("Couldn't terminate JVM process, pid ") << nProcessId << TEXT (", error ") << GetLastError ());
		bResult = false;
	} else {
		LOGINFO (TEXT ("Terminated JVM process, pid ") << nProcessId);
		if (!poProcess->Wait (lTimeout)) {
			LOGWARN (TEXT ("JVM process did not terminate within ") << lTimeout << TEXT ("ms"));
		}
	}
	return bResult;
}

#ifdef _WIN32
static bool _KillHungJVMService (SC_HANDLE hService, PCTSTR pszExecutable, DWORD dwPollTimeout, DWORD dwStartTimeout, DWORD dwStopTimeout, LPSERVICE_STATUS pss) {
	DWORD dwStart = GetTickCount ();
	DWORD dwServiceTimeout = (dwStartTimeout > dwStopTimeout) ? dwStartTimeout : dwStopTimeout;
	do {
		LOGDEBUG (TEXT ("Waiting for JVM service to respond, status = ") << pss->dwCurrentState);
		Sleep (dwPollTimeout);
		if (!ControlService (hService, SERVICE_CONTROL_INTERROGATE, pss)) {
			DWORD dwError = GetLastError ();
			if (dwError == ERROR_SERVICE_NOT_ACTIVE) {
				LOGDEBUG (TEXT ("Service is not running"));
				return true;
			} else if (dwError = ERROR_SERVICE_CANNOT_ACCEPT_CTRL) {
				LOGWARN (TEXT ("Service cannot accept control signal"));
				break;
			} else {
				LOGWARN (TEXT ("Couldn't query service state, error ") << GetLastError ());
				return false;
			}
		}
		switch (pss->dwCurrentState) {
			case SERVICE_STOPPED :
				LOGDEBUG (TEXT ("Service has stopped"));
				return true;
			case SERVICE_RUNNING :
				LOGDEBUG (TEXT ("Service is running"));
				return true;
			case SERVICE_START_PENDING :
				dwServiceTimeout = dwStartTimeout;
				break;
			case SERVICE_STOP_PENDING :
				dwServiceTimeout = dwStopTimeout;
				break;
		}
	} while (GetTickCount () - dwStart < dwServiceTimeout);
	LOGINFO (TEXT ("Killing hung JVM process"));
	bool bResult = true;
	CProcess *poProcess = CProcess::FindByName (pszExecutable);
	if (poProcess) {
		bResult = _KillJVMProcess (poProcess, dwStopTimeout);
		delete poProcess;
	} else {
		LOGWARN (TEXT ("JVM executable not running"));
	}
	return bResult;
}
#endif /* ifdef _WIN32 */

CClientJVM *CClientJVM::StartService (const TCHAR *pszServiceName, const TCHAR *pszExecutable, unsigned long lPollTimeout, unsigned long lStartTimeout, unsigned long lStopTimeout) {
	LOGINFO (TEXT ("Starting service ") << pszServiceName);
#ifdef _WIN32
	SC_HANDLE hSCM = OpenSCManager (NULL, NULL, GENERIC_READ);
	if (!hSCM) {
		LOGWARN (TEXT ("Couldn't connect to service manager, error ") << GetLastError ());
		return NULL;
	}
	SC_HANDLE hService = OpenService (hSCM, pszServiceName, SERVICE_INTERROGATE | SERVICE_START);
	if (!hService) {
		DWORD dwError = GetLastError ();
		if (dwError == ERROR_SERVICE_DOES_NOT_EXIST) {
			LOGWARN (TEXT ("Service ") << pszServiceName << TEXT (" does not exist"));
			CloseServiceHandle (hSCM);
			return StartExecutable (pszExecutable, lStartTimeout);
		} else if (dwError == ERROR_ACCESS_DENIED) {
			LOGDEBUG (TEXT ("Access denied to service, trying read only"));
			hService = OpenService (hSCM, pszServiceName, GENERIC_READ);
			if (!hService) {
				dwError = GetLastError ();
				CloseServiceHandle (hSCM);
				LOGWARN (TEXT ("Couldn't open service ") << pszServiceName << TEXT (", error ") << dwError);
				return NULL;
			}
		} else {
			CloseServiceHandle (hSCM);
			LOGWARN (TEXT ("Couldn't open service ") << pszServiceName << TEXT (", error ") << dwError);
			return NULL;
		}
	}
	CloseServiceHandle (hSCM);
	SERVICE_STATUS ss;
	if (!ControlService (hService, SERVICE_CONTROL_INTERROGATE, &ss)) {
		DWORD dwError = GetLastError ();
		if (dwError = ERROR_SERVICE_NOT_ACTIVE) {
			ss.dwCurrentState = SERVICE_STOPPED;
		} else {
			LOGWARN (TEXT ("Couldn't query status of ") << pszServiceName << TEXT (", error ") << dwError);
			CloseServiceHandle (hService);
			return NULL;
		}
	}
	if ((ss.dwCurrentState != SERVICE_STOPPED) && (ss.dwCurrentState != SERVICE_RUNNING)) {
		if (!_KillHungJVMService (hService, pszExecutable, lPollTimeout, lStartTimeout, lStopTimeout, &ss)) {
			LOGWARN (TEXT ("Couldn't kill hung JVM service"));
			CloseServiceHandle (hService);
			return NULL;
		}
	}
	bool bFirstConnection = (ss.dwCurrentState != SERVICE_RUNNING);
	if (bFirstConnection) {
		LOGINFO (TEXT ("Starting ") << pszServiceName);
retryStart:
		if (!::StartService (hService, 0, NULL)) {
			DWORD dwError = GetLastError ();
			if (dwError == ERROR_SERVICE_ALREADY_RUNNING) {
				if (ControlService (hService, SERVICE_CONTROL_INTERROGATE, &ss)) {
					if (ss.dwCurrentState != SERVICE_RUNNING) {
						Sleep (lPollTimeout);
						LOGDEBUG (TEXT ("Retrying start"));
						goto retryStart;
					}
				} else {
					LOGWARN (TEXT ("Couldn't start ") << pszServiceName << TEXT (" and couldn't query state, error ") << GetLastError ());
					CloseServiceHandle (hService);
					return NULL;
				}
			} else {
				LOGWARN (TEXT ("Couldn't start ") << pszServiceName << TEXT (", error ") << dwError);
				CloseServiceHandle (hService);
				return NULL;
			}
		}
		if (!_KillHungJVMService (hService, pszExecutable, lPollTimeout, lStartTimeout, lStopTimeout, &ss)) {
			LOGWARN (TEXT ("Couldn't kill hung JVM service"));
			CloseServiceHandle (hService);
			return NULL;
		}
	}
	return new CClientJVM (hService, bFirstConnection);
#else /* ifdef _WIN32 */
	// TODO: SysV service management (File a Jira for this)
	return StartExecutable (pszExecutable, lStartTimeout);
#endif /* ifdef _WIN32 */
}

CClientJVM *CClientJVM::Start () {
	LOGDEBUG (TEXT ("Starting JVM service"));
	CSettings oSettings;
	const TCHAR *pszServiceName = oSettings.GetServiceName ();
	if (pszServiceName && pszServiceName[0]) {
		return StartService (pszServiceName, oSettings.GetServiceExecutable (), oSettings.GetServicePoll (), oSettings.GetStartTimeout (), oSettings.GetStopTimeout ());
	} else {
		return StartExecutable (oSettings.GetServiceExecutable (), oSettings.GetStartTimeout ());
	}
}

bool CClientJVM::Stop () {
	LOGDEBUG (TEXT ("Stopping JVM service"));
	bool bResult;
	CSettings oSettings;
#ifdef _WIN32
	if (m_hService) {
		SERVICE_STATUS ss;
		if (ControlService (m_hService, SERVICE_CONTROL_STOP, &ss)) {
			LOGDEBUG (TEXT ("Service ") << oSettings.GetServiceName () << TEXT (" received STOP signal"));
			bResult = true;
		} else {
			LOGWARN (TEXT ("Couldn't send STOP signal to service ") << oSettings.GetServiceName () << TEXT (", error ") << GetLastError ());
			bResult = false;
		}
		bResult &= _KillHungJVMService (m_hService, oSettings.GetServiceExecutable (), oSettings.GetServicePoll (), oSettings.GetStartTimeout (), oSettings.GetStopTimeout (), &ss);
		CloseServiceHandle (m_hService);
		m_hService = NULL;
	} else
#else /* ifdef _WIN32 */
	// TODO: SysV service management (File a Jira for this)
#endif /* ifdef _WIN32 */
	if (m_poProcess) {
		bResult = _KillJVMProcess (m_poProcess, oSettings.GetStopTimeout ());
		delete m_poProcess;
		m_poProcess = NULL;
	} else {
		LOGFATAL (TEXT ("Service is not running"));
		assert (0);
		bResult = false;
	}
	return bResult;
}

bool CClientJVM::FirstConnection () {
	bool bResult = m_bFirstConnection;
	m_bFirstConnection = false;
	return bResult;
}

bool CClientJVM::IsAlive () const {
#ifdef _WIN32
	if (m_hService) {
		SERVICE_STATUS ss;
		if (!ControlService (m_hService, SERVICE_CONTROL_INTERROGATE, &ss)) {
			LOGWARN (TEXT ("Couldn't query status of JVM service, error ") << GetLastError ());
			return false;
		}
		if (ss.dwCurrentState == SERVICE_RUNNING) {
			return true;
		} else {
			LOGWARN (TEXT ("Service is not running"));
			return false;
		}
	} else
#else /* ifdef _WIN32 */
	// TODO: SysV service management (File a Jira for this)
#endif /* ifdef _WIN32 */
	if (m_poProcess) {
		return m_poProcess->IsAlive ();
	} else {
		LOGFATAL (TEXT ("Service is not running"));
		assert (0);
		return false;
	}
}
