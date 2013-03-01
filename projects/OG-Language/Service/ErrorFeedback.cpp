/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "ErrorFeedback.h"

LOGGING(com.opengamma.language.service.ErrorFeedback);

/// Creates a new feedback object.
CErrorFeedback::CErrorFeedback () {
}

/// Destroys the feedback object.
CErrorFeedback::~CErrorFeedback () {
}

/// Writes a feedback entry to the mechanism.
///
/// @param[in] pszMessage the string to write
void CErrorFeedback::Write (const TCHAR *pszMessage) {
	LOGERROR (TEXT ("A major error occurred"));
	TCHAR *psz = _tcsdup (pszMessage);
	if (psz) {
		TCHAR *pszNext;
		TCHAR *pszLine = _tcstok_s (psz, TEXT ("\n"), &pszNext);
		while (pszLine) {
			LOGWARN (TEXT (">> ") << pszLine);
			pszLine = _tcstok_s (NULL, TEXT ("\n"), &pszNext);
		}
		free (psz);
	} else {
		LOGFATAL (TEXT ("Out of memory"));
	}
}
