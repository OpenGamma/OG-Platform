/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Runtime configuration options

#include "Settings.h"

LOGGING (com.opengamma.language.connector.Settings);

#ifdef _WIN32
#define DEFAULT_PIPE_PREFIX			TEXT ("\\\\.\\pipe\\OpenGammaLanguageAPI-Client-")
#else
#define DEFAULT_PIPE_PREFIX			TEXT ("/var/run/OG-Language/Client-")
#endif

#define DEFAULT_DISPLAY_ALERTS		true
#define DEFAULT_INPUT_PIPE_PREFIX	DEFAULT_PIPE_PREFIX	TEXT ("Input-")
#define DEFAULT_LOG_CONFIGURATION	NULL
#define DEFAULT_MAX_PIPE_ATTEMPTS	3
#define DEFAULT_OUTPUT_PIPE_PREFIX	DEFAULT_PIPE_PREFIX TEXT ("Output-")

CSettings::CSettings () : CAbstractSettings () {
	// TODO
}

CSettings::~CSettings () {
	// TODO
}

bool CSettings::IsDisplayAlerts () {
	return IsDisplayAlerts (DEFAULT_DISPLAY_ALERTS);
}

const TCHAR *CSettings::GetInputPipePrefix () {
	return GetInputPipePrefix (DEFAULT_INPUT_PIPE_PREFIX);
}

const TCHAR *CSettings::GetLogConfiguration () {
	return GetLogConfiguration (DEFAULT_LOG_CONFIGURATION);
}

int CSettings::GetMaxPipeAttempts () {
	return GetMaxPipeAttempts (DEFAULT_MAX_PIPE_ATTEMPTS);
}

const TCHAR *CSettings::GetOutputPipePrefix () {
	return GetOutputPipePrefix (DEFAULT_OUTPUT_PIPE_PREFIX);
}