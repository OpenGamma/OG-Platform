/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <Windows.h>
#include "runmain.h"

static CConfigMultiString g_oArgumentStrings ("count", "arg%d");
static CConfigEntry *g_apoArgumentSection[1] = { &g_oArgumentStrings };
static CConfigSection g_oArgumentSection ("Arguments", 1, g_apoArgumentSection);
static CConfigString g_oInvokeClass ("class", "com/opengamma/install/launch/MainRunner");
static CConfigString g_oInvokeMain ("main", "main");
static CConfigEntry *g_apoInvokeSection[2] = { &g_oInvokeClass, &g_oInvokeMain };
static CConfigSection g_oInvokeSection ("Invoke", 2, g_apoInvokeSection);
static CConfigSection *g_apoConfig[2] = { &g_oArgumentSection, &g_oInvokeSection };
CConfig CMain::s_oConfig (2, g_apoConfig);

BOOL CMain::Run (const CJavaVM *poJVM) {
	if (!poJVM) {
		return FALSE;
	}
	return poJVM->Invoke (&g_oInvokeClass, &g_oInvokeMain, &g_oArgumentStrings);
}
