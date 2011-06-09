/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_memorypool_h
#define __inc_og_language_util_memorypool_h

#ifndef _WIN32

#include <apr-1/apr_pools.h>

/// C++ wrapper for the APR memory pool functions
class CMemoryPool {
private:

	/// Wrapped APR memory pool
	apr_pool_t *m_pPool;

public:

	/// Creates a new memory pool
	CMemoryPool () {
		m_pPool = NULL;
		apr_pool_create_core (&m_pPool);
	}

	/// Destroys a memory pool, releasing any allocated memory
	~CMemoryPool () {
		apr_pool_destroy (m_pPool);
	}

	/// Casts the memory pool to the underlying apt_pool_t* type for use with
	/// other APR library functions
	operator apr_pool_t *() {
		return m_pPool;
	}

	/// Allocates a block of memory from the pool
	///
	/// @param[in] cb bytes to allocate
	/// @return the allocated memory
	void *Malloc (size_t cb) {
		return apr_palloc (m_pPool, cb);
	}

	/// Releases any memory allocated from the pool
	void Clear () {
		apr_pool_clear (m_pPool);
	}

};

#endif /* ifndef _WIN32 */

#endif /* ifndef __inc_og_language_util_memorypool_h */
