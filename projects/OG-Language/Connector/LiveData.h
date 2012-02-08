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
#include "com_opengamma_language_livedata_QueryAvailable.h"
#include "com_opengamma_language_livedata_Result.h"

#ifndef CLASS_com_opengamma_language_livedata_Connect
#define CLASS_com_opengamma_language_livedata_Connect com_opengamma_language_livedata_Connect
#endif /* ifndef CLASS_com_opengamma_language_livedata_Connect */

#ifndef CLASS_com_opengamma_language_livedata_Result
#define CLASS_com_opengamma_language_livedata_Result com_opengamma_language_livedata_Result
#endif /* ifndef CLASS_com_opengamma_language_livedata_Result */

/// Message builder for LiveData/Connect.
REQUESTBUILDER_BEGIN (CLiveDataConnect)
	REQUESTBUILDER_REQUEST (CLASS_com_opengamma_language_livedata_Connect)
	void SetComponentId (int nComponentId) {
		m_request._identifier = nComponentId;
	}
	void SetConnectionId (int nConnectionId) {
		m_request._connection = (fudge_i32*)malloc (sizeof (fudge_i32));
		if (m_request._connection) {
			*m_request._connection = nConnectionId;
		}
	}
	void SetParameters (int nCount, const com_opengamma_language_Data * const *ppData) {
		m_request.fudgeCountParameter = nCount;
		m_request._parameter = (com_opengamma_language_Data**)ppData;
	}
	REQUESTBUILDER_RESPONSE (CLASS_com_opengamma_language_livedata_Result)
REQUESTBUILDER_END

#ifndef CLASS_com_opengamma_language_livedata_QueryAvailable
#define CLASS_com_opengamma_language_livedata_QueryAvailable com_opengamma_language_livedata_QueryAvailable
#endif /* CLASS_com_opengamma_language_livedata_QueryAvailable */

#ifndef CLASS_com_opengamma_language_livedata_Available
#define CLASS_com_opengamma_language_livedata_Available com_opengamma_language_livedata_Available
#endif /* ifndef CLASS_com_opengamma_language_livedata_Available */

/// Message builder for LiveData/QueryAvailable.
REQUESTBUILDER_BEGIN(CLiveDataQueryAvailable)
	REQUESTBUILDER_REQUEST (CLASS_com_opengamma_language_livedata_QueryAvailable)
	REQUESTBUILDER_RESPONSE (CLASS_com_opengamma_language_livedata_Available)
REQUESTBUILDER_END

#endif /* ifndef __inc_og_language_connector_livedata_h */
