/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <Windows.h>
#include <tchar.h>
#include <strsafe.h>
#include "jvm.h"

#define INI_ARGUMENTS		"Arguments"
#define INI_ARGUMENTS_COUNT	"count"
#define INI_ARGUMENTS_ARG	"arg%d"
#define INI_CLASSPATH		"Classpath"
#define INI_CLASSPATH_COUNT	"count"
#define INI_CLASSPATH_PATH	"path%d"
#define INI_INVOKE			"Invoke"
#define INI_INVOKE_CLASS	"class"
#define INI_INVOKE_MAIN		"main"
#define INI_INVOKE_STOP		"stop"
#define INI_OPTIONS			"Options"
#define INI_OPTIONS_COUNT	"count"
#define INI_OPTIONS_OPT		"opt%d"

typedef jint (JNICALL *JNI_CREATEJAVAVMPROC) (JavaVM **ppjvm, JNIEnv **ppenv, JavaVMInitArgs *pArgs);

struct _multi_string {
	int n;
	char **ppsz;
};

static struct _multi_string g_msArguments;
static struct _multi_string g_msClasspath;
static struct _multi_string g_msOptions;
static HMODULE g_hJVM;
static JavaVM *g_pjvm;
static JNIEnv *g_penv;
static char *g_pszMainClass;
static char *g_pszMainMethod;
static char *g_pszStopMethod;

static BOOL _ReadMultiString (struct _multi_string *pms, const char *pszFilename, const char *pszSection, const char *pszCount, const char *pszItem) {
	int n;
	char szKey[16];
	char szValue[256];
	pms->n = GetPrivateProfileInt (pszSection, pszCount, 0, pszFilename);
	if (pms->n == 0) return TRUE;
	pms->ppsz = (char**)malloc (sizeof (char*) * pms->n);
	if (pms->ppsz == NULL) return FALSE;
	for (n = 0; n < pms->n; n++) {
		StringCbPrintf (szKey, sizeof (szKey), pszItem, n);
		GetPrivateProfileString (pszSection, szKey, NULL, szValue, sizeof (szValue), pszFilename);
		pms->ppsz[n] = _strdup (szValue);
		if (pms->ppsz[n] == NULL) return FALSE;
	}
	return TRUE;
}

/// Reads the contents of the configuration INI file and populates the global variables.
///
/// @param[in] pszFilename name of the INI file to read
/// @return TRUE if the file was read, FALSE if there was a problem
BOOL ReadConfigurationFile (const char *pszFilenameArg) {
	BOOL bResult = FALSE;
	char szValue[256];
	const char *pszFilename;
	char *pszFilenameNew = NULL;
	do {
		if (*pszFilenameArg == '\"') {
			size_t cch = strlen (pszFilenameArg + 1) - 1;
			pszFilenameNew = (char*)malloc (cch + 1);
			if (!pszFilenameNew) return FALSE;
			memcpy (pszFilenameNew, pszFilenameArg + 1, cch);
			pszFilenameNew[cch] = 0;
			pszFilename = pszFilenameNew;
		} else {
			pszFilename = pszFilenameArg;
		}
		if (!_ReadMultiString (&g_msArguments, pszFilename, INI_ARGUMENTS, INI_ARGUMENTS_COUNT, INI_ARGUMENTS_ARG)) break;
		if (!_ReadMultiString (&g_msClasspath, pszFilename, INI_CLASSPATH, INI_CLASSPATH_COUNT, INI_CLASSPATH_PATH)) break;
		if (!_ReadMultiString (&g_msOptions, pszFilename, INI_OPTIONS, INI_OPTIONS_COUNT, INI_OPTIONS_OPT)) break;
		GetPrivateProfileString (INI_INVOKE, INI_INVOKE_CLASS, "com/opengamma/util/examples/MainRunner", szValue, sizeof (szValue), pszFilename);
		g_pszMainClass = _strdup (szValue);
		if (!g_pszMainClass) break;
		GetPrivateProfileString (INI_INVOKE, INI_INVOKE_MAIN, "main", szValue, sizeof (szValue), pszFilename);
		g_pszMainMethod = _strdup (szValue);
		if (!g_pszMainMethod) break;
		GetPrivateProfileString (INI_INVOKE, INI_INVOKE_STOP, "stop", szValue, sizeof (szValue), pszFilename);
		g_pszStopMethod = _strdup (szValue);
		if (!g_pszStopMethod) break;
		bResult = TRUE;
	} while (FALSE);
	if (pszFilenameNew) free (pszFilenameNew);
	return bResult;
}

