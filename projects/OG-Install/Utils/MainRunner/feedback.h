/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_mainrunner_feedback_h
#define __inc_mainrunner_feedback_h

#include "Common/jvm.h"
#include "Common/feedbackwindow.h"

class CFeedback : public CFeedbackWindow {
protected:
	void OnClose ();
	void OnDestroy ();
public:
	CFeedback (HINSTANCE hInstance);
	BOOL Connect (const CJavaVM *poJVM);
	static void Disconnect ();
};

#endif /* ifndef __inc_mainrunner_feedback_h */
