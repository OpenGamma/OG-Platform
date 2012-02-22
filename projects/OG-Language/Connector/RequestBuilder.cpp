/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "RequestBuilder.h"
#include "Settings.h"

LOGGING (com.opengamma.language.connector.RequestBuilder);

/// Sends the message to the Java stack, storing the overlapped call object
///
/// @param[in] msg message to send
/// @return TRUE if the call was initiated, FALSE if there was an error
bool CRequestBuilder::SendMsg (FudgeMsg msg) {
	LOGDEBUG (TEXT ("Sending request"));
	if (m_poQuery) {
		LOGWARN (TEXT ("Request has already been sent"));
		SetLastError (EALREADY);
		return false;
	}
	m_poQuery = m_poConnector->Call (msg);
	return m_poQuery != NULL;
}

/// Posts the message to the Java stack, no response is expected.
///
/// @param[in] msg message to send
/// @return TRUE if the call was initiated, FALSE if there was an error
bool CRequestBuilder::PostMsg (FudgeMsg msg) {
	LOGDEBUG (TEXT ("Sending request"));
	return m_poConnector->Send (msg);
}

/// Waits for a response from the Java stack.
///
/// @param[in] lTimeout maximum time to wait for a response in milliseconds
/// @return the response received or NULL if there was a problem.
FudgeMsg CRequestBuilder::RecvMsg (long lTimeout) {
	LOGDEBUG (TEXT ("Waiting for response"));
	if (!m_poQuery) {
		LOGWARN (TEXT ("No request pending"));
		SetLastError (EALREADY);
		return NULL;
	}
	FudgeMsg msg;
	if (!m_poQuery->WaitForResult (&msg, lTimeout)) {
		int ec = GetLastError ();
		LOGWARN (TEXT ("Response not received, error ") << ec);
		SetLastError (ec);
		return NULL;
	}
	delete m_poQuery;
	m_poQuery = NULL;
	return msg;
}

/// Creates a new request builder instance using the given connector.
///
/// @param[in] poConnector connector instance
CRequestBuilder::CRequestBuilder (const CConnector *poConnector) {
	poConnector->Retain ();
	m_poConnector = poConnector;
	m_poQuery = NULL;
	m_pResponse = NULL;
}

/// Destroys the request builder instance.
CRequestBuilder::~CRequestBuilder () {
	assert (!m_pResponse);
	CConnector::Release (m_poConnector);
	if (m_poQuery) {
		delete m_poQuery;
	}
}

/// Returns a default timeout setting to use for requests (taken from a CSettings
/// instance and cached). This should be used unless there is good reason to believe
/// the call will complete in a much longer (or shorter) time. If the latter, the
/// time should not be hard-coded elsewhere but held as a multiple of the default
/// send timeout.
///
/// @return the timeout in milliseconds
long CRequestBuilder::GetDefaultTimeout () {
	static long volatile s_lDefaultTimeout = 0;
	if (!s_lDefaultTimeout) {
		CSettings oSettings;
		s_lDefaultTimeout = oSettings.GetRequestTimeout ();
	}
	return s_lDefaultTimeout;
}
