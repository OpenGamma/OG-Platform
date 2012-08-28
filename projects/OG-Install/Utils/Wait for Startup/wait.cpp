/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#define _WINSOCKAPI_
#include <Windows.h>
#include <strsafe.h>
#include <ws2tcpip.h>
#include "wait.h"

CParamString CWait::s_oServiceName ("s", NULL, TRUE);
CParamString CWait::s_oHost ("h", "localhost", FALSE);
CParamInteger CWait::s_oPort ("p", 8080, FALSE);

static BOOL _socketConnect () {
	ADDRINFO *aiService = NULL, aiHint;
	BOOL bResult = FALSE;
	SOCKET sock = INVALID_SOCKET;
	int i;
	do {
		ZeroMemory (&aiHint, sizeof (aiHint));
		aiHint.ai_family = AF_INET;
		aiHint.ai_socktype = SOCK_STREAM;
		aiHint.ai_protocol = IPPROTO_TCP;
		if (GetAddrInfo (CWait::s_oHost.GetString (), CWait::s_oPort.GetString (), &aiHint, &aiService) != 0) break;
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

static int _startWinsock () {
	WSADATA wsa;
	return WSAStartup (0x0002, &wsa);
}

#define STATUS_INITIAL			1
#define STATUS_SERVICE_BUSY		2
#define STATUS_CONNECTING		3
#define STATUS_CONNECTING_2		4
#define STATUS_CONNECTING_3		5
#define STATUS_CONNECTING_4		6
#define STATUS_STARTED			7

static void _setStatus (CFeedback *poFeedback, int nStatus) {
	if (!poFeedback) return;
	switch (nStatus) {
	case STATUS_INITIAL :
		poFeedback->SetStatusText ("Waiting for the OpenGamma engine to start");
		break;
	case STATUS_SERVICE_BUSY :
		poFeedback->SetStatusText ("Waiting for the OpenGamma engine to start. The service is currently busy and not responding to requests");
		break;
	case STATUS_CONNECTING :
		poFeedback->SetStatusText ("Connecting to the OpenGamma engine");
		break;
	case STATUS_CONNECTING_2 :
		poFeedback->SetStatusText ("Connecting to the OpenGamma engine .");
		break;
	case STATUS_CONNECTING_3 :
		poFeedback->SetStatusText ("Connecting to the OpenGamma engine ..");
		break;
	case STATUS_CONNECTING_4 :
		poFeedback->SetStatusText ("Connecting to the OpenGamma engine ...");
		break;
	case STATUS_STARTED :
		poFeedback->SetStatusText ("The OpenGamma engine service has started and is accepting connections");
		break;
	}
}

BOOL CWait::WaitForStartup (CFeedback *poFeedback) {
	char szStatus[MAX_PATH] = "";
	SC_HANDLE hSCM = NULL;
	SC_HANDLE hService = NULL;
	int nWinsock = -1;
	SERVICE_STATUS ss;
	_setStatus (poFeedback, STATUS_INITIAL);
	do {
		if (!s_oServiceName.GetString ()) break;
		nWinsock = _startWinsock ();
		if (nWinsock) {
			StringCbPrintf (szStatus, sizeof (szStatus), "Internal error loading library, error %d", nWinsock);
			break;
		}
		hSCM = OpenSCManager (NULL, NULL, GENERIC_READ);
		if (!hSCM) {
			StringCbPrintf (szStatus, sizeof (szStatus), "Couldn't open service manager, error %d", GetLastError ());
			break;
		}
		hService = OpenService (hSCM, s_oServiceName.GetString (), SERVICE_INTERROGATE);
		if (!hService) {
			DWORD dwError = GetLastError ();
			switch (dwError) {
			case ERROR_SERVICE_DOES_NOT_EXIST :
				StringCbPrintf (szStatus, sizeof (szStatus), "The %s service was not installed", s_oServiceName.GetString ());
				break;
			default :
				StringCbPrintf (szStatus, sizeof (szStatus), "Couldn't connect to %s, error %d", s_oServiceName.GetString (), dwError);
				break;
			}
			break;
		}
		int nClock = 0;
		do {
			Sleep (1000);
			if (!ControlService (hService, SERVICE_CONTROL_INTERROGATE, &ss)) {
				DWORD dwError = GetLastError ();
				switch (dwError) {
				case ERROR_SERVICE_NEVER_STARTED :
				case ERROR_SERVICE_NOT_ACTIVE :
					StringCbPrintf (szStatus, sizeof (szStatus), "A problem occurred during the installation of the %s service and it was not started", s_oServiceName.GetString ());
					break;
				case ERROR_SERVICE_CANNOT_ACCEPT_CTRL :
					_setStatus (poFeedback, STATUS_SERVICE_BUSY);
					break;
				default :
					StringCbPrintf (szStatus, sizeof (szStatus), "Couldn't query %s, error %d", s_oServiceName.GetString (), GetLastError ());
					break;
				}
				continue;
			}
			if (ss.dwCurrentState != SERVICE_RUNNING) {
				StringCbPrintf (szStatus, sizeof (szStatus), "The %s service could not be started", s_oServiceName.GetString ());
				break;
			}
			_setStatus (poFeedback, STATUS_CONNECTING + (nClock++ & 3));
			if (_socketConnect ()) {
				_setStatus (poFeedback, STATUS_STARTED);
				break;
			}
		} while (!*szStatus);
	} while (FALSE);
	if (nWinsock == 0) WSACleanup ();
	if (hSCM) CloseServiceHandle (hSCM);
	if (hService) CloseServiceHandle (hService);
	if (*szStatus) {
		if (poFeedback) {
			poFeedback->BringToTop ();
			poFeedback->Alert (szStatus, "OpenGamma Installation", MB_OK | MB_ICONSTOP);
		}
		return FALSE;
	} else {
		return TRUE;
	}
}
