/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_common_param_h
#define __inc_common_param_h

class CParam {
private:
	PCSTR m_pszFlag;
public:
	CParam (PCSTR pszFlag);
	virtual ~CParam () { }
	virtual int ProcessExplicit (int nArgs, PCSTR *ppszArgs);
	virtual int ProcessImplied (int nArgs, PCSTR *ppszArgs) { return 0; }
	PCSTR GetFlag () { return m_pszFlag; }
};

class CAbstractParamFlag : public CParam {
protected:
	friend class CParamFlagInvert;
	virtual void SetValue (BOOL bValue) = 0;
public:
	CAbstractParamFlag (PCSTR pszFlag);
	int ProcessExplicit (int nArgs, PCSTR *ppszArgs);
	virtual BOOL IsSet () const = 0;
};

class CParamFlag : public CAbstractParamFlag {
private:
	BOOL m_bValue;
protected:
	void SetValue (BOOL bValue) { m_bValue = bValue; }
public:
	CParamFlag (PCSTR pszFlag);
	BOOL IsSet () const { return m_bValue; }
};

class CParamFlagInvert : public CAbstractParamFlag {
private:
	CAbstractParamFlag *m_pUnderlying;
protected:
	void SetValue (BOOL bValue) { m_pUnderlying->SetValue (!bValue); }
public:
	CParamFlagInvert (PCSTR pszFlag, CParamFlag *pUnderlying);
	BOOL IsSet () const { return !m_pUnderlying->IsSet (); }
};

class CParamString : public CParam {
private:
	PCSTR m_pszDefault;
	BOOL m_bImplied;
	PSTR m_pszValue;
public:
	CParamString (PCSTR pszFlag, PCSTR pszDefault, BOOL bImplied = FALSE);
	~CParamString ();
	int ProcessExplicit (int nArgs, PCSTR *ppszArgs);
	int ProcessImplied (int nArgs, PCSTR *ppszArgs);
	PCSTR GetString () const { return m_pszValue ? m_pszValue : m_pszDefault; }
};

class CParamInteger : public CParamString {
private:
	char m_szDefault[8];
public:
	CParamInteger (PCSTR pszFlag, int nDefault, BOOL bImplied = FALSE);
	int GetInteger () const { return atoi (GetString ()); }
};

class CParams {
private:
	UINT m_nParams;
	CParam **m_ppParams;
public:
	CParams (UINT nParams, CParam **ppParams);
	BOOL Process (PCWSTR pszCommandLine);
};

#endif /* ifndef __inc_common_param_h */