/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <Windows.h>
#include "feedback.h"

#define CLASS_NAME "com/opengamma/install/feedback/Feedback"

static CRITICAL_SECTION g_cs;
static CFeedback * volatile g_poConnected = NULL;

static void JNICALL _status (JNIEnv *pEnv, jclass cls, jstring jstrMessage) {
	EnterCriticalSection (&g_cs);
	CFeedback *poInstance = g_poConnected;
	if (poInstance) poInstance->AddRef ();
	LeaveCriticalSection (&g_cs);
	if (poInstance) {
		const char *pszMessage = pEnv->GetStringUTFChars (jstrMessage, NULL);
		if (pszMessage) {
			poInstance->SetStatusText (pszMessage);
			pEnv->ReleaseStringUTFChars (jstrMessage, pszMessage);
		}
	}
}

void CFeedback::OnClose () {
	if (Alert ("The current task must complete before installation can continue. Do you want to allow it to finish?", "Cancel installation", MB_YESNO | MB_ICONQUESTION) == IDNO) {
		PostQuitMessage (1);
	}
}

void CFeedback::OnDestroy () {
	PostQuitMessage (0);
}

CFeedback::CFeedback (HINSTANCE hInstance)
: CFeedbackWindow (HWND_DESKTOP, hInstance, "OpenGamma Installation") {
}

BOOL CFeedback::Connect (const CJavaVM *poJVM) {
	if (g_poConnected) return FALSE;
	InitializeCriticalSection (&g_cs);
	AddRef ();
	g_poConnected = this;
	JNINativeMethod aMethods[1] = {
		{ (char*)"status", (char*)"(Ljava/lang/String;)V", (void*)&_status }
	};
	poJVM->RegisterNatives (CLASS_NAME, 1, aMethods);
	return TRUE;
}

void CFeedback::Disconnect () {
	if (!g_poConnected) return;
	EnterCriticalSection (&g_cs);
	CFeedback *poInstance = g_poConnected;
	g_poConnected = NULL;
	LeaveCriticalSection (&g_cs);
	CFeedback::Release (poInstance);
}
