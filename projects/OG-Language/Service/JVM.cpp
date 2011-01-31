/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Start up an embedded JVM, and call methods on the "Main" class

#include "JVM.h"
#include "Service.h"
#include "Settings.h"

LOGGING(com.opengamma.svc.JVM);

//#define DESTROY_JVM /* If there are rogue threads, the JVM won't terminate gracefully so comment this line out */
#define MAIN_CLASS		"com/opengamma/excel/connector/Main"

typedef jint (JNICALL *JNI_CREATEJAVAVMPROC) (JavaVM **ppjvm, JNIEnv **ppEnv, JavaVMInitArgs *pArgs);

CJVM::CJVM (HMODULE hModule, JavaVM *pJVM, JNIEnv *pEnv) {
	LOGINFO (TEXT ("JVM created"));
	InitializeCriticalSection (&m_cs);
	m_hModule = hModule;
	m_pJVM = pJVM;
	m_pEnv = pEnv;
	m_hBusyTask = NULL;
	m_bRunning = FALSE;
}

CJVM::~CJVM () {
#ifdef DESTROY_JVM
	LOGDEBUG (TEXT ("Destroying JVM"));
	m_pJVM->DestroyJavaVM ();
	LOGINFO (TEXT ("JVM destroyed"));
#else /* ifdef DESTROY_JVM */
	LOGINFO (TEXT ("Destroying JVM implicitly through FreeLibrary call"));
#endif /* ifdef DESTROY_JVM */
	FreeLibrary (m_hModule);
	DeleteCriticalSection (&m_cs);
	if (m_hBusyTask) {
		CloseHandle (m_hBusyTask);
		m_hBusyTask = NULL;
	}
}

static void _SetAlternateDirectory (PCTSTR pszDLL) {
	PTSTR psz = _tcsdup (pszDLL);
	if (!psz) {
		LOGFATAL (TEXT ("Out of memory"));
		return;
	}
	int i = _tcslen (pszDLL), slashes = 2;
	while (--i > 0) {
		if (pszDLL[i] == '\\') {
			if (!--slashes) {
				psz[i] = 0;
				LOGDEBUG (TEXT ("DLL search path ") << psz);
				SetDllDirectory (psz);
				break;
			}
		}
	}
	free (psz);
}

static PSTR _OptionFudgeAnnotationCache (CSettings *pSettings) {
	PCTSTR pszCache = pSettings->GetAnnotationCache ();
	if (!pszCache) {
		LOGWARN (TEXT ("No path for Fudge annotation cache"));
		return NULL;
	}
	size_t cch = 34 + _tcslen (pszCache);
	PSTR pszOption = new CHAR[cch];
	if (!pszOption) {
		LOGFATAL (TEXT ("Out of memory"));
		return NULL;
	}
	StringCchPrintfA (pszOption, cch, "-Dfudgemsg.annotationCachePath=%ws", pszCache);
	LOGDEBUG ("Using " << pszOption);
	return pszOption;
}

static PSTR _BuildClasspath (PSTR pszBuffer, size_t *pcchUsed, size_t *pcchTotal, PCTSTR pszPath, PCTSTR pszFile) {
	LOGDEBUG (TEXT ("Appending ") << pszFile << TEXT (" to classpath"));
	size_t cchExtra = _tcslen (pszPath) + _tcslen (pszFile) + 2;
	if (*pcchUsed + cchExtra >= *pcchTotal) {
		size_t cchNeed = (*pcchUsed + cchExtra + 1) - *pcchTotal;
		if (cchNeed >= (*pcchTotal >> 3)) {
			LOGDEBUG (TEXT ("Need extra ") << cchNeed << TEXT (" incrementing by ") << (cchNeed << 1));
			*pcchTotal += cchNeed << 1;
		} else {
			LOGDEBUG (TEXT ("Need extra ") << cchNeed << TEXT (" incrementing by ") << (*pcchTotal >> 3));
			*pcchTotal += *pcchTotal >> 3;
		}
		LOGDEBUG (TEXT ("Reallocating classpath buffer to ") << (*pcchTotal) << TEXT (" chars"));
		PSTR pszNewBuffer = new CHAR[*pcchTotal];
		if (!pszNewBuffer) {
			LOGFATAL (TEXT ("Out of memory"));
			return pszBuffer;
		}
		memcpy (pszNewBuffer, pszBuffer, *pcchUsed * sizeof (char));
		free (pszBuffer);
		pszBuffer = pszNewBuffer;
	}
	StringCchPrintfA (pszBuffer + *pcchUsed, *pcchTotal - *pcchUsed, ";%ws\\%ws", pszPath, pszFile);
	*pcchUsed += cchExtra;
	return pszBuffer;
}

