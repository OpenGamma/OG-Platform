/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_livedata_h
#define __inc_og_language_connector_livedata_h

#include "RequestBuilder.h"
#include "com_opengamma_language_livedata_Available.h"
#include "com_opengamma_language_livedata_Connect.h"
#include "com_opengamma_language_livedata_Disconnect.h"
#include "com_opengamma_language_livedata_QueryAvailable.h"
#include "com_opengamma_language_livedata_QueryValue.h"
#include "com_opengamma_language_livedata_Result.h"

#ifndef CLASS_com_opengamma_language_livedata_Connect
#define CLASS_com_opengamma_language_livedata_Connect com_opengamma_language_livedata_Connect
#endif /* ifndef CLASS_com_opengamma_language_livedata_Connect */

#ifndef CLASS_com_opengamma_language_livedata_Result
#define CLASS_com_opengamma_language_livedata_Result com_opengamma_language_livedata_Result
#endif /* ifndef CLASS_com_opengamma_language_livedata_Result */

/// Message builder for LiveData/Connect.
REQUESTBUILDER_BEGIN (CLiveDataConnect)
private:
	fudge_i32 m_nConnectionId;
	REQUESTBUILDER_REQUEST (CLASS_com_opengamma_language_livedata_Connect)
	void SetComponentId (int nComponentId) {
		m_request._identifier = nComponentId;
	}
	void SetConnectionId (int nConnectionId) {
		m_nConnectionId = nConnectionId;
		m_request._connection = &m_nConnectionId;
	}
	void SetParameters (int nCount, const com_opengamma_language_Data * const *ppData) {
		m_request.fudgeCountParameter = nCount;
		m_request._parameter = (com_opengamma_language_Data**)ppData;
	}
	REQUESTBUILDER_RESPONSE (CLASS_com_opengamma_language_livedata_Result)
REQUESTBUILDER_END

#ifndef CLASS_com_opengamma_language_livedata_Disconnect
#define CLASS_com_opengamma_language_livedata_Disconnect com_opengamma_language_livedata_Disconnect
#endif /* ifndef CLASS_com_opengamma_language_livedata_Disconnect */

/// Message builder for LiveData/Disconnect.
REQUESTBUILDER_BEGIN (CLiveDataDisconnect)
	REQUESTBUILDER_POST (CLASS_com_opengamma_language_livedata_Disconnect)
	void SetConnectionId (int nConnectionId) {
		m_request._connection = nConnectionId;
	}
REQUESTBUILDER_END

#ifndef CLASS_com_opengamma_language_livedata_QueryAvailable
#define CLASS_com_opengamma_language_livedata_QueryAvailable com_opengamma_language_livedata_QueryAvailable
#endif /* ifndef CLASS_com_opengamma_language_livedata_QueryAvailable */

#ifndef CLASS_com_opengamma_language_livedata_Available
#define CLASS_com_opengamma_language_livedata_Available com_opengamma_language_livedata_Available
#endif /* ifndef CLASS_com_opengamma_language_livedata_Available */

/// Message builder for LiveData/QueryAvailable.
REQUESTBUILDER_BEGIN (CLiveDataQueryAvailable)
	REQUESTBUILDER_REQUEST (CLASS_com_opengamma_language_livedata_QueryAvailable)
	REQUESTBUILDER_RESPONSE (CLASS_com_opengamma_language_livedata_Available)
REQUESTBUILDER_END

#ifndef CLASS_com_opengamma_language_livedata_QueryValue
#define CLASS_com_opengamma_language_livedata_QueryValue com_opengamma_language_livedata_QueryValue
#endif /* ifndef CLASS_com_opengamma_language_livedata_QueryValue */

/// Message builder for LiveData/QueryValue
REQUESTBUILDER_BEGIN (CLiveDataQueryValue)
	REQUESTBUILDER_REQUEST (CLASS_com_opengamma_language_livedata_QueryValue)
	void SetConnectionId (int nConnectionId) {
		m_request._identifier = nConnectionId;
	}
	REQUESTBUILDER_RESPONSE (CLASS_com_opengamma_language_livedata_Result)
REQUESTBUILDER_END

#endif /* ifndef __inc_og_language_connector_livedata_h */
