/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_functions_h
#define __inc_og_language_connector_functions_h

#include "RequestBuilder.h"
#include "com_opengamma_language_function_Available.h"
#include "com_opengamma_language_function_Invoke.h"
#include "com_opengamma_language_function_QueryAvailable.h"
#include "com_opengamma_language_function_Result.h"

#ifndef CLASS_com_opengamma_language_function_Invoke
#define CLASS_com_opengamma_language_function_Invoke com_opengamma_language_function_Invoke
#endif /* ifndef CLASS_com_opengamma_language_function_Invoke */

#ifndef CLASS_com_opengamma_language_function_Result
#define CLASS_com_opengamma_language_function_Result com_opengamma_language_function_Result
#endif /* ifndef CLASS_com_opengamma_language_function_Result */

/// Message builder for Function/Invoke.
REQUESTBUILDER_BEGIN (CFunctionInvoke)
	REQUESTBUILDER_REQUEST (CLASS_com_opengamma_language_function_Invoke)
	void SetInvocationId (int nInvocationId) {
		m_request._identifier = nInvocationId;
	}
	void SetParameters (int nCount, const com_opengamma_language_Data * const *ppData) {
		m_request.fudgeCountParameter = nCount;
		m_request._parameter = (com_opengamma_language_Data**)ppData;
	}
	REQUESTBUILDER_RESPONSE (CLASS_com_opengamma_language_function_Result)
REQUESTBUILDER_END

#ifndef CLASS_com_opengamma_language_function_QueryAvailable
#define CLASS_com_opengamma_language_function_QueryAvailable com_opengamma_language_function_QueryAvailable
#endif /* CLASS_com_opengamma_language_function_QueryAvailable */

#ifndef CLASS_com_opengamma_language_function_Available
#define CLASS_com_opengamma_language_function_Available com_opengamma_language_function_Available
#endif /* ifndef CLASS_com_opengamma_language_function_Available */

/// Message builder for Function/QueryAvailable.
REQUESTBUILDER_BEGIN (CFunctionQueryAvailable)
	REQUESTBUILDER_REQUEST (CLASS_com_opengamma_language_function_QueryAvailable)
	REQUESTBUILDER_RESPONSE (CLASS_com_opengamma_language_function_Available)
REQUESTBUILDER_END

#endif /* ifndef __inc_og_language_connector_functions_h */
