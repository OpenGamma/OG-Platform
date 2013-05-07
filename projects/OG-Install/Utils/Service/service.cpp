/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <Windows.h>
#include "service.h"
#include "Common/errorref.h"

#define EXITHOOK_CLASS_NAME	"com/opengamma/install/service/ExitHook"

static CConfigMultiString g_oArgumentStrings ("count", "arg%d");
static CConfigEntry *g_apoArgumentSection[1] = { &g_oArgumentStrings };
static CConfigSection g_oArgumentSection ("Arguments", 1, g_apoArgumentSection);
static CConfigString g_oInvokeClass ("class", NULL);
static CConfigString g_oInvokeStart ("main", "main");
static CConfigString g_oInvokeStop ("stop", "stop");
static CConfigEntry *g_apoInvokeSection[3] = { &g_oInvokeClass, &g_oInvokeStart, &g_oInvokeStop };
static CConfigSection g_oInvokeSection ("Invoke", sizeof (g_apoInvokeSection) / sizeof (*g_apoInvokeSection), g_apoInvokeSection);
static CConfigString g_oExitHookClass ("class", EXITHOOK_CLASS_NAME);
static CConfigString g_oExitHookRegister ("register", "register");
static CConfigEntry *g_apoExitHookSection[2] = { &g_oExitHookClass, &g_oExitHookRegister };
static CConfigSection g_oExitHookSection ("ExitHook", sizeof (g_apoExitHookSection) / sizeof (*g_apoExitHookSection), g_apoExitHookSection);
static CConfigSection *g_apoConfig[3] = { &g_oArgumentSection, &g_oInvokeSection, &g_oExitHookSection };
CConfig CService::s_oConfig (sizeof (g_apoConfig) / sizeof (*g_apoConfig), g_apoConfig);

DWORD CService::RegisterShutdownHook (const CJavaVM *poJVM, void *pfn) {
	// Register the native for the default hook
	JNINativeMethod aMethods[1] = {
		{ (char*)"run", (char*)"()V", pfn }
	};
	DWORD dwError = poJVM->RegisterNatives (EXITHOOK_CLASS_NAME, 1, aMethods);
	if (dwError) return dwError;
	// Then call the config specified (or default) registration for the hook
	return poJVM->Invoke (&g_oExitHookClass, &g_oExitHookRegister);
}

DWORD CService::Run (const CJavaVM *poJVM) {
	if (!poJVM) {
		return ERROR_REF_SERVICE;
	}
	return poJVM->Invoke (&g_oInvokeClass, &g_oInvokeStart, &g_oArgumentStrings);
}

DWORD CService::Stop (const CJavaVM *poJVM) {
	if (!poJVM) {
		return ERROR_REF_SERVICE;
	}
	return poJVM->Invoke (&g_oInvokeClass, &g_oInvokeStop);
}