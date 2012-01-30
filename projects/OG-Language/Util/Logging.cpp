/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Logging.h"
#include "Unicode.h"
#include <log4cxx/propertyconfigurator.h>
#include <log4cxx/basicconfigurator.h>

LOGGING (com.opengamma.language.util.Logging);

static void setDefaultLoggingLevel () {
#ifndef _DEBUG
	::log4cxx::LoggerPtr ptrLogger = ::log4cxx::Logger::getRootLogger ();
	::log4cxx::LevelPtr ptrLevel = ::log4cxx::Level::getError ();
	ptrLogger->setLevel (ptrLevel);
#endif /* ifndef _DEBUG */
}

/// Initialise the LOG4CXX subsystem. Only the first call is applied, subsequent calls are
/// ignored to avoid duplicate loggers being created.
///
/// @param[in] pszLogConfiguration path to the log4cxx configuration file.
void LoggingInitImpl (const TCHAR *pszLogConfiguration, bool bApplyDefault) {
	static bool bInitialised = false;
	if (bInitialised) {
		LOGWARN (TEXT ("Logging already initialised, duplicate call with ") << pszLogConfiguration);
		return;
	} else {
		bInitialised = true;
	}
	if (pszLogConfiguration != NULL) {
		::log4cxx::PropertyConfigurator::configure (pszLogConfiguration);
		LOGINFO (TEXT ("Logging initialised from ") << pszLogConfiguration);
	} else {
		::log4cxx::BasicConfigurator::configure ();
		if (bApplyDefault) {
			setDefaultLoggingLevel ();
		}
		LOGINFO (TEXT ("Logging initialised with default settings"));
	}
}

/// Initialises the LOG4CXX subsystem. The log configuration file is obtained from the
/// settings object.
///
/// @param[in] poSettings settings object to resolve the configuration file, or NULL for default configuration
void LoggingInit (const CAbstractSettings *poSettings) {
	LoggingInitImpl (poSettings ? poSettings->GetLogConfiguration () : NULL, true);
}
