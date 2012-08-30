/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <Windows.h>
#include "feedback.h"

CParamString CFeedback::s_oTitle ("t", "OpenGamma", FALSE);

void CFeedback::OnClose () {
	if (Alert ("The OpenGamma service has not finished starting yet. Do you want to wait for it?", "OpenGamma Installation", MB_YESNO | MB_ICONQUESTION) == IDNO) {
		PostQuitMessage (1);
	}
}

void CFeedback::OnDestroy () {
	PostQuitMessage (0);
}

CFeedback::CFeedback (HINSTANCE hInstance)
: CFeedbackWindow (HWND_DESKTOP, hInstance, s_oTitle.GetString ()) {
}