static PSTR _OptionClassPath (CSettings *pSettings) {
	PCTSTR pszPath = pSettings->GetJarPath ();
	if (!pszPath) {
		LOGWARN (TEXT ("No JAR folder available"));
		return NULL;
	}
	if (!_tcsncmp (pszPath, TEXT ("\\\\?\\"), 4)) {
		LOGDEBUG (TEXT ("Skipping \\\\?\\ prefix on JAR path"));
		pszPath += 4;
	}
	size_t cchUsed = 19 + _tcslen (pszPath);
	size_t cch = 32 * _tcslen (pszPath);
	PSTR pszOption = new CHAR[cch];
	if (!pszOption) {
		LOGFATAL (TEXT ("Out of memory"));
		return NULL;
	}
	StringCchPrintfA (pszOption, cch, "-Djava.class.path=%ws\\", pszPath);
	WIN32_FIND_DATA wfd;
	PTSTR pszSearch = new TCHAR[cchUsed];
	if (!pszSearch) {
		LOGFATAL (TEXT ("Out of memory"));
		delete pszOption;
		return NULL;
	}
	StringCchPrintf (pszSearch, cchUsed, TEXT ("%s\\*.*"), pszPath);
	HANDLE hFind = FindFirstFile (pszSearch, &wfd);
	delete pszSearch;
	if (hFind != NULL) {
		do {
			if (wfd.cFileName[0] == '.') {
				continue;
			}
			PCTSTR psz = _tcsrchr (wfd.cFileName, '.');
			if (!psz) {
				LOGDEBUG (TEXT ("Ignoring extensionless file ") << wfd.cFileName);
				continue;
			}
			if (_tcsicmp (psz, TEXT (".ear"))
			 && _tcsicmp (psz, TEXT (".jar"))
			 && _tcsicmp (psz, TEXT (".war"))
			 && _tcsicmp (psz, TEXT (".zip"))) {
				 LOGDEBUG (TEXT ("Ignoring non-JAR ") << wfd.cFileName);
				 continue;
			}
			pszOption = _BuildClasspath (pszOption, &cchUsed, &cch, pszPath, wfd.cFileName);
		} while (FindNextFile (hFind, &wfd));
		FindClose (hFind);
	} else {
		LOGWARN (TEXT ("Can't read folder ") << pszPath << TEXT (", error ") << GetLastError ());
	}
	LOGDEBUG ("Using " << pszOption << " (" << strlen (pszOption) << " chars)");
	return pszOption;
}

#pragma managed (push, off)
extern "C" {
JNIEXPORT void JNICALL Java_com_opengamma_excel_connector_Main_notifyStop (JNIEnv *pEnv, jclass cls) {
	LOGINFO (TEXT ("STOP called from JVM"));
	ServiceStop (FALSE);
}
}
#pragma managed (pop)

#pragma managed (push, off)
extern "C" {
JNIEXPORT void JNICALL Java_com_opengamma_excel_connector_Main_notifyPause (JNIEnv *pEnv, jclass cls) {
	LOGINFO (TEXT ("PAUSE called from JVM"));
	ServiceSuspend ();
}
}
#pragma managed (pop)

