/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_procedures_h
#define __inc_og_language_connector_procedures_h

#include "RequestBuilder.h"
#include "com_opengamma_language_procedure_Available.h"
#include "com_opengamma_language_procedure_Invoke.h"
#include "com_opengamma_language_procedure_QueryAvailable.h"
#include "com_opengamma_language_procedure_Result.h"

#ifndef CLASS_com_opengamma_language_procedure_Invoke
#define CLASS_com_opengamma_language_procedure_Invoke com_opengamma_language_procedure_Invoke
#endif /* ifndef CLASS_com_opengamma_language_procedure_Invoke */

#ifndef CLASS_com_opengamma_language_procedure_Result
#define CLASS_com_opengamma_language_procedure_Result com_opengamma_language_procedure_Result
#endif /* ifndef CLASS_com_opengamma_language_procedure_Result */

/// Message builder for Procedure/Invoke
REQUESTBUILDER_BEGIN (CProcedureInvoke)
	REQUESTBUILDER_REQUEST (CLASS_com_opengamma_language_procedure_Invoke)
	void SetInvocationId (int nInvocationId) {
		m_request._identifier = nInvocationId;
	}
	void SetParameters (int nCount, const com_opengamma_language_Data * const *ppData) {
		m_request.fudgeCountParameter = nCount;
		m_request._parameter = (com_opengamma_language_Data**)ppData;
	}
	REQUESTBUILDER_RESPONSE (CLASS_com_opengamma_language_procedure_Result)
REQUESTBUILDER_END

#ifndef CLASS_com_opengamma_language_procedure_QueryAvailable
#define CLASS_com_opengamma_language_procedure_QueryAvailable com_opengamma_language_procedure_QueryAvailable
#endif /* ifndef CLASS_com_opengamma_language_procedure_QueryAvailable */

#ifndef CLASS_com_opengamma_language_procedure_Available
#define CLASS_com_opengamma_language_procedure_Available com_opengamma_language_procedure_Available
#endif /* ifndef CLASS_com_opengamma_language_procedure_Available */

/// Message builder for Procedure/QueryAvailable
REQUESTBUILDER_BEGIN (CProcedureQueryAvailable)
	REQUESTBUILDER_REQUEST (CLASS_com_opengamma_language_procedure_QueryAvailable)
	REQUESTBUILDER_RESPONSE (CLASS_com_opengamma_language_procedure_Available)
REQUESTBUILDER_END

#endif /* ifndef __inc_og_language_connector_procedures_h */
