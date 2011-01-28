/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Util/DllVersion.cpp

#include "Util/DllVersion.h"

LOGGING (com.opengamma.language.util.DllVersionTest);

static void DefaultValues () {
	CDllVersion version;
	ASSERT (!_tcslen (version.GetComments ())); // No comments in our reference file
	ASSERT (_tcslen (version.GetCompanyName ()));
	ASSERT (_tcslen (version.GetFileDescription ()));
	ASSERT (_tcslen (version.GetFileVersion ()));
	ASSERT (_tcslen (version.GetInternalName ()));
	ASSERT (_tcslen (version.GetLegalCopyright ()));
	ASSERT (_tcslen (version.GetOriginalFilename ()));
	ASSERT (_tcslen (version.GetProductName ()));
	ASSERT (_tcslen (version.GetProductVersion ()));
	ASSERT (!_tcslen (version.GetPrivateBuild ())); // No private build in our reference file
	ASSERT (!_tcslen (version.GetSpecialBuild ())); // No special build in our reference file
}

BEGIN_TESTS (DllVersionTest)
	TEST (DefaultValues)
END_TESTS