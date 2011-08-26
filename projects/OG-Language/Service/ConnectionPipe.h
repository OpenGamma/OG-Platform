/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_service_connectionpipe_h
#define __inc_og_language_service_connectionpipe_h

#include "Public.h"
#include <Util/NamedPipe.h>

/// Implementation of the IPC connection for incoming requests.
class CConnectionPipe {
private:

	/// Read timeout in milliseconds
	unsigned long m_dwReadTimeout;

	/// Underlying pipe for receiving raw connection data
	CNamedPipe *m_poPipe;

	CConnectionPipe (CNamedPipe *poPipe, unsigned long dwReadTimeout);
public:

	/// Returns the name of the IPC connection (the name of the underlying pipe).
	///
	/// @return the pipe name
	const TCHAR *GetName () const { return m_poPipe->GetName (); }

	/// Closes the IPC connection.
	///
	/// @return TRUE if successful, FALSE if there was a problem
	bool Close () const { return m_poPipe->Close (); }

	/// Cancels a pending LazyClose request if possible.
	///
	/// @return TRUE if the close was cancelled, FALSE if there was a problem (e.g. it had elapsed)
	bool CancelLazyClose () { return m_poPipe->CancelLazyClose (); }

	/// Tests if the connection is closed.
	///
	/// @return TRUE if the connection is closed, FALSE if it is still open (or in the process of closing)
	bool IsClosed () const { return m_poPipe->IsClosed (); }

	~CConnectionPipe ();
	static CConnectionPipe *Create (const TCHAR *pszSuffix = NULL);
	ClientConnect *ReadMessage ();
	bool LazyClose (unsigned long dwTimeout = 0);
};

#endif /* ifndef __inc_og_language_service_connectionpipe_h */
