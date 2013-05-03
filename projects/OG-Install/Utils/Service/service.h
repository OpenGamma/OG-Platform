/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_service_service_h
#define __inc_service_service_h

#include "Common/jvm.h"

class CService {
private:
	CService () { }
	~CService () { }
public:
	static CConfig s_oConfig;
	static DWORD RegisterShutdownHook (const CJavaVM *poJVM, void *callback);
	static DWORD Run (const CJavaVM *poJVM);
	static DWORD Stop (const CJavaVM *poJVM);
};

#endif /* ifndef __inc_service_service_h */