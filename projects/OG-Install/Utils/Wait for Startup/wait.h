/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_waitforstartup_wait_h
#define __inc_waitforstartup_wait_h

#include "feedback.h"

class CWait {
private:
	CWait () { }
	~CWait () { }
public:
	static CParamString s_oServiceName;
	static CParamString s_oHost;
	static CParamInteger s_oPort;
	static BOOL WaitForStartup (CFeedback *poFeedback);
};

#endif /* ifndef __inc_waitforstartup_wait_h */