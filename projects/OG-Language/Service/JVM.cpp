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
#define MAIN_CLASS		"com/opengamma/language/connector/Main"

typedef jint (JNICALL *JNI_CREATEJAVAVMPROC) (JavaVM **ppjvm, JNIEnv **ppEnv, JavaVMInitArgs *pArgs);

CJVM::CJVM (CLibrary *poModule, JavaVM *pJVM, JNIEnv *pEnv) {
	LOGINFO (TEXT ("JVM created"));
	m_poModule = poModule;
	m_pJVM = pJVM;
	m_pEnv = pEnv;
	m_poBusyTask = NULL;
	m_bRunning = false;
}

CJVM::~CJVM () {
#ifdef DESTROY_JVM
	LOGDEBUG (TEXT ("Destroying JVM"));
	m_pJVM->DestroyJavaVM ();
	LOGINFO (TEXT ("JVM destroyed"));
#else /* ifdef DESTROY_JVM */
	LOGINFO (TEXT ("Destroying JVM implicitly through FreeLibrary call"));
#endif /* ifdef DESTROY_JVM */
	delete m_poModule;
	if (m_poBusyTask) {
		CThread::Release (m_poBusyTask);
	}
}

static CLibrary *_LoadJVMLibrary (const TCHAR *pszLibrary) {
	const TCHAR *pszSearchPath = NULL;
	TCHAR *psz = _tcsdup (pszLibrary);
	if (!psz) {
		LOGFATAL (TEXT ("Out of memory"));
		return NULL;
	}
	int i = _tcslen (pszLibrary), separators = 2;
	while (--i > 0) {
		if (pszLibrary[i] == PATH_CHAR) {
			if (!--separators) {
				psz[i] = 0;
				LOGDEBUG (TEXT ("Library search path ") << psz);
				pszSearchPath = psz;
				break;
			}
		}
	}
	CLibrary *po = CLibrary::Create (pszLibrary, pszSearchPath);
	free (psz);
	return po;
}

static char *_OptionFudgeAnnotationCache (CSettings *pSettings) {
	const TCHAR *pszCache = pSettings->GetAnnotationCache ();
	if (!pszCache) {
		LOGWARN (TEXT ("No path for Fudge annotation cache"));
		return NULL;
	}
	size_t cch = 34 + _tcslen (pszCache);
	char *pszOption = new char[cch];
	if (!pszOption) {
		LOGFATAL (TEXT ("Out of memory"));
		return NULL;
	}
#ifdef _UNICODE
	StringCbPrintfA (pszOption, cch, "-Dfudgemsg.annotationCachePath=%ws", pszCache);
#else
	StringCbPrintf (pszOption, cch, "-Dfudgemsg.annotationCachePath=%s", pszCache);
#endif
	LOGDEBUG ("Using " << pszOption);
	return pszOption;
}

static char *_BuildClasspath (char *pszBuffer, size_t *pcchUsed, size_t *pcchTotal, const TCHAR *pszPath, const TCHAR *pszFile) {
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
		char *pszNewBuffer = new char[*pcchTotal];
		if (!pszNewBuffer) {
			LOGFATAL (TEXT ("Out of memory"));
			return pszBuffer;
		}
		memcpy (pszNewBuffer, pszBuffer, *pcchUsed * sizeof (char));
		free (pszBuffer);
		pszBuffer = pszNewBuffer;
	}
#ifdef _UNICODE
	StringCbPrintfA (pszBuffer + *pcchUsed, *pcchTotal - *pcchUsed, SEP_CHAR_STR "%ws" PATH_CHAR_STR "%ws", pszPath, pszFile);
#else
	StringCbPrintf (pszBuffer + *pcchUsed, *pcchTotal - *pcchUsed, SEP_CHAR_STR "%s" PATH_CHAR_STR "%s", pszPath, pszFile);
#endif
	*pcchUsed += cchExtra;
	return pszBuffer;
}

