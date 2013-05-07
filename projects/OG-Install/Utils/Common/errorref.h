/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_common_errorref_h
#define __inc_common_errorref_h

// Error references are DWORD values. Each file in the solution has a unique offset; the error to
// be raised is the line number plus that offset. This at least gives the location of the error
// to the developers even though the number is quite meaningless to the end user.

#define ERROR_REF(x)	(__LINE__ + x)

#define ERROR_REF_MAIN		ERROR_REF(0x10000)
#define ERROR_REF_JVM		ERROR_REF(0x20000)
#define ERROR_REF_RUNMAIN	ERROR_REF(0x30000)
#define ERROR_REF_SERVICE	ERROR_REF(0x40000)

void ReportErrorReference (DWORD dwError);

#endif /* ifndef __inc_common_errorref_h */