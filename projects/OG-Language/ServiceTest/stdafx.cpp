/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Service/Service.h"

#ifndef __cplusplus_cli
int main (int argc, char **argv) {
	if ((argc == 3) && !strcmp (argv[1], "jvm")) {
		return ServiceTestJVM (argv[2]) ? 0 : 1;
	}
	CAbstractTest::Main (argc, argv);
	return 0;
}
#endif /* ifndef __cplusplus_cli */
