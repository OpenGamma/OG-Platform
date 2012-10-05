/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#define _WINSOCKAPI_
#include <Windows.h>
#include <strsafe.h>
#include <ws2tcpip.h>
#include "service.h"

CService::CService (const CParamString *poServiceName, const CParamString *poHost, const CParamString *poPort) {
	m_poServiceName = poServiceName;
	m_poHost = poHost;
	m_poPort = poPort;
	WSADATA wsa;
	m_nWinsock = WSAStartup (0x0002, &wsa);
}

CService::~CService () {
	if (m_nWinsock) WSACleanup ();
}

static PCSTR _scmHost (PCSTR psz) {
	if (!strcmp (psz, "localhost") || !strcmp (psz, "127.0.0.1")) {
		return NULL;
	} else {
		return psz;
	}
}

BOOL CService::Start () {
	SC_HANDLE hSCM = NULL;
	SC_HANDLE hService = NULL;
	BOOL bResult = FALSE;
	do {
		if (!GetServiceName ())  {
			SetLastError (ERROR_BAD_CONFIGURATION);
			break;
		}
		hSCM = OpenSCManager (_scmHost (GetHost ()), NULL, GENERIC_READ);
		if (!hSCM) break;
		hService = OpenService (hSCM, GetServiceName (), SERVICE_START);
		if (!hService) break;
		if (!StartService (hService, 0, NULL)) break;
		bResult = TRUE;
	} while (FALSE);
	int nError = bResult ? 0 : GetLastError ();
	if (hSCM) CloseServiceHandle (hSCM);
	if (hService) CloseServiceHandle (hService);
	if (bResult) {
		return TRUE;
	} else {
		SetLastError (nError);
		return FALSE;
	}
}

BOOL CService::Stop () {
	SC_HANDLE hSCM = NULL;
	SC_HANDLE hService = NULL;
	BOOL bResult = FALSE;
	do {
		if (!GetServiceName ()) break;
		hSCM = OpenSCManager (_scmHost (GetHost ()), NULL, GENERIC_READ);
		if (!hSCM) break;
		hService = OpenService (hSCM, GetServiceName (), SERVICE_STOP);
		if (!hService) break;
		SERVICE_STATUS status;
		if (!ControlService (hService, SERVICE_CONTROL_STOP, &status)) break;
		bResult = TRUE;
	} while (FALSE);
	int nError = bResult ? 0 : GetLastError ();
	if (hSCM) CloseServiceHandle (hSCM);
	if (hService) CloseServiceHandle (hService);
	if (bResult) {
		return TRUE;
	} else {
		SetLastError (nError);
		return FALSE;
	}
}

BOOL CService::IsAutoStart () {
	SC_HANDLE hSCM = NULL;
	SC_HANDLE hService = NULL;
	LPQUERY_SERVICE_CONFIG lpqsc = NULL;
	BOOL bResult = FALSE;
	do {
		if (!GetServiceName ()) break;
		hSCM = OpenSCManager (_scmHost (GetHost ()), NULL, GENERIC_READ);
		if (!hSCM) break;
		hService = OpenService (hSCM, GetServiceName (), SERVICE_QUERY_CONFIG);
		if (!hService) break;
		DWORD dwBytes = 1024;
		do {
			DWORD dwBytesNeeded;
			lpqsc = (LPQUERY_SERVICE_CONFIG)new BYTE[dwBytes];
			if (!lpqsc) break;
			if (!QueryServiceConfig (hService, lpqsc, dwBytes, &dwBytesNeeded)) {
				if (GetLastError () == ERROR_INSUFFICIENT_BUFFER) {
					delete lpqsc;
					lpqsc = NULL;
					dwBytes = dwBytesNeeded;
					continue;
				}
			}
			break;
		} while (TRUE);
		if (!lpqsc) break;
		bResult = (lpqsc->dwStartType == SERVICE_AUTO_START);
	} while (FALSE);
	if (hSCM) CloseServiceHandle (hSCM);
	if (hService) CloseServiceHandle (hService);
	return bResult;
}

