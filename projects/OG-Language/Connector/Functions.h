/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_functions_h
#define __inc_og_language_connector_functions_h

#include "RequestBuilder.h"
#include "com_opengamma_language_function_Available.h"
#include "com_opengamma_language_function_QueryAvailable.h"

class CFunctionQueryAvailable : public CRequestBuilder {
protected:
	REQUESTBUILDER_REQUEST (com_opengamma_language_function_QueryAvailable)
public:
	CFunctionQueryAvailable (CConnector *poConnector) : CRequestBuilder (poConnector) { }
	bool Send () {
		com_opengamma_language_function_QueryAvailable query;
		memset (&query, 0, sizeof (query));
		return Send (&query);
	}
	REQUESTBUILDER_RESPONSE (com_opengamma_language_function_Available)
};

#endif /* ifndef __inc_og_language_connector_functions_h */
