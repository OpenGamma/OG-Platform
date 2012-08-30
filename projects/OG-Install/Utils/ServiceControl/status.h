/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_servicecontrol_status_h
#define __inc_servicecontrol_status_h

class CStatus {
private:
	CStatus () { }
	~CStatus () { }
public:
	static BOOL Register (HINSTANCE hInstance);
	static int GetHeight ();
	static PCSTR GetClass ();
};

#endif /* ifndef __inc_servicecontrol_status_h */