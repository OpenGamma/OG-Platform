/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "RequestBuilder.h"
#include "Settings.h"

LOGGING (com.opengamma.language.connector.RequestBuilder);

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

CRequestBuilder::CRequestBuilder (CConnector *poConnector) {
	poConnector->Retain ();
	m_poConnector = poConnector;
	m_poQuery = NULL;
	m_pResponse = NULL;
}

CRequestBuilder::~CRequestBuilder () {
	assert (!m_pResponse);
	CConnector::Release (m_poConnector);
	if (m_poQuery) {
		delete m_poQuery;
	}
}

long CRequestBuilder::GetDefaultTimeout () {
	static long volatile s_lDefaultTimeout = 0;
	if (!s_lDefaultTimeout) {
		CSettings oSettings;
		s_lDefaultTimeout = oSettings.GetSendTimeout () * 2;
	}
	return s_lDefaultTimeout;
}