CJVM *CJVM::Create () {
	CSettings settings;
	PCTSTR pszDLL = settings.GetJvmLibrary ();
	LOGDEBUG (TEXT ("Loading library ") << pszDLL << TEXT (" and creating JVM"));
	_SetAlternateDirectory (pszDLL);
	HMODULE hModule = LoadLibraryEx (pszDLL, NULL, LOAD_WITH_ALTERED_SEARCH_PATH);
	SetDllDirectory (NULL);
	if (!hModule) {
		LOGWARN (TEXT ("Couldn't load ") << pszDLL << TEXT (", error ") << GetLastError ());
		return NULL;
	}
	JNI_CREATEJAVAVMPROC procCreateVM = (JNI_CREATEJAVAVMPROC)GetProcAddress (hModule, "JNI_CreateJavaVM");
	if (!procCreateVM) {
		LOGWARN (TEXT ("Couldn't find JNI_CreateJavaVM, error ") << GetLastError ());
		FreeLibrary (hModule);
		return NULL;
	}
	JavaVM *pJVM;
	JNIEnv *pEnv;
	JavaVMOption option[2];
	ZeroMemory (&option, sizeof (option));
	option[0].optionString = _OptionClassPath (&settings);
	option[1].optionString = _OptionFudgeAnnotationCache (&settings);
	// TODO: additional option strings from registry
	JavaVMInitArgs args;
	ZeroMemory (&args, sizeof (args));
	args.version = JNI_VERSION_1_6;
	args.options = option;
	args.nOptions = 2;
	jint err = procCreateVM (&pJVM, &pEnv, &args);
	if (option[0].optionString) {
		delete option[0].optionString;
	}
	if (option[1].optionString) {
		delete option[1].optionString;
	}
	if (err) {
		LOGWARN (TEXT ("Couldn't create JVM, error ") << err);
		FreeLibrary (hModule);
		return NULL;
	}
	CJVM *pJvm = new CJVM (hModule, pJVM, pEnv);
	if (!pJvm) {
		LOGFATAL (TEXT ("Out of memory"));
		return NULL;
	}
	if (!GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS | GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT, (LPCTSTR)Java_com_opengamma_excel_connector_Main_notifyStop, &hModule)) {
		LOGWARN (TEXT ("Couldn't get current module handle, error ") << GetLastError ());
		delete pJvm;
		return NULL;
	}
	TCHAR szFilename[MAX_PATH];
	if (!GetModuleFileName (hModule, szFilename, MAX_PATH)) {
		LOGWARN (TEXT ("Couldn't get current module filename, error ") << GetLastError ());
		delete pJVM;
		return NULL;
	}
	if (!_tcsncmp (szFilename, TEXT ("\\\\?\\UNC\\"), 8)) {
		LOGDEBUG (TEXT ("Removing \\\\?\\UNC\\ prefix from filename"));
		memmove (szFilename + 2, szFilename + 8, (_tcslen (szFilename + 8) + 1) * sizeof (TCHAR));
	} else if (!_tcsncmp (szFilename, TEXT ("\\\\?\\"), 4)) {
		LOGDEBUG (TEXT ("Removing \\\\?\\ prefix from filename"));
		memmove (szFilename, szFilename + 4, (_tcslen (szFilename + 4) + 1) * sizeof (TCHAR));
	}
	pEnv->PushLocalFrame (1);
#ifdef _UNICODE
	jstring jsPath = pEnv->NewString ((jchar*)szFilename, wcslen (szFilename));
#else
	jstring jsPath = pEnv->NewStringUtf (szFilename, strlen (szFilename));
#endif
	LOGDEBUG (TEXT ("Injecting library reference ") << szFilename << TEXT (" into JVM"));
	if (pJvm->Invoke (pEnv, "svcInitialise", "(Ljava/lang/String;)Z", jsPath)) {
		LOGINFO (TEXT ("JVM ready"));
	} else {
		LOGERROR (TEXT ("Error initialising native library within JVM"));
	}
	pEnv->PopLocalFrame (NULL);
	return pJvm;
}

/**
 * <summary>Calls a static method on our main class.</summary>
 * <param name="pEnv">The JNI environment.</param>
 * <param name="pszMethodName">The name of the method to invoke.</param>
 * <param name="pszSignature">The method signature to invoke.</param>
 * <returns>The boolean result of the method.</returns>
 */
bool CJVM::Invoke (JNIEnv *pEnv, const char *pszMethodName, const char *pszSignature, ...) {
	LOGDEBUG ("Invoking " << pszMethodName << " on " << MAIN_CLASS);
	jclass cls = pEnv->FindClass (MAIN_CLASS);
	if (!cls) {
		LOGWARN (TEXT ("Couldn't find class ") << TEXT (MAIN_CLASS));
		return FALSE;
	}
	jmethodID mtd = pEnv->GetStaticMethodID (cls, pszMethodName, pszSignature);
	if (!mtd) {
		LOGWARN ("Couldn't find method " << pszMethodName << " on " << MAIN_CLASS);
		return FALSE;
	}
	va_list args;
	va_start (args, pszSignature);
	jboolean res = pEnv->CallStaticBooleanMethodV (cls, mtd, args);
	LOGDEBUG (pszMethodName << " returned " << (res ? "true" : "false"));
	return res != 0;
}

/**
 * <summary>Attaches the calling thread to the JVM and calls the no-arg static method on our main class.</summary>
 * <param name="pszMethodName">The name of the method to invoke.</param>
 * <returns>The boolean result of the method.</returns>
 */
bool CJVM::Invoke (const char *pszMethodName) {
	JNIEnv *pEnv;
	JavaVMAttachArgs args;
	ZeroMemory (&args, sizeof (args));
	args.version = JNI_VERSION_1_6;
	args.name = "Asynchronous SCM thread";
	jint err = m_pJVM->AttachCurrentThread ((PVOID*)&pEnv, &args);
	if (err) {
		LOGWARN (TEXT ("Couldn't attach thread to JVM, error ") << err);
		return FALSE;
	}
	jboolean res = Invoke (pEnv, pszMethodName, "()Z");
	m_pJVM->DetachCurrentThread ();
	return res != 0;
}

