/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_common_config_h
#define __inc_common_config_h

class CConfigEntry {
public:
	CConfigEntry () { }
	virtual ~CConfigEntry () { }
	virtual BOOL Read (PCSTR pszFilename, PCSTR pszSection) = 0;
};

class CConfigString : public CConfigEntry {
private:
	PCSTR m_pszParameter;
	PCSTR m_pszDefault;
	PSTR m_pszValue;
public:
	CConfigString (PCSTR pszParameter, PCSTR pszDefault);
	~CConfigString ();
	PCSTR GetValue () const { return m_pszValue ? m_pszValue : m_pszDefault; }
	BOOL Read (PCSTR pszFilename, PCSTR pszSection);
};

class CConfigMultiString : public CConfigEntry {
private:
	PCSTR m_pszCount;
	PCSTR m_pszParameter;
	UINT m_nValues;
	PSTR *m_ppszValues;
public:
	CConfigMultiString (PCSTR pszCount, PCSTR pszParameter);
	~CConfigMultiString ();
	UINT GetValueCount () const { return m_nValues; }
	PCSTR GetValue (UINT nIndex) const { return (nIndex < m_nValues) ? m_ppszValues[nIndex] : NULL; }
	BOOL Read (PCSTR pszFilename, PCSTR pszSection);
};

class CConfigSection {
private:
	PCSTR m_pszSection;
	UINT m_nEntries;
	CConfigEntry **m_ppEntries;
public:
	CConfigSection (PCSTR pszSection, UINT nEntries, CConfigEntry **ppEntries);
	BOOL Read (PCSTR pszFilename);
};

class CConfig {
private:
	UINT m_nSections;
	CConfigSection **m_ppSections;
public:
	CConfig (UINT nSections, CConfigSection **ppSections);
	BOOL Read (PCSTR pszFilename);
};

#endif /* ifndef __inc_common_config_h */