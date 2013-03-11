/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_service_errorfeedback_h
#define __inc_og_language_service_errorfeedback_h

/// Provides a mechanism for error messages to be routed back to the service caller. This may be by
/// writing to a file in a known location, posting to a system error log in a particular fashion, writing to
/// a particular IPC channel, or some other system specific device.
class CErrorFeedback {
public:
	CErrorFeedback ();
	~CErrorFeedback ();
	void Write (const TCHAR *pszMessage);
};

#endif /* ifndef __inc_og_language_service_errorfeedback_h */
