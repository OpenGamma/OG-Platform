/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Connector/Settings.cpp

#include "Connector/Settings.h"

LOGGING (com.opengamma.language.connector.SettingsTest);

#define PRINT(_key_) \
	LOGINFO (TEXT (#_key_) TEXT (" = ") << settings.Get##_key_ ());

#define PRINT_AND_ASSERT(_key_) \
	PRINT (_key_); \
	ASSERT (settings.Get##_key_());

static void Defaults () {
	CSettings settings;
	PRINT_AND_ASSERT (ConnectionPipe)
	PRINT_AND_ASSERT (ConnectTimeout)
	PRINT_AND_ASSERT (HeartbeatTimeout)
	PRINT_AND_ASSERT (InputPipePrefix)
	PRINT (LogConfiguration)
	PRINT_AND_ASSERT (MaxPipeAttempts)
	PRINT_AND_ASSERT (OutputPipePrefix)
	PRINT_AND_ASSERT (SendTimeout)
	PRINT_AND_ASSERT (ServiceExecutable)
	PRINT_AND_ASSERT (ServiceName)
	PRINT_AND_ASSERT (ServicePoll)
	PRINT_AND_ASSERT (StartTimeout)
	PRINT_AND_ASSERT (StopTimeout)
}

BEGIN_TESTS (ConnectorSettingsTest)
	TEST (Defaults);
END_TESTS
