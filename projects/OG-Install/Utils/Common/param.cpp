/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <Windows.h>
#include <strsafe.h>
#include "param.h"

CParam::CParam (PCSTR pszFlag) {
	m_pszFlag = pszFlag;
}

int CParam::ProcessExplicit (int nArgs, PCSTR *ppszArgs) {
	if (!m_pszFlag) return 0;
	if ((**ppszArgs == '-') || (**ppszArgs =='/')) {
		return strcmp (*ppszArgs + 1, m_pszFlag) ? 0 : 1;
	} else {
		return -1;
	}
}

CAbstractParamFlag::CAbstractParamFlag (PCSTR pszFlag)
: CParam (pszFlag) {
}

int CAbstractParamFlag::ProcessExplicit (int nArgs, PCSTR *ppszArgs) {
	int nTaken = CParam::ProcessExplicit (nArgs, ppszArgs);
	if (nTaken == 1) {
		SetValue (TRUE);
		return 1;
	} else {
		return 0;
	}
}

CParamFlag::CParamFlag (PCSTR pszFlag)
: CAbstractParamFlag (pszFlag) {
	m_bValue = FALSE;
}

CParamFlagInvert::CParamFlagInvert (PCSTR pszFlag, CParamFlag *pUnderlying)
: CAbstractParamFlag (pszFlag) {
	m_pUnderlying = pUnderlying;
}

CParamString::CParamString (PCSTR pszFlag, PCSTR pszDefault, BOOL bImplied)
: CParam (pszFlag) {
	m_pszDefault = pszDefault;
	m_pszValue = NULL;
	m_bImplied = bImplied;
}

CParamString::~CParamString () {
	delete m_pszValue;
}

int CParamString::ProcessExplicit (int nArgs, PCSTR *ppszArgs) {
	int nTaken = CParam::ProcessExplicit (nArgs, ppszArgs);
	if (nTaken > 0) {
		if (nArgs >= 2) {
			m_pszValue = _strdup (ppszArgs[nTaken++]);
			return nTaken;
		}
		return 0;
	} else {
		return (m_bImplied && !m_pszValue) ? nTaken : 0;
	}
}

int CParamString::ProcessImplied (int nArgs, PCSTR *ppszArgs) {
	m_pszValue = _strdup (*ppszArgs);
	return 1;
}

CParamInteger::CParamInteger (PCSTR pszFlag, int nDefault, BOOL bImplied)
: CParamString (pszFlag, m_szDefault, bImplied) {
	StringCbPrintf (m_szDefault, sizeof (m_szDefault), "%d", nDefault);
}

CParams::CParams (UINT nParams, CParam **ppParams) {
	m_nParams = nParams;
	m_ppParams = ppParams;
}

BOOL CParams::Process (PCWSTR pszCommandLine) {
	BOOL bResult = FALSE;
	int nArgs, nArg;
	UINT nParam;
	PWSTR *ppwzArgs = NULL;
	PCSTR *ppczArgs = NULL;
	do {
		ppwzArgs = CommandLineToArgvW (pszCommandLine, &nArgs);
		if (!ppwzArgs) break;
		ppczArgs = new PCSTR[nArgs];
		if (!ppczArgs) break;
		for (nArg = 0; nArg < nArgs; nArg++) {
			char sz[MAX_PATH];
			WideCharToMultiByte (CP_ACP, 0, ppwzArgs[nArg], -1, sz, sizeof (sz), NULL, NULL);
			ppczArgs[nArg] = _strdup (sz);
			if (!ppczArgs[nArg]) break;
		}
		if (nArg != nArgs) break;
		for (nArg = 1; nArg < nArgs; ) {
			int nTaken = 0;
			int nImplied = -1;
			for (nParam = 0; nParam < m_nParams; nParam++) {
				nTaken = m_ppParams[nParam]->ProcessExplicit (nArgs - nArg, ppczArgs + nArg);
				if (nTaken > 0) break;
				if ((nTaken < 0) && (nImplied < 0)) {
					nImplied = nParam;
				}
			}
			if (nTaken > 0) {
				nArg += nTaken;
			} else if (nImplied >= 0) {
				nTaken = m_ppParams[nImplied]->ProcessImplied (nArgs - nArg, ppczArgs + nArg);
				if (nTaken > 0) {
					nArg += nTaken;
				} else {
					break;
				}
			} else {
				break;
			}
		}
		if (nArg != nArgs) break;
		bResult = TRUE;
	} while (FALSE);
	if (ppczArgs) {
		for (nArg = 0; nArg < nArgs; nArg++) {
			delete ppczArgs[nArg];
		}
		delete ppczArgs;
	}
	if (ppwzArgs) LocalFree (ppwzArgs);
	return bResult;
}