static BOOL _findJavaFromRegistry (PCTSTR pszPublisher) {
	TCHAR szKey[MAX_PATH];
	TCHAR szPath[MAX_PATH];
	DWORD cbPath;
	HANDLE hFile;
	int n;
	size_t i;
	StringCbPrintf (szKey, sizeof (szKey), TEXT ("SOFTWARE\\%s\\Java Runtime Environment\\1.6"), pszPublisher);
	cbPath = sizeof (szPath);
	if (RegGetValue (HKEY_LOCAL_MACHINE, szKey, TEXT ("RuntimeLib"), RRF_RT_REG_SZ, NULL, szPath, &cbPath) != ERROR_SUCCESS) return FALSE;
	hFile = CreateFile (szPath, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
	if (hFile == INVALID_HANDLE_VALUE) {
		size_t cchPath = _tcslen (szPath);
		if ((cchPath > 15) && !_tcscmp (szPath + cchPath - 15, TEXT ("\\client\\jvm.dll"))) {
			memcpy (szPath + cchPath - 14, TEXT ("server"), sizeof (TCHAR) * 6);
			hFile = CreateFile (szPath, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
			if (hFile == INVALID_HANDLE_VALUE) return FALSE;
		} else {
			return FALSE;
		}
	}
	CloseHandle (hFile);
	i = strlen (szPath);
	n = 2;
	while (--i > 0) {
		if (szPath[i] == '\\') {
			if (!--n) {
				szPath[i] = 0;
				break;
			}
		}
	}
	SetDllDirectory (szPath);
	szPath[i] = '\\';
	g_hJVM = LoadLibraryEx (szPath, NULL, LOAD_WITH_ALTERED_SEARCH_PATH);
	SetDllDirectory (NULL);
	return (g_hJVM != INVALID_HANDLE_VALUE);
}

/// Locates and loads the JVM library DLL.
///
/// @return TRUE if a JVM was found, FALSE if there was a problem
BOOL FindJava () {
	return _findJavaFromRegistry (TEXT ("OpenGammaLtd")) || _findJavaFromRegistry (TEXT ("JavaSoft"));
}

static BOOL _isFolderExpansion (const char *pszPath) {
	size_t cchPath = strlen (pszPath);
	return (pszPath[cchPath - 2] == '\\') && (pszPath[cchPath - 1] == '*');
}

static char *_createClasspathOption () {
	int n;
	size_t cchLength = 18, cch = 0;
	char *pszOption;
	char sz[MAX_PATH];
	WIN32_FIND_DATAA wfd;
	HANDLE hFind;
	size_t cchPath;
	for (n = 0; n < g_msClasspath.n; n++) {
		if (_isFolderExpansion (g_msClasspath.ppsz[n])) {
			StringCbPrintf (sz, sizeof (sz), "%s.*", g_msClasspath.ppsz[n]);
			cchPath = strlen (g_msClasspath.ppsz[n]);
			hFind = FindFirstFile (sz, &wfd);
			if (hFind) {
				do {
					if (!(wfd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)) {
						cchLength += 2 + cchPath + strlen (wfd.cFileName);
					}
				} while (FindNextFile (hFind, &wfd));
				FindClose (hFind);
			}
		} else {
			cchLength += 1 + strlen (g_msClasspath.ppsz[n]);
		}
	}
	pszOption = (char*)malloc (cchLength);
	if (!pszOption) return NULL;
	StringCchCopy (pszOption, cchLength, "-Djava.class.path");
	cch += 17;
	for (n = 0; n < g_msClasspath.n; n++) {
		if (_isFolderExpansion (g_msClasspath.ppsz[n])) {
			StringCbPrintf (sz, sizeof (sz), "%s.*", g_msClasspath.ppsz[n]);
			cchPath = strlen (g_msClasspath.ppsz[n]);
			hFind = FindFirstFile (sz, &wfd);
			if (hFind) {
				do {
					if (!(wfd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)) {
						StringCchCopy (pszOption + cch, cchLength - cch, n ? ";" : "=");
						cch++;
						StringCchCopy (pszOption + cch, cchLength - cch, g_msClasspath.ppsz[n]);
						cch += cchPath - 2;
						StringCchCopy (pszOption + cch, cchLength - cch, "\\");
						cch++;
						StringCchCopy (pszOption + cch, cchLength - cch, wfd.cFileName);
						cch += strlen (wfd.cFileName);
					}
				} while (FindNextFile (hFind, &wfd));
				FindClose (hFind);
			}
		} else {
			StringCchCopy (pszOption + cch, cchLength - cch, n ? ";" : "=");
			cch++;
			StringCchCopy (pszOption + cch, cchLength - cch, g_msClasspath.ppsz[n]);
			cch += strlen (g_msClasspath.ppsz[n]);
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
		return strdup (pszOption);
	}
}

/// Creates a Java VM instance.
///
/// @return TRUE if the VM was created, FALSE if there was a problem
BOOL CreateJavaVM () {
	int n;
	BOOL bResult = FALSE;
	JNI_CREATEJAVAVMPROC fnCreateJavaVM;
	JavaVMInitArgs args;
	JavaVMOption *poptions = NULL;
	do {
		poptions = (JavaVMOption*)malloc (sizeof (JavaVMOption) * (1 + g_msOptions.n));
		if (!poptions) break;
		ZeroMemory (poptions, sizeof (JavaVMOption) * (1 + g_msOptions.n));
		fnCreateJavaVM = (JNI_CREATEJAVAVMPROC)GetProcAddress (g_hJVM, "JNI_CreateJavaVM");
		if (!fnCreateJavaVM) break;
		args.version = JNI_VERSION_1_6;
		args.nOptions = 1 + g_msOptions.n;
		args.options = poptions;
		poptions[0].optionString = _createClasspathOption ();
		for (n = 0; n < g_msOptions.n; n++) {
			poptions[n + 1].optionString = _createOption (g_msOptions.ppsz[n]);
			if (!poptions[n + 1].optionString) break;
		}
#ifdef _DEBUG
		for (n = 0; n < args.nOptions; n++) {
			printf ("%d: %s\n", n, args.options[n].optionString);
		}
#endif /* ifdef _DEBUG */
		args.ignoreUnrecognized = JNI_TRUE;
		if (fnCreateJavaVM (&g_pjvm, &g_penv, &args)) break;
		bResult = TRUE;
	} while (FALSE);
	if (poptions) {
		for (n = 0; n < args.nOptions; n++) {
			if (args.options[n].optionString) free (args.options[n].optionString);
		}
		free (poptions);
	}
	return bResult;
}

/// Calls the main method on a class. This must be called from the same thread that created the VM.
///
/// @param[in] pszClass the class name to invoke (e.g. com/opengamma/util/Foo) or NULL for the value
///            from the INI file or default (MainRunner)
/// @return TRUE if the method was invoked, FALSE if there was a problem
BOOL InvokeMain (const char *pszClass) {
	int n;
	jclass clsMain;
	jclass clsString;
	jmethodID mtdMain;
	jobjectArray oaArgs;
	clsMain = (*g_penv)->FindClass (g_penv, pszClass ? pszClass : g_pszMainClass);
	if (!clsMain) return FALSE;
	clsString = (*g_penv)->FindClass (g_penv, "java/lang/String");
	if (!clsString) return FALSE;
	mtdMain = (*g_penv)->GetStaticMethodID (g_penv, clsMain, g_pszMainMethod, "([Ljava/lang/String;)V");
	if (!mtdMain) return FALSE;
	(*g_penv)->PushLocalFrame (g_penv, g_msArguments.n + 1);
	oaArgs = (*g_penv)->NewObjectArray (g_penv, g_msArguments.n, clsString, NULL);
	for (n = 0; n < g_msArguments.n; n++) {
		(*g_penv)->SetObjectArrayElement (g_penv, oaArgs, n, (*g_penv)->NewStringUTF (g_penv, g_msArguments.ppsz[n]));
	}
	(*g_penv)->CallStaticVoidMethod (g_penv, clsMain, mtdMain, oaArgs);
	(*g_penv)->PopLocalFrame (g_penv, NULL);
	return TRUE;
}

/// Calls the stop method on a class. This must be called from a different thread to the one currently calling
/// the main method.
///
/// @param[in] pszClass the class name to invoke (e.g. com/opengamma/util/Foo) or NULL for the value
///            from the INI file or default (MainRunner)
/// @return TRUE if the method was invoked, FALSE if there was a problem
BOOL InvokeStop (const char *pszClass) {
	jclass clsStop;
	jmethodID mtdStop;
	JavaVMAttachArgs args;
	JNIEnv *penv;
	args.group = 0;
	args.name = (char*)"Stop thread";
	args.version = JNI_VERSION_1_6;
	(*g_pjvm)->AttachCurrentThread (g_pjvm, (void**)&penv, &args);
	clsStop = (*penv)->FindClass (penv, pszClass ? pszClass : g_pszMainClass);
	if (!clsStop) return FALSE;
	mtdStop = (*penv)->GetStaticMethodID (penv, clsStop, g_pszStopMethod, "()V");
	if (!mtdStop) return FALSE;
	(*penv)->CallStaticVoidMethod (penv, clsStop, mtdStop, NULL);
	return TRUE;
}
