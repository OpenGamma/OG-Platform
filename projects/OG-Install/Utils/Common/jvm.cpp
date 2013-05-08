/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <Windows.h>
#include <strsafe.h>
#include "jvm.h"
#include "errorref.h"

typedef jint (JNICALL *JNI_CREATEJAVAVMPROC) (JavaVM **ppjvm, JNIEnv **ppenv, JavaVMInitArgs *pArgs);

static CConfigMultiString g_oClasspathFolders ("count", "path%d");
static CConfigEntry *g_apoClasspathSection[1] = { &g_oClasspathFolders };
static CConfigSection g_oClasspathSection ("Classpath", 1, g_apoClasspathSection);
static CConfigMultiString g_oOptionStrings ("count", "opt%d");
static CConfigEntry *g_apoOptionSection[1] = { &g_oOptionStrings };
static CConfigSection g_oOptionSection ("Options", 1, g_apoOptionSection);
static CConfigSection *g_apoConfig[2] = { &g_oClasspathSection, &g_oOptionSection };
CConfig CJavaRT::s_oConfig (sizeof (g_apoConfig) / sizeof (*g_apoConfig), g_apoConfig);

CJavaVM::CJavaVM (const CJavaVM *poRoot, JNIEnv *penv) {
	m_dwRefCount = 1;
	poRoot->AddRef ();
	m_poRoot = poRoot;
	m_pjvm = poRoot->m_pjvm;
	m_penv = penv;
}

CJavaVM::CJavaVM (JavaVM *pjvm, JNIEnv *penv) {
	m_dwRefCount = 1;
	m_poRoot = NULL;
	m_pjvm = pjvm;
	m_penv = penv;
}

CJavaVM::~CJavaVM () {
	if (m_poRoot) {
		// TODO: detach the thread
		Release (m_poRoot);
	} else {
		// TODO: destroy the JVM
	}
}

void CJavaVM::Release (const CJavaVM *po) {
	if (po) {
		if (InterlockedDecrement (&po->m_dwRefCount) == 0) {
			delete po;
		}
	}
}

void CJavaVM::AddRef () const {
	InterlockedIncrement (&m_dwRefCount);
}

static jclass _findClass (JNIEnv *penv, PCSTR pszClass) {
	if (!pszClass) {
		return NULL;
	}
	return penv->FindClass (pszClass);
}

static jclass _findClass (JNIEnv *penv, const CConfigString *poClass) {
	if (!poClass) {
		return NULL;
	}
	return _findClass (penv, poClass->GetValue ());
}

static jmethodID _findMethod (JNIEnv *penv, jclass cls, const CConfigString *poMethod, PCSTR pszSignature) {
	if (!poMethod) {
		return NULL;
	}
	PCSTR pszMethod = poMethod->GetValue ();
	if (!pszMethod) {
		return NULL;
	}
	return penv->GetStaticMethodID (cls, pszMethod, pszSignature);
}

DWORD CJavaVM::Invoke (const CConfigString *poClass, const CConfigString *poMethod) const {
	jclass cls = _findClass (m_penv, poClass);
	if (!cls) return ERROR_REF_JVM;
	jmethodID mtd = _findMethod (m_penv, cls, poMethod, "()V");
	if (!mtd) return ERROR_REF_JVM;
	m_penv->CallStaticVoidMethod (cls, mtd, NULL);
	return 0;
}

DWORD CJavaVM::Invoke (const CConfigString *poClass, const CConfigString *poMethod, const CConfigMultiString *poArgs) const {
	jclass clsCall = _findClass (m_penv, poClass);
	if (!clsCall) return ERROR_REF_JVM;
	jclass clsString = _findClass (m_penv, "java/lang/String");
	if (!clsString) return ERROR_REF_JVM;
	jmethodID mtd = _findMethod (m_penv, clsCall, poMethod, "([Ljava/lang/String;)V");
	if (!mtd) return ERROR_REF_JVM;
	m_penv->PushLocalFrame (poArgs->GetValueCount () + 1); // array + string array members
	DWORD dwResult;
	jobjectArray oaArgs = m_penv->NewObjectArray (poArgs->GetValueCount (), clsString, NULL);
	if (!oaArgs) {
		dwResult = ERROR_REF_JVM;
		goto popAndReturn;
	}
	UINT n;
	for (n = 0; n < poArgs->GetValueCount (); n++) {
		jobject oParameter = m_penv->NewStringUTF (poArgs->GetValue (n));
		if (!oParameter) {
			dwResult = ERROR_REF_JVM;
			goto popAndReturn;
		}
		m_penv->SetObjectArrayElement (oaArgs, n, oParameter);
	}
	m_penv->CallStaticVoidMethod (clsCall, mtd, oaArgs);
	dwResult = 0;
popAndReturn:
	m_penv->PopLocalFrame (NULL);
	return dwResult;
}

