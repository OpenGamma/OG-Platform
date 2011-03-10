/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_memorypool_h
#define __inc_og_language_util_memorypool_h

// C++ wrapper for APR memory pool functions

#include <apr-1/apr_pools.h>
#include "Error.h"

class CMemoryPool {
private:
	apr_pool_t *m_pPool;
public:
	CMemoryPool () {
		m_pPool = NULL;
		apr_pool_create_core (&m_pPool);
	}
	~CMemoryPool () {
		apr_pool_destroy (m_pPool);
	}
	operator apr_pool_t *() {
		return m_pPool;
	}
	void *Malloc (size_t cb) {
		return apr_palloc (m_pPool, cb);
	}
	void Clear () {
		apr_pool_clear (m_pPool);
	}
};

#endif /* ifndef __inc_og_language_util_memorypool_h */
