/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_waitforstartup_feedback_h
#define __inc_waitforstartup_feedback_h

#include "Common/feedbackwindow.h"
#include "Common/param.h"

class CFeedback : public CFeedbackWindow {
protected:
	void OnClose ();
	void OnDestroy ();
public:
	static CParamString s_oTitle;
	CFeedback (HINSTANCE hInstance);
};

#endif /* ifndef __inc_waitforstartup_feedback_h */