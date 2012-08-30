/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <Windows.h>
#include <strsafe.h>
#include "wait.h"
#include "Common/service.h"

CParamString CWait::s_oServiceName ("s", NULL, TRUE);
CParamString CWait::s_oHost ("h", "localhost", FALSE);
CParamInteger CWait::s_oPort ("p", 8080, FALSE);

BOOL CWait::WaitForStartup (CFeedback *poFeedback) {
	CService oService (&s_oServiceName, &s_oHost, &s_oPort);
	char szStatus[MAX_PATH] = "";
	if (poFeedback) {
		poFeedback->SetStatusText ("Waiting for the OpenGamma engine to start");
	}
	int nClock = 0;
	int nStatus;
	do {
		nStatus = oService.GetStatus ();
		switch (nStatus) {
		case SERVICE_STATUS_BAD_CONFIG :
			StringCbPrintf (szStatus, sizeof (szStatus), "Configuration error");
			break;
		case SERVICE_STATUS_BAD_WINSOCK :
			StringCbPrintf (szStatus, sizeof (szStatus), "%s, error %d", "Couldn't start Winsock", GetLastError ());
			break;
		case SERVICE_STATUS_BAD_SCM :
			StringCbPrintf (szStatus, sizeof (szStatus), "%s, error %d", "Couldn't connect to SCM", GetLastError ());
			break;
		case SERVICE_STATUS_NOT_INSTALLED :
			StringCbPrintf (szStatus, sizeof (szStatus), "The %s service was not installed", s_oServiceName.GetString ());
			break;
		case SERVICE_STATUS_CONNECTOR_ERROR :
			StringCbPrintf (szStatus, sizeof (szStatus), "%s, error %d", "Couldn't open service", GetLastError ());
			break;
		case SERVICE_STATUS_STOPPED :
			StringCbPrintf (szStatus, sizeof (szStatus), "A problem occurred during the installation of the %s service and it was not started", s_oServiceName.GetString ());
			break;
		case SERVICE_STATUS_BUSY :
			if (poFeedback) {
				poFeedback->SetStatusText ("Waiting for the OpenGamma engine to start. The service is currently busy and not responding to requests");
			}
			break;
		case SERVICE_STATUS_QUERY_ERROR :
			StringCbPrintf (szStatus, sizeof (szStatus), "%s, error %d", "Couldn't query service status", GetLastError ());
			break;
		case SERVICE_STATUS_OK :
			if (poFeedback) {
				poFeedback->SetStatusText ("The OpenGamma engine service has started and is accepting connections");
			}
			break;
		case SERVICE_STATUS_STARTING :
			if (poFeedback) {
				switch ((nClock++) & 3) {
				case 0 :
					poFeedback->SetStatusText ("Connecting to the OpenGamma engine");
					break;
				case 1 :
					poFeedback->SetStatusText ("Connecting to the OpenGamma engine .");
					break;
				case 2 :
					poFeedback->SetStatusText ("Connecting to the OpenGamma engine ..");
					break;
				case 3 :
					poFeedback->SetStatusText ("Connecting to the OpenGamma engine ...");
					break;
				}
			}
			break;
		}
		if (nStatus == SERVICE_STATUS_OK) {
			break;
		} else if (!*szStatus) {
			Sleep (1000);
			continue;
		} else {
			break;
		}
	} while (TRUE);
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
