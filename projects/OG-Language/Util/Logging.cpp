/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Logging wrapper for log4cxx to simplify it

#include "Logging.h"
#include "Unicode.h"
#include <log4cxx/propertyconfigurator.h>
#include <log4cxx/basicconfigurator.h>

LOGGING (com.opengamma.language.util.Logging);

void LoggingInitImpl (const TCHAR *pszLogConfiguration) {
	static bool bInitialised = false;
	if (bInitialised) {
		LOGWARN (TEXT ("Logging already initialised, duplicate call with ") << pszLogConfiguration);
		return;
	} else {
		bInitialised = true;
	}
	if (pszLogConfiguration != NULL) {
		LOGDEBUG (TEXT ("Initialising logging from ") << pszLogConfiguration);
		::log4cxx::PropertyConfigurator::configure (pszLogConfiguration);
	} else {
		LOGDEBUG (TEXT ("Initialising logging with default settings"));
		::log4cxx::BasicConfigurator::configure ();
	}
	LOGINFO (TEXT ("Logs initialised"));
}

void LoggingInit (CAbstractSettings *poSettings) {
	LoggingInitImpl (poSettings ? poSettings->GetLogConfiguration () : NULL);
}