DWORD CJVM::StartProc (PVOID _po) {
	CJVM *po = (CJVM*)_po;
	if (po->Invoke ("svcStart")) {
		LOGINFO (TEXT ("Service started"));
		po->m_bRunning = TRUE;
	} else {
		LOGERROR (TEXT ("Couldn't start service"));
	}
	return 0;
}

DWORD CJVM::StopProc (PVOID _po) {
	CJVM *po = (CJVM*)_po;
	if (po->Invoke ("svcStop")) {
		LOGINFO (TEXT ("Service stopped"));
		po->m_bRunning = FALSE;
	} else {
		LOGERROR (TEXT ("Couldn't stop service"));
	}
	return 0;
}

void CJVM::Start () {
	EnterCriticalSection (&m_cs);
	if (m_hBusyTask) {
		LOGERROR (TEXT ("Already a busy task running"));
	} else {
		DWORD dwThreadId;
		m_hBusyTask = CreateThread (NULL, 0, StartProc, this, 0, &dwThreadId);
		if (m_hBusyTask) {
			LOGINFO (TEXT ("Created startup thread ") << dwThreadId);
		} else {
			LOGERROR (TEXT ("Couldn't create startup thread, error ") << GetLastError ());
		}
	}
	LeaveCriticalSection (&m_cs);
}

void CJVM::Stop () {
	EnterCriticalSection (&m_cs);
	if (m_hBusyTask) {
		LOGERROR (TEXT ("Already a busy task running"));
	} else {
		DWORD dwThreadId;
		m_hBusyTask = CreateThread (NULL, 0, StopProc, this, 0, &dwThreadId);
		if (m_hBusyTask) {
			LOGINFO (TEXT ("Created stop thread ") << dwThreadId);
		} else {
			LOGERROR (TEXT ("Couldn't create stop thread, error ") << GetLastError ());
		}
	}
	LeaveCriticalSection (&m_cs);
}

bool CJVM::IsBusy (unsigned long dwTimeout) {
	HANDLE hBusyTask = m_hBusyTask;
	EnterCriticalSection (&m_cs);
	hBusyTask = m_hBusyTask;
	LeaveCriticalSection (&m_cs);
	if (hBusyTask) {
		DWORD dw = WaitForSingleObject (hBusyTask, dwTimeout);
		if (dw == WAIT_OBJECT_0) {
			EnterCriticalSection (&m_cs);
			m_hBusyTask = NULL;
			LeaveCriticalSection (&m_cs);
			CloseHandle (hBusyTask);
			return FALSE;
		} else if (dw == WAIT_TIMEOUT) {
			return TRUE;
		} else {
			LOGERROR (TEXT ("Error waiting for busy task, dw=") << dw);
			return TRUE;
		}
	} else {
		return FALSE;
	}
}

bool CJVM::IsRunning () {
	bool bResult;
	EnterCriticalSection (&m_cs);
	bResult = m_bRunning;
	LeaveCriticalSection (&m_cs);
	return bResult;
}

void CJVM::UserConnection (PCTSTR pszUserName, PCTSTR pszInputPipe, PCTSTR pszOutputPipe) {
	EnterCriticalSection (&m_cs);
	if (m_bRunning) {
		m_pEnv->PushLocalFrame (3);
#ifdef _UNICODE
		jstring jsUserName = m_pEnv->NewString ((jchar*)pszUserName, wcslen (pszUserName));
		jstring jsInputPipe = m_pEnv->NewString ((jchar*)pszInputPipe, wcslen (pszInputPipe));
		jstring jsOutputPipe = m_pEnv->NewString ((jchar*)pszOutputPipe, wcslen (pszOutputPipe));
#else
		jstring jsUserName = m_pEnv->NewStringUTF (pszUserName, strlen (pszUserName));
		jstring jsInputPipe = m_pEnv->NewStringUTF (pszInputPipe, strlen (pszInputPipe));
		jstring jsOutputPipe = m_pEnv->NewStringUTF (pszOutputPipe, strlen (pszOutputPipe));
#endif
		if (Invoke (m_pEnv, "svcAccept", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z", jsUserName, jsInputPipe, jsOutputPipe)) {
			LOGINFO (TEXT ("Connection from ") << pszUserName << TEXT (" accepted"));
		} else {
			LOGWARN (TEXT ("Couldn't accept connection from ") << pszUserName);
		}
		m_pEnv->PopLocalFrame (NULL);
	} else {
		// This shouldn't happen
		LOGFATAL (TEXT ("JVM is shutting down - discarding connection request"));
	}
	LeaveCriticalSection (&m_cs);
}

bool CJVM::IsStopped () {
	return Invoke (m_pEnv, "svcIsStopped", "()Z");
}