DWORD CJavaVM::RegisterNatives (PCSTR pszClass, int nMethods, JNINativeMethod *aMethods) const {
	jclass cls = _findClass (m_penv, pszClass);
	if (!cls) return ERROR_REF_JVM;
    return m_penv->RegisterNatives (cls, aMethods, nMethods) ? ERROR_REF_JVM : 0;
}

CJavaVM *CJavaVM::Attach (PCSTR pszThreadName) const {
	if (m_poRoot) {
		return m_poRoot->Attach (pszThreadName);
	} else {
		JavaVMAttachArgs args;
		JNIEnv *penv;
		args.group = 0;
		args.name = (char*)pszThreadName;
		args.version = JNI_VERSION_1_6;
		if (m_pjvm->AttachCurrentThread ((void**)&penv, &args)) {
			return NULL;
		}
		return new CJavaVM (this, penv);
	}
}

CJavaRT::CJavaRT (HMODULE hModule) {
	m_hDll = hModule;
}

CJavaRT::~CJavaRT () {
	FreeLibrary (m_hDll);
}

static HMODULE _findJavaFromRegistry (HKEY hkeyPublisher, PCSTR pszVersion) {
	HANDLE hFile = INVALID_HANDLE_VALUE;
	HMODULE hModule = (HMODULE)INVALID_HANDLE_VALUE;
	do {
		char sz[MAX_PATH];
		DWORD cb = sizeof (sz);
		if (RegGetValue (hkeyPublisher, pszVersion, "RuntimeLib", RRF_RT_REG_SZ, NULL, sz, &cb) != ERROR_SUCCESS) break;
		hFile = CreateFile (sz, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
		size_t cch = strlen (sz);
		if (hFile == INVALID_HANDLE_VALUE) {
			// JRE 1.7, 32-bit doesn't seem to have this bug but 64-bit does.
			if ((cch > 15) && !strcmp (sz + cch - 15, "\\client\\jvm.dll")) {
				memcpy (sz + cch - 14, "server", 6);
				hFile = CreateFile (sz, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
				if (hFile == INVALID_HANDLE_VALUE) break;
			} else {
				break;
			}
		}
		int n = 2;
		while (--cch > 0) {
			if (sz[cch] == '\\') {
				if (!--n) {
					sz[cch] = 0;
					break;
				}
			}
		}
		SetDllDirectory (sz);
		sz[cch] = '\\';
		hModule = LoadLibraryEx (sz, NULL, LOAD_WITH_ALTERED_SEARCH_PATH);
		SetDllDirectory (NULL);
	} while (FALSE);
	if (hFile) CloseHandle (hFile);
	return hModule;
}

static int _VersionFragment (PCSTR pszVersion, int nFragment) {
	int nValue;
	size_t cch = strlen (pszVersion), i = 0;
	do {
		nValue = 0;
		while ((i < cch) && (pszVersion[i] != '.')) {
			if ((pszVersion[i] >= '0') && (pszVersion[i] <= '9')) {
				nValue = (nValue * 10) + (pszVersion[i] - '0');
			}
			i++;
		}
		if (i < cch) i++;
	} while (nFragment-- > 0);
	return nValue;
}

#define JAVA_MINIMUM_MAJOR_VERSION	1
#define JAVA_MINIMUM_MINOR_VERSION	7

static HMODULE _findJavaFromRegistry (PCSTR pszPublisher) {
	HMODULE hModule = (HMODULE)INVALID_HANDLE_VALUE;
	HKEY hKey = NULL;
	do {
		char sz[MAX_PATH];
		StringCbPrintf (sz, sizeof (sz), "SOFTWARE\\%s\\Java Runtime Environment", pszPublisher);
		if (RegOpenKey (HKEY_LOCAL_MACHINE, sz, &hKey) != ERROR_SUCCESS) break;
		DWORD cb = sizeof (sz);
		if (RegGetValue (hKey, NULL, "CurrentVersion", RRF_RT_REG_SZ, NULL, sz, &cb) != ERROR_SUCCESS) break;
		int nMajorVersion = _VersionFragment (sz, 0);
		if (nMajorVersion == JAVA_MINIMUM_MAJOR_VERSION) {
			if (_VersionFragment (sz, 1) >= JAVA_MINIMUM_MINOR_VERSION) {
				hModule = _findJavaFromRegistry (hKey, sz);
			}
		} else if (nMajorVersion > JAVA_MINIMUM_MAJOR_VERSION) {
			hModule = _findJavaFromRegistry (hKey, sz);
		}
		if (hModule) break;
		cb = sizeof (sz);
		if (RegGetValue (hKey, NULL, "Java7FamilyVersion", RRF_RT_REG_SZ, NULL, sz, &cb) != ERROR_SUCCESS) break;
		hModule = _findJavaFromRegistry (hKey, sz);
		if (hModule) break;
		hModule = _findJavaFromRegistry (hKey, "1.7");
	} while (FALSE);
	if (hKey) RegCloseKey (hKey);
	return hModule;
}

CJavaRT *CJavaRT::Init () {
	HMODULE hModule = _findJavaFromRegistry ("OpenGammaLtd");
	if (hModule == INVALID_HANDLE_VALUE) {
		hModule = _findJavaFromRegistry ("JavaSoft");
		if (hModule == INVALID_HANDLE_VALUE) {
			return NULL;
		}
	}
	return new CJavaRT (hModule);
}

static BOOL _isFolderExpansion (const char *pszPath) {
	size_t cchPath = strlen (pszPath);
	return (pszPath[cchPath - 2] == '\\') && (pszPath[cchPath - 1] == '*');
}

static size_t _countClasspathLength (const char *pszClasspath) {
	size_t cchPath = strlen (pszClasspath);
	size_t cchLength = 0;
	char sz[MAX_PATH];
	WIN32_FIND_DATAA wfd;
	HANDLE hFind;
	StringCbPrintf (sz, sizeof (sz), "%s\\*.*", pszClasspath);
	hFind = FindFirstFile (sz, &wfd);
	if (hFind) {
		do {
			if (wfd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) {
				if (wfd.cFileName[0] != '.') {
					StringCbPrintf (sz + cchPath + 1, sizeof (sz) - sizeof (char) * (cchPath + 1), "%s", wfd.cFileName);
					cchLength += _countClasspathLength (sz);
				}
			} else {
				cchLength += 2 + cchPath + strlen (wfd.cFileName);
			}
		} while (FindNextFile (hFind, &wfd));
		FindClose (hFind);
	}
	return cchLength;
}

static void _getClasspath (const char *pszClasspath, char *pszOption, size_t cchLength, size_t *pcch) {
	size_t cchPath = strlen (pszClasspath);
	char sz[MAX_PATH];
	WIN32_FIND_DATAA wfd;
	HANDLE hFind;
	StringCbPrintf (sz, sizeof (sz), "%s\\*.*", pszClasspath);
	hFind = FindFirstFile (sz, &wfd);
	if (hFind) {
		do {
			if (wfd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) {
				if (wfd.cFileName[0] != '.') {
					StringCbPrintf (sz + cchPath + 1, sizeof (sz) - sizeof (char) * (cchPath + 1), "%s", wfd.cFileName);
					_getClasspath (sz, pszOption, cchLength, pcch);
				}
			} else {
				StringCchCopy (pszOption + *pcch, cchLength - *pcch, (*pcch > 17) ? ";" : "=");
				(*pcch)++;
				StringCchCopy (pszOption + *pcch, cchLength - *pcch, pszClasspath);
				(*pcch) += cchPath;
				StringCchCopy (pszOption + *pcch, cchLength - *pcch, "\\");
				(*pcch)++;
				StringCchCopy (pszOption + *pcch, cchLength - *pcch, wfd.cFileName);
				(*pcch) += strlen (wfd.cFileName);
			}
		} while (FindNextFile (hFind, &wfd));
		FindClose (hFind);
	}
}

static char *_createClasspathOption () {
	UINT n;
	size_t cchLength = 18;
	char *pszOption;
	char sz[MAX_PATH];
	size_t cchPath;
	for (n = 0; n < g_oClasspathFolders.GetValueCount (); n++) {
		PCSTR pszClasspath = g_oClasspathFolders.GetValue (n);
		if (!pszClasspath) {
			continue;
		}
		cchPath = strlen (pszClasspath);
		if (_isFolderExpansion (pszClasspath)) {
			StringCbCopy (sz, sizeof (sz), pszClasspath);
			sz[cchPath - 2] = 0;
			cchLength += _countClasspathLength (sz);
		} else {
			cchLength += 1 + cchPath;
		}
	}
	pszOption = (char*)malloc (cchLength);
	if (!pszOption) return NULL;
	StringCchCopy (pszOption, cchLength, "-Djava.class.path");
	size_t cch = 17;
	for (n = 0; n < g_oClasspathFolders.GetValueCount (); n++) {
		PCSTR pszClasspath = g_oClasspathFolders.GetValue (n);
		if (!pszClasspath) {
			continue;
		}
		cchPath = strlen (pszClasspath);
		if (_isFolderExpansion (pszClasspath)) {
			StringCbCopy (sz, sizeof (sz), pszClasspath);
			sz[cchPath - 2] = 0;
			_getClasspath (sz, pszOption, cchLength, &cch);
		} else {
			StringCchCopy (pszOption + cch, cchLength - cch, (cch > 17) ? ";" : "=");
			cch++;
			StringCchCopy (pszOption + cch, cchLength - cch, pszClasspath);
			cch += cchPath;
		}
	}
	return pszOption;
}

static char *_fixupMemoryOption (const char *pszMemory, const char *pszPrefix) {
	char *pszResult;
	size_t cchMemory = strlen (pszMemory);
	size_t cchPrefix = strlen (pszPrefix);
	if (pszMemory[cchMemory - 1] == 'P') {
		size_t nPercent = atoi (pszMemory), nMinimum, nMaximum;
		const char *psz = strchr (pszMemory, ':');
		MEMORYSTATUS ms;
		if (!psz) return NULL;
		nMinimum = atoi (psz + 1);
		psz = strchr (psz + 1, ':');
		if (!psz) return NULL;
		nMaximum = atoi (psz + 1);
		GlobalMemoryStatus (&ms);
		nPercent = ((ms.dwAvailPhys >> 20) * nPercent) / 100;
		if (nPercent < nMinimum) {
			nPercent = nMinimum;
		} else if (nPercent > nMaximum) {
			nPercent = nMaximum;
		}
		pszResult = (char*)malloc (cchPrefix + 8);
		StringCbPrintf (pszResult, cchPrefix + 8, "%s%dM", pszPrefix, nPercent);
	} else {
		pszResult = (char*)malloc (cchMemory + cchPrefix + 1);
		if (pszResult) {
			memcpy (pszResult, pszPrefix, cchPrefix);
			memcpy (pszResult + cchPrefix, pszMemory, cchMemory + 1);
		}
	}
	return pszResult;
}

static char *_createOption (const char *pszOption) {
	if (!strncmp (pszOption, "-Xms", 4)) {
		return _fixupMemoryOption (pszOption + 4, "-Xms");
	} else if (!strncmp (pszOption, "-Xmx", 4)) {
		return _fixupMemoryOption (pszOption + 4, "-Xmx");
	} else {
		return _strdup (pszOption);
	}
}

CJavaVM *CJavaRT::CreateVM () const {
	UINT n;
	JNI_CREATEJAVAVMPROC fnCreateJavaVM;
	JavaVMInitArgs args;
	JavaVMOption *poptions = NULL;
	JavaVM *pjvm = NULL;
	JNIEnv *penv = NULL;
	do {
		poptions = (JavaVMOption*)malloc (sizeof (JavaVMOption) * (1 + g_oOptionStrings.GetValueCount ()));
		if (!poptions) break;
		ZeroMemory (poptions, sizeof (JavaVMOption) * (1 + g_oOptionStrings.GetValueCount ()));
		fnCreateJavaVM = (JNI_CREATEJAVAVMPROC)GetProcAddress (m_hDll, "JNI_CreateJavaVM");
		if (!fnCreateJavaVM) break;
		args.version = JNI_VERSION_1_6;
		args.nOptions = 1 + g_oOptionStrings.GetValueCount ();
		args.options = poptions;
		poptions[0].optionString = _createClasspathOption ();
		for (n = 0; n < g_oOptionStrings.GetValueCount (); n++) {
			poptions[n + 1].optionString = _createOption (g_oOptionStrings.GetValue (n));
			if (!poptions[n + 1].optionString) break;
		}
		args.ignoreUnrecognized = JNI_TRUE;
		if (fnCreateJavaVM (&pjvm, &penv, &args)) {
			pjvm = NULL;
			break;
		}
	} while (FALSE);
	if (poptions) {
		for (n = 0; n < (UINT)args.nOptions; n++) {
			delete args.options[n].optionString;
		}
		delete poptions;
	}
	if (pjvm) {
		return new CJavaVM (pjvm, penv);
	} else {
		return NULL;
	}
}
