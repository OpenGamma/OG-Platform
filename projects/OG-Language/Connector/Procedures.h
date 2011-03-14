/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_procedures_h
#define __inc_og_language_connector_procedures_h

#include "RequestBuilder.h"
#include "com_opengamma_language_procedure_Available.h"
#include "com_opengamma_language_procedure_QueryAvailable.h"

class CProcedureQueryAvailable : public CRequestBuilder {
protected:
	REQUESTBUILDER_REQUEST (com_opengamma_language_procedure_QueryAvailable)
public:
	CProcedureQueryAvailable (CConnector *poConnector) : CRequestBuilder (poConnector) { }
	bool Send () {
		com_opengamma_language_procedure_QueryAvailable query;
		memset (&query, 0, sizeof (query));
		return Send (&query);
	}
	REQUESTBUILDER_RESPONSE (com_opengamma_language_procedure_Available)
};

#endif /* ifndef __inc_og_language_connector_procedures_h */
