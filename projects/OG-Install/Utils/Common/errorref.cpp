/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <Windows.h>
#include "errorref.h"

/// Reports error information to the Windows event log.
void ReportErrorReference (DWORD dwError) {
	HANDLE hLog = RegisterEventSource (NULL, "OpenGamma");
	if (hLog) {
		ReportEvent (hLog, EVENTLOG_ERROR_TYPE, 1, 0xE0000000 | dwError, NULL, 0, 0, NULL, NULL);
		DeregisterEventSource (hLog);
	}
}