BOOL CService::SetAutoStart (BOOL bAutoStart) {
	SC_HANDLE hSCM = NULL;
	SC_HANDLE hService = NULL;
	LPQUERY_SERVICE_CONFIG lpqsc = NULL;
	BOOL bResult = FALSE;
	do {
		if (!GetServiceName ()) break;
		hSCM = OpenSCManager (_scmHost (GetHost ()), NULL, GENERIC_READ);
		if (!hSCM) break;
		hService = OpenService (hSCM, GetServiceName (), SERVICE_QUERY_CONFIG | SERVICE_CHANGE_CONFIG);
		if (!hService) break;
		DWORD dwBytes = 1024;
		do {
			DWORD dwBytesNeeded;
			lpqsc = (LPQUERY_SERVICE_CONFIG)new BYTE[dwBytes];
			if (!lpqsc) break;
			if (!QueryServiceConfig (hService, lpqsc, dwBytes, &dwBytesNeeded)) {
				if (GetLastError () == ERROR_INSUFFICIENT_BUFFER) {
					delete lpqsc;
					lpqsc = NULL;
					dwBytes = dwBytesNeeded;
					continue;
				}
			}
			break;
		} while (TRUE);
		if (!lpqsc) break;
		if (bAutoStart) {
			if (lpqsc->dwStartType == SERVICE_AUTO_START) {
				bResult = TRUE;
				break;
			}
		} else {
			if (lpqsc->dwStartType == SERVICE_DEMAND_START) {
				bResult = TRUE;
				break;
			}
		}
		if (!ChangeServiceConfig (
			hService,
			SERVICE_NO_CHANGE,
			bAutoStart ? SERVICE_AUTO_START : SERVICE_DEMAND_START,
			SERVICE_NO_CHANGE,
			NULL,
			NULL,
			NULL,
			NULL,
			NULL,
			NULL,
			NULL)) break;
		bResult = TRUE;
	} while (FALSE);
	int nError = bResult ? 0 : GetLastError ();
	if (hSCM) CloseServiceHandle (hSCM);
	if (hService) CloseServiceHandle (hService);
	delete lpqsc;
	if (bResult) {
		return TRUE;
	} else {
		SetLastError (nError);
		return FALSE;
	}
}

static BOOL _socketConnect (PCSTR pszHost, PCSTR pszPort) {
	ADDRINFO *aiService = NULL, aiHint;
	BOOL bResult = FALSE;
	SOCKET sock = INVALID_SOCKET;
	int i;
	do {
		ZeroMemory (&aiHint, sizeof (aiHint));
		aiHint.ai_family = AF_INET;
		aiHint.ai_socktype = SOCK_STREAM;
		aiHint.ai_protocol = IPPROTO_TCP;
		if (GetAddrInfo (pszHost, pszPort, &aiHint, &aiService) != 0) break;
		sock = socket (aiService->ai_family, aiService->ai_socktype, aiService->ai_protocol);
		if (sock == INVALID_SOCKET) break;
		i = 3000;
		setsockopt (sock, SOL_SOCKET, SO_RCVTIMEO, (char*)&i, sizeof (i));
		setsockopt (sock, SOL_SOCKET, SO_SNDTIMEO, (char*)&i, sizeof (i));
		if (connect (sock, aiService->ai_addr, (int)aiService->ai_addrlen) != 0) break;
		bResult = TRUE;
	} while (FALSE);
	if (aiService) FreeAddrInfo (aiService);
	if (sock) closesocket (sock);
	return bResult;
}

#define STATUS_INITIAL			1
#define STATUS_SERVICE_BUSY		2
#define STATUS_CONNECTING		3
#define STATUS_CONNECTING_2		4
#define STATUS_CONNECTING_3		5
#define STATUS_CONNECTING_4		6
#define STATUS_STARTED			7

int CService::GetStatus () {
	SC_HANDLE hSCM = NULL;
	SC_HANDLE hService = NULL;
	SERVICE_STATUS ss;
	int nResult;
	do {
		if (!GetServiceName () || !GetHost () || !GetPort ()) {
			nResult = SERVICE_STATUS_BAD_CONFIG;
			break;
		}
		if (m_nWinsock) {
			nResult = SERVICE_STATUS_BAD_WINSOCK;
			SetLastError (m_nWinsock);
			break;
		}
		hSCM = OpenSCManager (_scmHost (GetHost ()), NULL, GENERIC_READ);
		if (!hSCM) {
			nResult = SERVICE_STATUS_BAD_SCM;
			break;
		}
		hService = OpenService (hSCM, GetServiceName (), SERVICE_INTERROGATE);
		if (!hService) {
			DWORD dwError = GetLastError ();
			switch (dwError) {
			case ERROR_SERVICE_DOES_NOT_EXIST :
				nResult = SERVICE_STATUS_NOT_INSTALLED;
				break;
			default :
				nResult = SERVICE_STATUS_CONNECTOR_ERROR;
				break;
			}
			break;
		}
		if (!ControlService (hService, SERVICE_CONTROL_INTERROGATE, &ss)) {
			DWORD dwError = GetLastError ();
			switch (dwError) {
			case ERROR_SERVICE_NEVER_STARTED :
			case ERROR_SERVICE_NOT_ACTIVE :
				nResult = SERVICE_STATUS_STOPPED;
				break;
			case ERROR_SERVICE_CANNOT_ACCEPT_CTRL :
				nResult = SERVICE_STATUS_BUSY;
				break;
			default :
				nResult = SERVICE_STATUS_QUERY_ERROR;
				break;
			}
			break;
		}
		if (ss.dwCurrentState != SERVICE_RUNNING) {
			nResult = SERVICE_STATUS_BUSY;
			break;
		}
		nResult = _socketConnect (GetHost (), GetPort ()) ? SERVICE_STATUS_OK : SERVICE_STATUS_STARTING;
	} while (FALSE);
	if (hSCM) CloseServiceHandle (hSCM);
	if (hService) CloseServiceHandle (hService);
	return nResult;
}
