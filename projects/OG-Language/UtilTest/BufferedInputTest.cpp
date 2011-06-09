/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#define _INTERNAL
#include "Util/BufferedInput.h"

LOGGING (com.opengamma.language.util.BufferedInputTest);

static void EmptyState () {
	CBufferedInput *poBuffer = new CBufferedInput ();
	ASSERT (poBuffer);
	ASSERT (poBuffer->GetData ());
	ASSERT (!poBuffer->GetAvailable ());
	poBuffer->Discard (1024);
	ASSERT (!poBuffer->GetAvailable ());
	delete poBuffer;
}

#ifdef _WIN32
static TCHAR *_createTempFile (size_t cbBytes) {
	TCHAR szTempPath[MAX_PATH];
	TCHAR szTempFile[MAX_PATH];
	DWORD dwPathLen;
	ASSERT (((dwPathLen = GetTempPath (MAX_PATH, szTempPath)) > 0) && (dwPathLen < MAX_PATH));
	ASSERT (GetTempFileName (szTempPath, TEXT ("BufferedInputTest"), 0, szTempFile) != 0);
	LOGDEBUG (TEXT ("Creating temporary file ") << szTempFile << TEXT (" for test"));
	HANDLE hFile = CreateFile (szTempFile, GENERIC_WRITE, 0, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
	ASSERT (hFile != NULL);
	DWORD dw;
	while ((cbBytes > 0) && WriteFile (hFile, szTempPath, dwPathLen * sizeof (TCHAR), &dw, NULL)) {
		if (dw >= cbBytes) {
			break;
		} else {
			cbBytes -= dw;
		}
	}
	CloseHandle (hFile);
	return _tcsdup (szTempFile);
}
#endif /* ifdef _WIN32 */

#define TIMEOUT_READ	1000 /* the test file shouldn't block, but 1s for slow devices */

static void ReadSequence () {
	size_t cbInitialBuffer = INITIAL_BUFFER_SIZE;
#ifdef _WIN32
	PTSTR pszTemp = _createTempFile (cbInitialBuffer * 7);
	ASSERT (pszTemp != NULL);
	HANDLE hFile = CreateFile (pszTemp, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL | FILE_FLAG_OVERLAPPED, NULL);
	ASSERT (hFile != INVALID_HANDLE_VALUE);
	CTimeoutIO *poRead = new CTimeoutIO (hFile);
#else
	int file = open ("/dev/urandom", O_RDONLY);
	ASSERT (file);
	CTimeoutIO *poRead = new CTimeoutIO (file);
#endif
	CBufferedInput *poBuffer = new CBufferedInput ();
	const void *pSave = poBuffer->GetData ();
	// Enough room in the buffer to fulfill request
	ASSERT (poBuffer->Read (poRead, cbInitialBuffer / 2, TIMEOUT_READ)); // 0.5
	ASSERT (poBuffer->GetAvailable () >= cbInitialBuffer / 2);
	ASSERT (poBuffer->GetData () == pSave);
	ASSERT (poBuffer->Read (poRead, cbInitialBuffer, TIMEOUT_READ)); // 1.5
	ASSERT (poBuffer->GetAvailable () >= cbInitialBuffer);
	ASSERT (poBuffer->GetData () == pSave);
	// Not enough room for request - forces realloc
	ASSERT (poBuffer->Read (poRead, cbInitialBuffer * 2, TIMEOUT_READ)); // 3.5
	ASSERT (poBuffer->GetAvailable () >= cbInitialBuffer * 2);
	ASSERT (poBuffer->GetData () != pSave);
	pSave = poBuffer->GetData ();
	// Enough data already in the buffer for a request
	ASSERT (poBuffer->Read (poRead, cbInitialBuffer, TIMEOUT_READ)); // 4.5
	poBuffer->Discard (cbInitialBuffer);
	ASSERT (poBuffer->GetData () != pSave);
	// Must shift buffer to satisfy read request
	ASSERT (poBuffer->Read (poRead, cbInitialBuffer * 2, TIMEOUT_READ)); // 6.5
	ASSERT (poBuffer->GetData () == pSave);
	delete poRead;
#ifdef _WIN32
	// Remove the temp file
	ASSERT (DeleteFile (pszTemp));
	free (pszTemp);
#endif
}

/// Tests the functions and objects in Util/BufferedInput.cpp
BEGIN_TESTS (BufferedInputTest)
	TEST (EmptyState)
	TEST (ReadSequence)
END_TESTS
