/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Service/Settings.cpp

#include "Service/Settings.h"

LOGGING (com.opengamma.language.service.SettingsTest);

#define PRINT(_key_) \
	LOGINFO (TEXT (#_key_) TEXT (" = ") << settings.Get##_key_ ());

#define PRINT_AND_ASSERT(_key_) \
	PRINT (_key_); \
	ASSERT (settings.Get##_key_());

static void Defaults () {
	CSettings settings;
	PRINT_AND_ASSERT (AnnotationCache);
	PRINT_AND_ASSERT (BusyTimeout);
	PRINT_AND_ASSERT (ConnectionPipe);
	PRINT_AND_ASSERT (ConnectionTimeout);
	PRINT_AND_ASSERT (IdleTimeout);
	PRINT_AND_ASSERT (JarPath);
	PRINT_AND_ASSERT (JvmLibrary);
	PRINT (LogConfiguration);
	PRINT_AND_ASSERT (ServiceName);
#ifdef _WIN32
	PRINT (ServiceSDDL);
#endif /* ifdef _WIN32 */
}

BEGIN_TESTS (ServiceSettingsTest)
	TEST (Defaults);
END_TESTS