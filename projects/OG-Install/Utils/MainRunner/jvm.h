/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_jvm_h
#define __inc_jvm_h

#include <jni.h>

BOOL ReadConfigurationFile (const char *pszFilename);
BOOL FindJava ();
BOOL CreateJavaVM ();
BOOL InvokeMain (const char *pszClass);
BOOL InvokeStop (const char *pszClass);

#endif /* ifndef __inc_jvm_h */