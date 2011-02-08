/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Main connector API

#include "Public.h"

LOGGING (com.opengamma.language.connector.Connector);

CConnector::CCall::CCall () {
	TODO (__FUNCTION__);
}

CConnector::CCall::~CCall () {
	TODO (__FUNCTION__);
}

bool CConnector::CCall::Cancel () {
	TODO (__FUNCTION__);
	return false;
}

bool CConnector::CCall::WaitForResult (FudgeMsg *pmsgResponse, unsigned long lTimeout) {
	TODO (__FUNCTION__);
	return false;
}

CConnector::CConnector () {
	m_oRefCount.Set (1);
}

CConnector::~CConnector () {
	assert (!m_oRefCount.Get ());
}

CConnector *CConnector::Start () {
	TODO (__FUNCTION__);
	return NULL;
}

bool CConnector::Stop () {
	TODO (__FUNCTION__);
	return false;
}

bool CConnector::WaitForStartup (unsigned long lTimeout) {
	TODO (__FUNCTION__);
	return false;
}

bool CConnector::Call (FudgeMsg msgPayload, FudgeMsg *pmsgResponse, unsigned long lTimeout) {
	CCall *poOverlapped = Call (msgPayload);
	if (!poOverlapped) {
		int error = GetLastError ();
		LOGWARN (TEXT ("Couldn't initiate call, error ") << error);
		SetLastError (error);
		return false;
	}
	if (poOverlapped->WaitForResult (pmsgResponse, lTimeout)) {
		delete poOverlapped;
		return true;
	} else {
		int error = GetLastError ();
		LOGWARN (TEXT ("Couldn't get result, error ") << error);
		poOverlapped->Cancel ();
		delete poOverlapped;
		SetLastError (error);
		return false;
	}
}

CConnector::CCall *CConnector::Call (FudgeMsg msgPayload) {
	TODO (__FUNCTION__);
	return NULL;
}

bool CConnector::Send (FudgeMsg msgPayload) {
	TODO (__FUNCTION__);
	return false;
}

bool CConnector::AddCallback (const TCHAR *pszClass, CCallback *poCallback) {
	TODO (__FUNCTION__);
	return false;
}

bool CConnector::RemoveCallback (CCallback *poCallback) {
	TODO (__FUNCTION__);
	return false;
}