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
	const CConnector *m_poConnector;
	CConnector::CCall *m_poQuery;
protected:
	void *m_pResponse;
	bool SendMsg (FudgeMsg msg);
	FudgeMsg RecvMsg (long lTimeout);
	virtual void Init () { }
	virtual void Done () { }
	virtual void _Done () { }
	virtual bool SendOk () const { return true; }
public:
	CRequestBuilder (const CConnector *poConnector);
    virtual ~CRequestBuilder ();
    static long GetDefaultTimeout ();
    const CConnector *GetConnector () const { m_poConnector->Retain (); return m_poConnector; }
#define REQUESTBUILDER_REQUEST(_objtype_) \
protected: \
	_objtype_ m_request; \
	bool Send (_objtype_ *obj) { \
		if (!obj) return false; \
		FudgeMsg msg; \
		if (_objtype_##_toFudgeMsg (obj, &msg) != FUDGE_OK) return false; \
		bool bResult = SendMsg (msg); \
		FudgeMsg_release (msg); \
		return bResult; \
	} \
	void Init () { \
		memset (&m_request, 0, sizeof (m_request)); \
	} \
public: \
	bool Send () { return SendOk () ? Send (&m_request) : false; }
#define REQUESTBUILDER_RESPONSE(_objtype_) \
protected: \
	void _Done () { \
		if (m_pResponse) { _objtype_##_free ((_objtype_*)m_pResponse); m_pResponse = NULL; } \
	} \
public: \
	_objtype_ *Recv (long lTimeout) { \
		if (m_pResponse) return (_objtype_*)m_pResponse; \
		FudgeMsg msg = RecvMsg (lTimeout); \
		if (!msg) return NULL; \
		if (_objtype_##_fromFudgeMsg (msg, (_objtype_**)&m_pResponse) != FUDGE_OK) { \
			m_pResponse = NULL; \
		} \
		FudgeMsg_release (msg); \
		return (_objtype_*)m_pResponse; \
	} \
	_objtype_ *DetachResponse () { \
		_objtype_ *pResponse = Recv (GetDefaultTimeout ()); \
		m_pResponse = NULL; \
		return pResponse; \
	}
};

#define REQUESTBUILDER_BEGIN(_class_) \
	class _class_ : public CRequestBuilder { \
	public: \
		_class_ (const CConnector *poConnector) : CRequestBuilder (poConnector) { Init (); } \
		~_class_ () { _Done (); Done (); }

#define REQUESTBUILDER_END };

#endif /* ifndef __inc_og_language_connector_requestbuilder_h */
