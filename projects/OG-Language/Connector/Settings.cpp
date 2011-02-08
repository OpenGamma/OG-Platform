/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Runtime configuration options

#include "Settings.h"

LOGGING (com.opengamma.language.connector.Settings);

#define DEFAULT_DISPLAY_ALERTS		true
#define DEFAULT_LOG_CONFIGURATION	NULL

CSettings::CSettings () : CAbstractSettings () {
	// TODO
}

CSettings::~CSettings () {
	// TODO
}

bool CSettings::IsDisplayAlerts () {
	return IsDisplayAlerts (DEFAULT_DISPLAY_ALERTS);
}

const TCHAR *CSettings::GetLogConfiguration () {
	return GetLogConfiguration (DEFAULT_LOG_CONFIGURATION);
}