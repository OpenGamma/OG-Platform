/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_servicecontrol_startstop_h
#define __inc_servicecontrol_startstop_h

#define STARTSTOP_START	"Start service"
#define STARTSTOP_STOP	"Stop service"

class CStartStop {
private:
	CStartStop () { }
	~CStartStop () { }
public:
	static BOOL Register (HINSTANCE hInstance);
	static int GetHeight ();
	static PCSTR GetClass ();
};

#endif /* ifndef __inc_servicecontrol_startstop_h */