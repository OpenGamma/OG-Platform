/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_requestbuilder_h
#define __inc_og_language_connector_requestbuilder_h

#include "Connector.h"

// Constructs a request to send to the Java stack. If a response is received, a pointer
// to the object allocated internally is returned. This must not be freed by the caller
// it will be deallocated at RequestBuilder destruction. If the returned object is needed
// beyond the lifetime of the RequestBuilder, call DetachResponse.
class CRequestBuilder {
private:
	CConnector *m_poConnector;
	CConnector::CCall *m_poQuery;
protected:
	bool SendMsg (FudgeMsg msg);
	FudgeMsg RecvMsg (long lTimeout);
	virtual void Init () { }
	virtual void Done () { }
#define REQUESTBUILDER_REQUEST(_objtype_) \
	bool Send (_objtype_ *obj) { \
		if (!obj) return false; \
		FudgeMsg msg; \
		if (_objtype_##_toFudgeMsg (obj, &msg) != FUDGE_OK) return false; \
		bool bResult = SendMsg (msg); \
		FudgeMsg_release (msg); \
		return bResult; \
	}
public:
	CRequestBuilder (CConnector *poConnector);
	virtual ~CRequestBuilder ();
	virtual bool Send () = 0;
	static long GetDefaultTimeout ();
#define REQUESTBUILDER_RESPONSE(_objtype_) \
private: \
	_objtype_ *m_pResponse; \
protected: \
	void Init () { m_pResponse = NULL; } \
	void Done () { if (m_pResponse) _objtype_##_free (m_pResponse); } \
public: \
	_objtype_ *Recv (long lTimeout) { \
		if (m_pResponse) return m_pResponse; \
		FudgeMsg msg = RecvMsg (lTimeout); \
		if (!msg) return NULL; \
		if (_objtype_##_fromFudgeMsg (msg, &m_pResponse) != FUDGE_OK) { \
			m_pResponse = NULL; \
		} \
		FudgeMsg_release (msg); \
		return m_pResponse; \
	} \
	_objtype_ *DetachResponse () { \
		_objtype_ *pResponse = Recv (GetDefaultTimeout ()); \
		m_pResponse = NULL; \
		return pResponse; \
	}
};

#endif /* ifndef __inc_og_language_connector_requestbuilder_h */