static char *_ScanClassPath (char *pszBuffer, size_t *pcchUsed, size_t *pcchTotal, const TCHAR *pszPath) {
	size_t cchSearch;
	TCHAR *pszSearch;
#ifdef _WIN32
	WIN32_FIND_DATA wfd;
	cchSearch = _tcslen (pszPath) + 5;
	pszSearch = new TCHAR[cchSearch];
	if (!pszSearch) {
		LOGFATAL (TEXT ("Out of memory"));
		return pszBuffer;
	}
	StringCchPrintf (pszSearch, cchSearch, TEXT ("%s") TEXT (PATH_CHAR_STR) TEXT ("*.*"), pszPath);
	HANDLE hFind = FindFirstFile (pszSearch, &wfd);
	delete pszSearch;
	if (hFind != NULL) {
		do {
#define __filename	wfd.cFileName
#define __isdir		(wfd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
#else
	DIR *dir = opendir (pszPath);
	if (dir) {
		struct dirent *dp;
		while ((dp = readdir (dir)) != NULL) {
#define __filename	dp->d_name
#define __isdir		(dp->d_type & DT_DIR)
#endif
			if (__filename[0] == '.') {
				continue;
			}
			if (__isdir) {
				LOGDEBUG (TEXT ("Recursing into folder ") << __filename);
				cchSearch = _tcslen (pszPath) + _tcslen (__filename) + 2;
				pszSearch = new TCHAR[cchSearch];
				if (!pszSearch) {
					LOGFATAL (TEXT ("Out of memory"));
					break;
				}
				StringCbPrintf (pszSearch, cchSearch / sizeof (TCHAR), TEXT ("%s") TEXT (PATH_CHAR_STR) TEXT ("%s"), pszPath, __filename);
				pszBuffer = _ScanClassPath (pszBuffer, pcchUsed, pcchTotal, pszSearch);
				delete pszSearch;
				continue;
			}
			const TCHAR *psz = _tcsrchr (__filename, '.');
			if (!psz) {
				continue;
			}
			if (_tcsicmp (psz, TEXT (".ear"))
			 && _tcsicmp (psz, TEXT (".jar"))
			 && _tcsicmp (psz, TEXT (".war"))
			 && _tcsicmp (psz, TEXT (".zip"))) {
				 continue;
			}
			pszBuffer = _BuildClasspath (pszBuffer , pcchUsed, pcchTotal, pszPath, __filename);
#undef __filename
#undef __isdir
#ifdef _WIN32
		} while (FindNextFile (hFind, &wfd));
		FindClose (hFind);
#else
		}
		closedir (dir);
#endif
	} else {
		LOGWARN (TEXT ("Can't read folder ") << pszPath << TEXT (", error ") << GetLastError ());
	}
	return pszBuffer;
}

static char *_OptionClassPath (CSettings *pSettings) {
	const TCHAR *pszPath = pSettings->GetJarPath ();
	if (!pszPath) {
		LOGWARN (TEXT ("No JAR folder available"));
		return NULL;
	}
#ifdef _WIN32
	if (!_tcsncmp (pszPath, TEXT ("\\\\?\\"), 4)) {
		LOGDEBUG (TEXT ("Skipping \\\\?\\ prefix on JAR path"));
		pszPath += 4;
	}
#endif /* ifdef _WIN32 */
	size_t cchUsed = 19 + _tcslen (pszPath);
	size_t cch = 32 * _tcslen (pszPath);
	char *pszOption = new char[cch];
	if (!pszOption) {
		LOGFATAL (TEXT ("Out of memory"));
		return NULL;
	}
#ifdef _UNICODE
	StringCbPrintfA (pszOption, cch, "-Djava.class.path=%ws" PATH_CHAR_STR, pszPath);
#else
	StringCbPrintf (pszOption, cch, "-Djava.class.path=%s" PATH_CHAR_STR, pszPath);
#endif
	pszOption = _ScanClassPath (pszOption, &cchUsed, &cch, pszPath);
	LOGDEBUG ("Using " << pszOption << " (" << strlen (pszOption) << " chars)");
	return pszOption;
}

static void JNICALL _notifyStop (JNIEnv *pEnv, jclass cls) {
	LOGINFO (TEXT ("STOP called from JVM"));
	ServiceStop (false);
}

static void JNICALL _notifyPause (JNIEnv *pEnv, jclass cls) {
	LOGINFO (TEXT ("PAUSE called from JVM"));
	ServiceSuspend ();
}

CJVM *CJVM::Create () {
	CSettings settings;
	const TCHAR *pszLibrary = settings.GetJvmLibrary ();
	LOGDEBUG (TEXT ("Loading library ") << pszLibrary << TEXT (" and creating JVM"));
	CLibrary *poLibrary = _LoadJVMLibrary (pszLibrary);
	if (!poLibrary) {
		LOGWARN (TEXT ("Couldn't load ") << pszLibrary << TEXT (", error ") << GetLastError ());
		return NULL;
	}
	JNI_CREATEJAVAVMPROC procCreateVM = (JNI_CREATEJAVAVMPROC)poLibrary->GetAddress ("JNI_CreateJavaVM");
	if (!procCreateVM) {
		LOGWARN (TEXT ("Couldn't find JNI_CreateJavaVM, error ") << GetLastError ());
		delete poLibrary;
		return NULL;
	}
	JavaVM *pJVM;
	JNIEnv *pEnv;
	JavaVMOption option[3];
	memset (&option, 0, sizeof (option));
	option[0].optionString = _OptionClassPath (&settings);
	option[1].optionString = _OptionFudgeAnnotationCache (&settings);
#ifdef _DEBUG
	option[2].optionString = (char*)"-Dservice.debug=true";
#else
	option[2].optionString = (char*)"-Dservice.ndebug=true";
#endif
	// TODO [XLS-187] additional option strings from registry
	JavaVMInitArgs args;
	memset (&args, 0, sizeof (args));
	args.version = JNI_VERSION_1_6;
	args.options = option;
	args.nOptions = 3;
	LOGDEBUG (TEXT ("Creating JVM"));
	jint err = procCreateVM (&pJVM, &pEnv, &args);
	if (option[0].optionString) {
		delete option[0].optionString;
	}
	if (option[1].optionString) {
		delete option[1].optionString;
	}
	if (err) {
		LOGWARN (TEXT ("Couldn't create JVM, error ") << err);
		delete poLibrary;
		return NULL;
	}
	CJVM *pJvm = new CJVM (poLibrary, pJVM, pEnv);
	if (!pJvm) {
		LOGFATAL (TEXT ("Out of memory"));
		delete poLibrary;
		return NULL;
	}
	LOGDEBUG (TEXT ("Registering native methods"));
	JNINativeMethod methods[2] = {
		{ "notifyPause", "()V", (void*)&_notifyPause },
		{ "notifyStop", "()V", (void*)&_notifyStop }
	};
	jclass cls = pEnv->FindClass (MAIN_CLASS);
	if (!cls) {
		LOGWARN (TEXT ("Couldn't find class ") << TEXT (MAIN_CLASS));
		delete pJvm;
		return NULL;
	}
	err = pEnv->RegisterNatives (cls, methods, 2);
	if (err) {
		LOGWARN (TEXT ("Couldn't register native methods, error ") << err);
		delete pJvm;
		return NULL;
	}
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
		return false;
	}
	jmethodID mtd = pEnv->GetStaticMethodID (cls, pszMethodName, pszSignature);
	if (!mtd) {
		LOGWARN ("Couldn't find method " << pszMethodName << " on " << MAIN_CLASS);
		return false;
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
	memset (&args, 0, sizeof (args));
	args.version = JNI_VERSION_1_6;
	args.name = (char*)"Asynchronous SCM thread";
	jint err = m_pJVM->AttachCurrentThread ((void**)&pEnv, &args);
	if (err) {
		LOGWARN (TEXT ("Couldn't attach thread to JVM, error ") << err);
		return false;
	}
	jboolean res = Invoke (pEnv, pszMethodName, "()Z");
	m_pJVM->DetachCurrentThread ();
	return res != 0;
}

class CBusyTask : public CThread {
private:
	CJVM *m_pJVM;
	bool m_bStart;
public:
	CBusyTask (CJVM *pJVM, bool bStart) {
		m_pJVM = pJVM;
		m_bStart = bStart;
	}
	void Run () {
		if (m_bStart) {
			m_pJVM->Start (false);
		} else {
			m_pJVM->Stop (false);
		}
	}
};

void CJVM::Start (bool bAsync) {
	if (bAsync) {
		m_oMutex.Enter ();
		if (m_poBusyTask) {
			LOGERROR (TEXT ("Already a busy task running"));
		} else {
			m_poBusyTask = new CBusyTask (this, true);
			if (m_poBusyTask->Start ()) {
				LOGINFO (TEXT ("Created startup thread ") << m_poBusyTask->GetThreadId ());
			} else {
				LOGERROR (TEXT ("Couldn't create startup thread, error ") << GetLastError ());
				CThread::Release (m_poBusyTask);
				m_poBusyTask = NULL;
			}
		}
		m_oMutex.Leave ();
	} else {
		if (Invoke ("svcStart")) {
			LOGINFO (TEXT ("Service started"));
			m_bRunning = true;
		} else {
			LOGERROR (TEXT ("Couldn't start service"));
		}
	}
}

void CJVM::Stop (bool bAsync) {
	if (bAsync) {
		m_oMutex.Enter ();
		if (m_poBusyTask) {
			LOGERROR (TEXT ("Already a busy task running"));
		} else {
			m_poBusyTask = new CBusyTask (this, false);
			if (m_poBusyTask->Start ()) {
				LOGINFO (TEXT ("Created stop thread ") << m_poBusyTask->GetThreadId ());
			} else {
				LOGERROR (TEXT ("Couldn't create stop thread, error ") << GetLastError ());
				CThread::Release (m_poBusyTask);
				m_poBusyTask = NULL;
			}
		}
		m_oMutex.Leave ();
	} else {
		if (Invoke ("svcStop")) {
			LOGINFO (TEXT ("Service stopped"));
			m_bRunning = false;
		} else {
			LOGERROR (TEXT ("Couldn't stop service"));
		}
	}
}

bool CJVM::IsBusy (unsigned long dwTimeout) {
	CThread *poBusyTask;
	m_oMutex.Enter ();
	poBusyTask = m_poBusyTask;
	m_oMutex.Leave ();
	if (poBusyTask) {
		if (poBusyTask->Wait (dwTimeout)) {
			m_oMutex.Enter ();
			CThread::Release (m_poBusyTask);
			m_poBusyTask = NULL;
			m_oMutex.Leave ();
			return false;
		} else {
			int error = GetLastError ();
			if (error != ETIMEDOUT) {
				LOGERROR (TEXT ("Couldn't wait for busy task, error ") << error);
			}
			return true;
		}
	} else {
		return false;
	}
}

bool CJVM::IsRunning () {
	bool bResult;
	m_oMutex.Enter ();
	bResult = m_bRunning;
	m_oMutex.Leave ();
	return bResult;
}

void CJVM::UserConnection (const TCHAR *pszUserName, const TCHAR *pszInputPipe, const TCHAR *pszOutputPipe) {
	m_oMutex.Enter ();
	if (m_bRunning) {
		m_pEnv->PushLocalFrame (3);
#ifdef _UNICODE
		jstring jsUserName = m_pEnv->NewString ((jchar*)pszUserName, wcslen (pszUserName));
		jstring jsInputPipe = m_pEnv->NewString ((jchar*)pszInputPipe, wcslen (pszInputPipe));
		jstring jsOutputPipe = m_pEnv->NewString ((jchar*)pszOutputPipe, wcslen (pszOutputPipe));
#else
		jstring jsUserName = m_pEnv->NewStringUTF (pszUserName);
		jstring jsInputPipe = m_pEnv->NewStringUTF (pszInputPipe);
		jstring jsOutputPipe = m_pEnv->NewStringUTF (pszOutputPipe);
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
	m_oMutex.Leave ();
}

bool CJVM::IsStopped () {
	return Invoke (m_pEnv, "svcIsStopped", "()Z");
}