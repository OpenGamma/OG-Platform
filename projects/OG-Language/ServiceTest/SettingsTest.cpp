/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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

static void InternalDefaults () {
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

#undef PRINT
#define PRINT(_key_) \
	LOGINFO (TEXT (#_key_) TEXT (" = ") << _key_ ());

#undef PRINT_AND_ASSERT
#define PRINT_AND_ASSERT(_key_) \
	PRINT (_key_); \
	ASSERT (_key_ ());

static void PublicDefaults () {
	PRINT_AND_ASSERT (ServiceDefaultConnectionPipe);
	PRINT_AND_ASSERT (ServiceDefaultServiceName);
}

static void PublicCommandHelpers () {
#ifndef _WIN32
	TCHAR *psz = ServiceCreateStartCmd ("og-language");
	if (psz) {
		LOGINFO (TEXT ("startCmd = ") << psz);
		free (psz);
	} else {
		LOGINFO (TEXT ("no default start command"));
	}
	psz = ServiceCreateStopCmd ("og-language");
	if (psz) {
		LOGINFO (TEXT ("stopCmd = ") << psz);
		free (psz);
	} else {
		LOGINFO (TEXT ("no default stop command"));
	}
#endif /* ifndef _WIN32 */
}

BEGIN_TESTS (ServiceSettingsTest)
	TEST (InternalDefaults);
	TEST (PublicDefaults);
	TEST (PublicCommandHelpers);
END_TESTS
