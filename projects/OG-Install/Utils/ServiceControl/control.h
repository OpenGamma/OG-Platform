/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_servicecontrol_control_h
#define __inc_servicecontrol_control_h

#include "Common/param.h"

class CControl {
private:
	CControl () { }
	~CControl () { }
public:
	static CParamString s_oServiceName;
	static CParamString s_oHost;
	static CParamString s_oPort;
	static CParamString s_oElevate;
	static BOOL Register (HINSTANCE hInstance, int nIcon);
	static BOOL Create (HINSTANCE hInstance, int nCmdShow);
	static void DispatchMessages ();
};

#endif /* ifndef __inc_servicecontrol_control_h */