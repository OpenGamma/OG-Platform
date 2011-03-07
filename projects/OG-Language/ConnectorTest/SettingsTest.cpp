/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
	// TODO
}

BEGIN_TESTS (ConnectorSettingsTest)
	TEST (Defaults);
END_TESTS