/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_common_service_h
#define __inc_common_service_h

#include "param.h"

#define SERVICE_STATUS_UNKNOWN			0
#define SERVICE_STATUS_BAD_CONFIG		1
#define SERVICE_STATUS_BAD_WINSOCK		2
#define SERVICE_STATUS_BAD_SCM			3
#define SERVICE_STATUS_NOT_INSTALLED	4
#define SERVICE_STATUS_CONNECTOR_ERROR	5
#define SERVICE_STATUS_STOPPED			6
#define SERVICE_STATUS_BUSY				7
#define SERVICE_STATUS_QUERY_ERROR		8
#define SERVICE_STATUS_OK				9
#define SERVICE_STATUS_STARTING			10

class CService {
private:
	int m_nWinsock;
	const CParamString *m_poServiceName;
	const CParamString *m_poHost;
	const CParamString *m_poPort;
	PCSTR GetServiceName () { return m_poServiceName ? m_poServiceName->GetString () : NULL; }
	PCSTR GetHost () { return m_poHost ? m_poHost->GetString () : NULL; }
	PCSTR GetPort () { return m_poPort ? m_poPort->GetString () : NULL; }
public:
	CService (const CParamString *poServiceName, const CParamString *poHost, const CParamString *poPort);
	~CService ();
	BOOL Start ();
	BOOL Stop ();
	BOOL IsAutoStart ();
	BOOL SetAutoStart (BOOL bEnable);
	int GetStatus ();
};

#endif /* ifndef __inc_common_service_h */