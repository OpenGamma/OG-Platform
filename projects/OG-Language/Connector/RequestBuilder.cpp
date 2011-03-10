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
	if (!m_poConnector) {
		LOGWARN (TEXT ("Connector has already been released"));
		SetLastError (EALREADY);
		return false;
	}
	m_poQuery = m_poConnector->Call (msg);
	CConnector::Release (m_poConnector);
	m_poConnector = NULL;
	return m_poQuery != NULL;
}

FudgeMsg CRequestBuilder::RecvMsg (long lTimeout) {
	LOGDEBUG (TEXT ("Waiting for response"));
	if (!m_poQuery) {
		if (m_poConnector) {
			LOGWARN (TEXT ("Request has not been sent"));
		} else {
			LOGWARN (TEXT ("Response has already been received"));
		}
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
	return msg;
}

CRequestBuilder::CRequestBuilder (CConnector *poConnector) {
	poConnector->Retain ();
	m_poConnector = poConnector;
	m_poQuery = NULL;
	Init ();
}

CRequestBuilder::~CRequestBuilder () {
	Done ();
	if (m_poConnector) {
		CConnector::Release (m_poConnector);
	}
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