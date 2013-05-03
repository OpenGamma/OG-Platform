/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_mainrunner_runmain_h
#define __inc_mainrunner_runmain_h

#include "Common/jvm.h"

class CMain {
private:
	CMain () { }
	~CMain () { }
public:
	static CConfig s_oConfig;
	static DWORD Run (const CJavaVM *poJVM);
};

#endif /* ifndef __inc_mainrunner_runmain_h */