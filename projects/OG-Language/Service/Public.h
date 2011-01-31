/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_service_public_h
#define __inc_og_language_service_public_h

// Public interface for Service component

#define SERVICE_SETTINGS_CONNECTION_PIPE	TEXT ("connectionPipe")
#define SERVICE_DEFAULT_CONNECTION_PIPE		TEXT ("\\\\.\\pipe\\OpenGamma-ExcelPlugin-Connection")
#define SERVICE_SETTINGS_SERVICE_NAME		TEXT ("serviceName")
#define SERVICE_DEFAULT_SERVICE_NAME		TEXT ("OpenGammaLanguageAPI")

#pragma pack (push, 4)
typedef struct {
	fudge_i32 cbSize;
	fudge_i32 cbChar;
	fudge_i32 cchUserNameOffset;
	fudge_i32 cchCPPToJavaPipeOffset;
	fudge_i32 cchJavaToCPPPipeOffset;
} JAVACLIENT_CONNECT, *PJAVACLIENT_CONNECT;
#pragma pack (pop)

__inline const TCHAR *JavaClientGetUserName (PJAVACLIENT_CONNECT pjcc) {
	return (const TCHAR*)(pjcc + 1) + pjcc->cchUserNameOffset;
}

__inline const TCHAR *JavaClientGetCPPToJavaPipe (PJAVACLIENT_CONNECT pjcc) {
	return (const TCHAR*)(pjcc + 1) + pjcc->cchCPPToJavaPipeOffset;
}

__inline const TCHAR *JavaClientGetJavaToCPPPipe (PJAVACLIENT_CONNECT pjcc) {
	return (const TCHAR*)(pjcc + 1) + pjcc->cchJavaToCPPPipeOffset;
}

#define JAVACLIENTCREATE_FUNCTION(_mode_, _strtype_, _strlen_, _chartype_) \
__inline PJAVACLIENT_CONNECT JavaClientCreate##_mode_ (_strtype_ pszUserName, _strtype_ pszCPPToJavaPipe, _strtype_ pszJavaToCPPPipe) { \
	DWORD cchUserName = _strlen_ (pszUserName); \
	DWORD cchCPPToJavaPipe = _strlen_ (pszCPPToJavaPipe); \
	DWORD cchJavaToCPPPipe = _strlen_ (pszJavaToCPPPipe); \
	DWORD cbSize = sizeof (JAVACLIENT_CONNECT) + (cchUserName + cchCPPToJavaPipe + cchJavaToCPPPipe + 3) * sizeof (_chartype_); \
	PJAVACLIENT_CONNECT pjcc = (PJAVACLIENT_CONNECT)malloc (cbSize); \
	if (pjcc) { \
		pjcc->cbSize = cbSize; \
		pjcc->cbChar = sizeof (_chartype_); \
		pjcc->cchUserNameOffset = 0; \
		memcpy ((pjcc + 1), pszUserName, (cchUserName + 1) * sizeof (_chartype_)); \
		DWORD dwOfs = cchUserName + 1; \
		pjcc->cchCPPToJavaPipeOffset = dwOfs; \
		memcpy ((_chartype_*)(pjcc + 1) + dwOfs, pszCPPToJavaPipe, (cchCPPToJavaPipe + 1) * sizeof (_chartype_)); \
		dwOfs += cchCPPToJavaPipe + 1; \
		pjcc->cchJavaToCPPPipeOffset = dwOfs; \
		memcpy ((_chartype_*)(pjcc + 1) + dwOfs, pszJavaToCPPPipe, (cchJavaToCPPPipe + 1) * sizeof (_chartype_)); \
	} \
	return pjcc; \
}

JAVACLIENTCREATE_FUNCTION (A, PCSTR, strlen, char)
JAVACLIENTCREATE_FUNCTION (W, PCWSTR, wcslen, wchar_t)

#ifdef _UNICODE
#define JavaClientCreate JavaClientCreateW
#else
#define JavaClientCreate JavaClientCreateA
#endif

#endif /* ifndef __inc_og_language_service_public_h */