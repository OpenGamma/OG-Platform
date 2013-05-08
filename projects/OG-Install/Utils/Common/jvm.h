/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_common_jvm_h
#define __inc_common_jvm_h

#include "config.h"
#include <jni.h>

class CJavaVM {
private:
	mutable volatile DWORD m_dwRefCount;
	const CJavaVM *m_poRoot;
	JavaVM *m_pjvm;
	JNIEnv *m_penv;
	CJavaVM (const CJavaVM *poParent, JNIEnv *penv);
	~CJavaVM ();
public:
	CJavaVM (JavaVM *pjvm, JNIEnv *penv);
	DWORD Invoke (const CConfigString *poClass, const CConfigString *poMethod) const;
	DWORD Invoke (const CConfigString *poClass, const CConfigString *poMethod, const CConfigMultiString *poArgs) const;
	DWORD RegisterNatives (PCSTR pszClass, int nMethods, JNINativeMethod *pMethods) const;
	CJavaVM *Attach (PCSTR pszThreadName) const;
	static void Release (const CJavaVM *po);
	void AddRef () const;
};

class CJavaRT {
private:
	HMODULE m_hDll;
public:
	static CConfig s_oConfig;
	CJavaRT (HMODULE hModule);
	~CJavaRT ();
	static CJavaRT *Init ();
	CJavaVM *CreateVM () const;
};

#endif /* ifndef __inc_common_jvm_h */
