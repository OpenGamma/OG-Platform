/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

#include "JVM.h"
#include "Service.h"
#include <Util/File.h>
#include <Util/Library.h>

LOGGING(com.opengamma.language.service.JVM);

//#define DESTROY_JVM /* If there are rogue threads, the JVM won't terminate gracefully so comment this line out */
#define MAIN_CLASS		"com/opengamma/language/connector/Main"

typedef jint (JNICALL *JNI_CREATEJAVAVMPROC) (JavaVM **ppjvm, JNIEnv **ppEnv, JavaVMInitArgs *pArgs);

/// Count of the number of times writeProperty has been called.
static int g_nWriteProperty;

/// Creates a new JVM instance. Note that only one JVM instance is sensible; some libraries don't like
/// destroying JVMs or creating multiple ones.
///
/// @param[in] poModule the JVM library, never NULL
/// @param[in] pJVM the created JVM instance, never NULL
/// @param[in] pEnv the created JVM environment, never NULL
CJVM::CJVM (CLibrary *poModule, JavaVM *pJVM, JNIEnv *pEnv) {
	LOGINFO (TEXT ("JVM created"));
	m_poModule = poModule;
	m_pJVM = pJVM;
	m_pEnv = pEnv;
	m_poBusyTask = NULL;
	m_bRunning = false;
}

/// Destroys the JVM. Not all JVM libraries support the DestroyJavaVM method properly when there are
/// Java threads still running. Use the DESTROY_JVM macro to control whether to make the call at compile-time.
CJVM::~CJVM () {
// TODO: this should be a "setting" with a compile-time default, not just a compile-time flag
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

/// Loads the JVM library.
///
/// @param[in] pszLibrary full path to the JVM library (or just the path name if it is in the path
/// @return the library, or NULL if none could be loaded
static CLibrary *_LoadJVMLibrary (const TCHAR *pszLibrary) {
	TCHAR *psz = _tcsdup (pszLibrary);
	if (!psz) {
		LOGFATAL (TEXT ("Out of memory"));
		return NULL;
	}
#ifdef _WIN32
	const TCHAR *pszSearchPath = NULL;
	size_t i = _tcslen (pszLibrary);
	int separators = 2;
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
#endif /* ifdef _WIN32 */
	CLibrary *po = CLibrary::Create (pszLibrary
#ifdef _WIN32
			, pszSearchPath
#endif /* ifdef _WIN32 */
			);
	free (psz);
	return po;
}

/// Appends a fragment to the classpath buffer.
///
/// @param[in,out] pszBuffer classpath buffer, never NULL
/// @param[in,out] pcchUsed number of characters used in the buffer, never NULL
/// @param[in] pszPath path fragment to append, never NULL
/// @param[in] pszFile filename to append, never NULL
/// @return the classpath buffer; this may be pszBuffer, or may be reallocated memory
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

/// Recursively scans a folder for Java resources and adds them to the classpath.
///
/// @param[in,out] pszBuffer classpath buffer, never NULL
/// @param[in,out] pcchUsed number of characters used in the buffer, never NULL
/// @param[in,out] pcchTotal size of the buffer in characters, never NULL
/// @param[in] pszPath folder to scan, never NULL
/// @return the classpath buffer; this may be pszBuffer, or may be reallocated memory
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
				StringCbPrintf (pszSearch, cchSearch * sizeof (TCHAR), TEXT ("%s") TEXT (PATH_CHAR_STR) TEXT ("%s"), pszPath, __filename);
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

/// Creates a JVM parameter for the classpath to use. The caller must free the allocated memory.
/// The classpath is based on the JarPath returned by the settings object; with each Java resource
/// underneath it explicitly named.
///
/// @param[in] pSettings the current settings
/// @return the classpath
static char *_OptionClassPath (const CSettings *pSettings) {
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

/// Creates a JVM memory parameter.
///
/// @param[in] pszPrefix parameter prefix
/// @param[in] dwMemory memory limit in Mb
/// @return the option string
static char *_OptionMemory (const char *pszPrefix, unsigned long dwMemory) {
	char szOption[16];
	StringCbPrintfA (szOption, sizeof (szOption), "-X%s%dm", pszPrefix, dwMemory);
	return strdup (szOption);
}

/// Implementation of the notifyStop method in the Main class.
///
/// @param pEnv see Java documentation
/// @param cls see Java documentation
static void JNICALL _notifyStop (JNIEnv *pEnv, jclass cls) {
	__unused (pEnv)
	__unused (cls)
	LOGINFO (TEXT ("STOP called from JVM"));
	ServiceStop (false);
}

/// Implementation of the notifyPause method in the Main class.
///
/// @param pEnv see Java documentation
/// @param cls see Java documentation
static void JNICALL _notifyPause (JNIEnv *pEnv, jclass cls) {
	__unused (pEnv)
	__unused (cls)
	LOGINFO (TEXT ("PAUSE called from JVM"));
	ServiceSuspend ();
}

/// Implementation of the writeProperty method in the Main class.
///
/// @param pEnv see Java documentation
/// @param cls see Java documentation
static void JNICALL _writeProperty (JNIEnv *pEnv, jclass cls, jstring jstrProperty, jstring jstrValue) {
	LOGINFO (TEXT ("WRITEPROPERTY called from JVM"));
#ifdef _UNICODE
	const wchar_t *pszProperty = (const wchar_t*)pEnv->GetStringChars (jstrProperty, NULL);
	const wchar_t *pszValue = jstrValue ? (const wchar_t*)pEnv->GetStringChars (jstrValue, NULL) : NULL;
#else /* ifdef _UNICODE */
	const char *pszProperty = pEnv->GetStringUTFChars (jstrProperty, NULL);
	const char *pszValue = jstrValue ? pEnv->GetStringUTFChars (jstrValue, NULL) : NULL;
#endif /* ifdef _UNICODE */
	CSettings oSettings;
	oSettings.SetJvmProperty (pszProperty, pszValue);
#ifdef _UNICODE
	pEnv->ReleaseStringChars (jstrProperty, (const jchar*)pszProperty);
	if (pszValue) pEnv->ReleaseStringChars (jstrValue, (const jchar*)pszValue);
#else /* ifdef _UNICODE */
	pEnv->ReleaseStringUTFChars (jstrProperty, pszProperty);
	if (pszValue) pEnv->ReleaseStringUTFChars (jstrValue, pszValue);
#endif /* ifdef _UNICODE */
	g_nWriteProperty++;
}

/// Creates a new JVM instance.
///
/// @param[in] poFeedback the feedback reporting mechansim for errors, never NULL
/// @return the JVM, or NULL if there was a problem
CJVM *CJVM::Create (CErrorFeedback *poFeedback) {
	CSettings oSettings;
	const TCHAR *pszLibrary = oSettings.GetJvmLibrary ();
	LOGDEBUG (TEXT ("Loading library ") << pszLibrary << TEXT (" and creating JVM"));
	CLibrary *poLibrary = _LoadJVMLibrary (pszLibrary);
	if (!poLibrary) {
		int ec = GetLastError ();
		LOGWARN (TEXT ("Couldn't load ") << pszLibrary << TEXT (", error ") << ec);
		TCHAR *psz = new TCHAR[1024];
		if (psz) {
			StringCchPrintf (psz, 1024,
				TEXT ("Couldn't load the Java virtual machine - %s, error %d.")
				TEXT ("\nPlease check that a suitable version of Java is installed. If Java is installed and you are seeing this message, please refer to the OpenGamma documentation for instructions on how to update the configuration to locate your Java version.")
#ifdef _WIN32
#ifdef _WIN64
				TEXT ("\nThis is a 64-bit installation.")
#else /* ifdef _WIN64 */
				TEXT ("\nThis is a 32-bit installation.")
#endif /* ifdef _WIN64 */
#endif /* ifdef _WIN32 */
				, pszLibrary, ec);
			poFeedback->Write (psz);
			delete psz;
		} else {
			LOGFATAL (TEXT ("Out of memory"));
		}
		return NULL;
	}
	JNI_CREATEJAVAVMPROC procCreateVM = (JNI_CREATEJAVAVMPROC)poLibrary->GetAddress ("JNI_CreateJavaVM");
	if (!procCreateVM) {
		int ec = GetLastError ();
		LOGWARN (TEXT ("Couldn't find JNI_CreateJavaVM, error ") << ec);
		TCHAR *psz = new TCHAR[1024];
		if (psz) {
			StringCchPrintf (psz, 1024,
				TEXT ("Invalid Java virtual machine - %s, error %d.")
				TEXT ("\nPlease check that a suitable version of Java is installed. If Java is installed and you are seeing this message, please refer to the OpenGamma documentation for instructions on how to update the configuration to locate your Java version.")
#ifdef _WIN32
#ifdef _WIN64
				TEXT ("\nThis is a 64-bit installation.")
#else /* ifdef _WIN64 */
				TEXT ("\tThis is a 32-bit installation.")
#endif /* ifdef _WIN64 */
#endif /* ifdef _WIN32 */
				, pszLibrary, ec);
			poFeedback->Write (psz);
			delete psz;
		} else {
			LOGFATAL (TEXT ("Out of memory"));
		}
		delete poLibrary;
		return NULL;
	}
	JavaVM *pJVM;
	JNIEnv *pEnv;
	JavaVMInitArgs args;
	memset (&args, 0, sizeof (args));
	args.version = JNI_VERSION_1_6;
	JavaVMOption option[4];
	memset (&option, 0, sizeof (option));
	args.options = option;
	if ((option[args.nOptions].optionString = _OptionClassPath (&oSettings)) != NULL) args.nOptions++;
	if ((option[args.nOptions].optionString = strdup ("-Dcom.sun.management.jmxremote")) != NULL) args.nOptions++;
	if ((option[args.nOptions].optionString = _OptionMemory ("ms", oSettings.GetJvmMinHeap ())) != NULL) args.nOptions++;
	if ((option[args.nOptions].optionString = _OptionMemory ("mx", oSettings.GetJvmMaxHeap ())) != NULL) args.nOptions++;
	// TODO [PLAT-1116] additional option strings from registry
	LOGDEBUG (TEXT ("Creating JVM"));
	jint err = procCreateVM (&pJVM, &pEnv, &args);
	while (args.nOptions > 0) {
		free (option[--args.nOptions].optionString);
	}
	if (err) {
		LOGWARN (TEXT ("Couldn't create JVM, error ") << err);
		TCHAR *psz = new TCHAR[1024];
		if (psz) {
			StringCchPrintf (psz, 1024,
				TEXT ("Couldn't create the Java virtual machine, error %d.")
				TEXT ("\nThe Java library being used is %s"),
				err, pszLibrary);
			poFeedback->Write (psz);
			delete psz;
		}
		delete poLibrary;
		return NULL;
	}
	CProperties oProperties (pEnv);
	oProperties.SetProperties (&oSettings);
	CJVM *pJvm = new CJVM (poLibrary, pJVM, pEnv);
	if (!pJvm) {
		LOGFATAL (TEXT ("Out of memory"));
		delete poLibrary;
		poFeedback->Write (TEXT ("Out of memory"));
		return NULL;
	}
	LOGDEBUG (TEXT ("Registering native methods"));
	JNINativeMethod methods[3] = {
		{ (char*)"notifyPause", (char*)"()V", (void*)&_notifyPause },
		{ (char*)"notifyStop", (char*)"()V", (void*)&_notifyStop },
		{ (char*)"writeProperty", (char*)"(Ljava/lang/String;Ljava/lang/String;)V", (void*)&_writeProperty }
	};
	jclass cls = pEnv->FindClass (MAIN_CLASS);
	if (!cls) {
		LOGWARN (TEXT ("Couldn't find class ") << TEXT (MAIN_CLASS));
		poFeedback->Write (TEXT ("One or more class files could not be found.\nPlease refer to the OpenGamma documentation for instructions on how to set the classpath for this service correctly."));
		delete pJvm;
		return NULL;
	}
	err = pEnv->RegisterNatives (cls, methods, 3);
	if (err) {
		LOGWARN (TEXT ("Couldn't register native methods, error ") << err);
		TCHAR *psz = new TCHAR[1024];
		if (psz) {
			StringCchPrintf (psz, 1024, TEXT ("Couldn't register native methods, error %d"), err);
			poFeedback->Write (psz);
			delete psz;
		}
		delete pJvm;
		return NULL;
	}
	return pJvm;
}

/// Sets the system properties on the JVM using the Main.setProperty method.
///
/// @param[in] poSettings settings source
void CJVM::CProperties::SetProperties (const CSettings *poSettings) const {
	// Annotation caches
	const TCHAR *psz = poSettings->GetAnnotationCache ();
	if (psz) {
		LOGDEBUG (TEXT ("Using ") << psz << TEXT (" for annotation caches"));
		Setting (TEXT ("fudgemsg.annotationCachePath"), psz);
		Setting (TEXT ("language.annotationCachePath"), psz);
	} else {
		LOGWARN (TEXT ("No path for annotation caches"));
	}
	// Language extension dir
	psz = poSettings->GetExtPath ();
	if (psz) {
		LOGDEBUG (TEXT ("Using ") << psz << TEXT (" for extension folder"));
		Setting (TEXT ("language.ext.path"), psz);
	} else {
		LOGWARN (TEXT ("No extension folder available"));
	}
	// Debug flag
	Setting (
#ifdef _DEBUG
		TEXT ("service.debug")
#else /* ifdef _DEBUG */
		TEXT ("service.ndebug")
#endif /* ifdef _DEBUG */
		, TEXT ("true"));
	poSettings->GetJvmProperties (this);
}

/// Calls a static method on the Main class that returns a boolean value
///
/// @param[in] pEnv the JNI environment, never NULL
/// @param[in] pszMethodName name of the method to invoke, never NULL
/// @param[in] pszSignature method signature to invoke, never NULL
/// @return the method result, or FALSE if it could not be invoked
bool CJVM::InvokeBool (JNIEnv *pEnv, const char *pszMethodName, const char *pszSignature, ...) {
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

/// Calls a static method on the Main class that returns a string value. The string (from Java)
/// will be duplicated and a copy allocated on the heap. The caller must release this memory
/// when done.
///
/// @param[in] pEnv the JNI environment, never NULL
/// @param[in] pszMethodName name of the method to invoke, never NULL
/// @param[in] pszSignature method signature of psMethodName, never NULL
/// @return the method result, duplicated onto the heap if not NULL
TCHAR *CJVM::InvokeString (JNIEnv *pEnv, const char *pszMethodName, const char *pszSignature, ...) {
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
	jstring jstrResult = (jstring)pEnv->CallStaticObjectMethodV (cls, mtd, args);
	if (jstrResult) {
		TCHAR *pszResult;
#ifdef _UNICODE
		jsize cchResult = pEnv->GetStringLength (jstrResult);
#else /* ifdef _UNICODE */
		jsize cchResult = pEnv->GetStringUTFLength (jstrResult);
#endif /* ifdef _UNICODE */
		pszResult = new TCHAR[cchResult + 1];
		if (pszResult) {
			jboolean bCopy;
#ifdef _UNICODE
			const jchar *pszResultChars = pEnv->GetStringChars (jstrResult, &bCopy);
#else /* ifdef _UNICODE */
			const char *pszResultChars = pEnv->GetStringUTFChars (jstrResult, &bCopy);
#endif /* ifdef_ UNICODE */
			memcpy (pszResult, pszResultChars, cchResult * sizeof (TCHAR));
			pszResult[cchResult] = 0;
#ifdef _UNICODE
			pEnv->ReleaseStringChars (jstrResult, pszResultChars);
#else /* ifdef _UNICODE */
			pEnv->ReleaseStringUTFChars (jstrResult, pszResultChars);
#endif /* ifdef _UNICODE */
			LOGDEBUG ("String returned from " << pszMethodName);
			LOGDEBUG (pszResult);
		} else {
			LOGFATAL (TEXT ("Out of memory"));
		}
		pEnv->DeleteLocalRef (jstrResult);
		return pszResult;
	}  else {
		LOGDEBUG (pszMethodName << " returned NULL");
		return NULL;
	}
}

/// Attaches the calling thread to the JVM and return the thread's Java context/environment.
///
/// @param[in] pvm the Java VM
/// @return the Java environment
static JNIEnv *_AttachThread (JavaVM *pvm) {
	JNIEnv *pEnv;
	JavaVMAttachArgs args;
	memset (&args, 0, sizeof (args));
	args.version = JNI_VERSION_1_6;
	args.name = (char*)"Asynchronous SCM thread";
	jint err = pvm->AttachCurrentThread ((void**)&pEnv, &args);
	if (err) {
		LOGWARN (TEXT ("Couldn't attach thread to JVM, error ") << err);
		return NULL;
	}
	return pEnv;
}

/// Attaches the calling thread to the JVM and calls the no-arg static method on the Main class.
///
/// @param[in] pszMethodName name of the method to invoke
/// @return boolean result of the method, or FALSE if it could not be invoked
bool CJVM::InvokeBool (const char *pszMethodName) {
	JNIEnv *pEnv = _AttachThread (m_pJVM);
	if (!pEnv) {
		return false;
	}
	jboolean res = InvokeBool (pEnv, pszMethodName, "()Z");
	m_pJVM->DetachCurrentThread ();
	return res != 0;
}

/// Attaches the calling thread to the JVM and calls the no-arg static method on the Main class.
///
/// @param[in] pszMethodName name of the method to invoke
/// @return the string result of the method, or NULL if there was none.
TCHAR *CJVM::InvokeString (const char *pszMethodName) {
	JNIEnv *pEnv = _AttachThread (m_pJVM);
	if (!pEnv) {
		return _tcsdup (TEXT ("Internal Java error"));
	}
	TCHAR *psz = InvokeString (pEnv, pszMethodName, "()Ljava/lang/String;");
	m_pJVM->DetachCurrentThread ();
	return psz;
}

#ifdef _UNICODE
#ifdef _WIN64
/// Returns the length of a string, truncated gracefully on 64-bit architecture.
///
/// @param[in] psz string to measure
/// @return the length of the string, truncated to a positive integer
static inline jsize _SafeLen (const TCHAR *psz) {
	size_t len = wcslen (psz);
	return (len > MAXINT32) ? MAXINT32 : (jsize)len;
}
#else /* ifdef _WIN64 */
#define _SafeLen wcslen
#endif /* ifdef _WIN64 */
#endif /* ifdef _UNICODE */

/// Calls the SetProperty method on the Main class to define a system property. This should be
/// used in preference to System.setProperty as it includes diagnostic logging and handles
/// exceptions.
///
/// @param[in] pszKey property name to be set, never NULL
/// @param[in] pszValue property value to set, never NULL
void CJVM::CProperties::Setting (const TCHAR *pszKey, const TCHAR *pszValue) const {
	m_pEnv->PushLocalFrame (2);
#ifdef _UNICODE
	jstring jsKey = m_pEnv->NewString ((jchar*)pszKey, _SafeLen (pszKey));
	jstring jsValue = m_pEnv->NewString ((jchar*)pszValue, _SafeLen (pszValue));
#else /* ifdef _UNICODE */
	jstring jsKey = m_pEnv->NewStringUTF (pszKey);
	jstring jsValue = m_pEnv->NewStringUTF (pszValue);
#endif /* ifdef _UNICODE */
	bool bResult = InvokeBool (m_pEnv, "setProperty", "(Ljava/lang/String;Ljava/lang/String;)Z", jsKey, jsValue);
	m_pEnv->PopLocalFrame (NULL);
	if (bResult) {
		LOGDEBUG (TEXT ("Set property ") << pszKey << TEXT (" = ") << pszValue);
	} else {
		LOGWARN (TEXT ("Couldn't set property ") << pszKey << TEXT (" = ") << pszValue);
	}
}

/// Slave thread for asynchronous Start and Stop calls.
class CBusyTask : public CThread {
private:

	/// Feedback instance
	CErrorFeedback *m_poFeedback;

	/// JVM instance to invoke the method on
	CJVM *m_pJVM;

	/// TRUE to invoke Start, FALSE to invoke Stop
	bool m_bStart;
public:

	/// Creates a new slave thread
	///
	/// @param[in] pJVM JVM instance to invoke the method in
	/// @param[in] poFeedback the feedback instance
	/// @param[in] bStart TRUE to invoke Start, FALSE to invoke Stop
	CBusyTask (CJVM *pJVM, CErrorFeedback *poFeedback, bool bStart) {
		m_pJVM = pJVM;
		m_poFeedback = poFeedback;
		m_bStart = bStart;
	}

	// Invokes either Start or Stop on the JVM instance
	void Run () {
		if (m_bStart) {
			m_pJVM->Start (m_poFeedback, false);
		} else {
			m_pJVM->Stop (false);
		}
	}
};

/// Attempts to start the OpenGamma Java stack. Success of the call can be seen by the IsRunning, IsBusy and/or IsStopped methods.
///
/// @param[in] bAsync TRUE to return immediately and use a slave thread to start the Java stack, FALSE
/// to start it from the calling thread.
void CJVM::Start (CErrorFeedback *poFeedback, bool bAsync) {
	if (bAsync) {
		m_oMutex.Enter ();
		if (m_poBusyTask) {
			LOGERROR (TEXT ("Already a busy task running"));
		} else {
			m_poBusyTask = new CBusyTask (this, poFeedback, true);
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
		TCHAR *pszStartupError = InvokeString ("svcStart");
		if (pszStartupError) {
			LOGERROR (TEXT ("Couldn't start service"));
			poFeedback->Write (pszStartupError);
			free (pszStartupError);
		} else {
			LOGINFO (TEXT ("Service started"));
			m_bRunning = true;
		}
	}
}

/// Attempts to stop the OpenGamma Java stack. Success of the call can be seen by the IsRunning, IsBusy, and/or IsStopped methods.
///
/// @param[in] bAsync TRUE to return immediately and use a slave thread to stop the Java stack, FALSE
/// to stop it from the calling thread.
void CJVM::Stop (bool bAsync) {
	if (bAsync) {
		m_oMutex.Enter ();
		if (m_poBusyTask) {
			LOGERROR (TEXT ("Already a busy task running"));
		} else {
			m_poBusyTask = new CBusyTask (this, NULL, false);
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
		if (InvokeBool ("svcStop")) {
			LOGINFO (TEXT ("Service stopped"));
			m_bRunning = false;
		} else {
			LOGERROR (TEXT ("Couldn't stop service"));
		}
	}
}

/// Tests if the JVM instance is busy servicing an asynchronous call to Start or to Stop.
///
/// @param[in] dwTimeout milliseconds to wait for the servicing to finish
/// @return TRUE if an asynchronous call is in progress, FALSE otherwise
bool CJVM::IsBusy (unsigned long dwTimeout) const {
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

/// Tests if the OpenGamma Java stack is running (or at least indicated it could start).
///
/// @return TRUE if the stack is running, FALSE if it has not been started, has been stopped or the
/// call to Start failed.
bool CJVM::IsRunning () const {
	bool bResult;
	m_oMutex.Enter ();
	bResult = m_bRunning;
	m_oMutex.Leave ();
	return bResult;
}

/// Notifies the Java stack of a new user connection.
///
/// @param[in] pszUserName name of the user, never NULL
/// @param[in] pszInputPipe name of the pipe the stack should open for reading (i.e. the client will be writing to it), never NULL
/// @param[in] pszOutputPipe name of the pipe the stack should open for writing (i.e. the client will be reading from it), never NULL
/// @param[in] pszLanguageID language ID of the client, never NULL
void CJVM::UserConnection (const TCHAR *pszUserName, const TCHAR *pszInputPipe, const TCHAR *pszOutputPipe, const TCHAR *pszLanguageID) {
	m_oMutex.Enter ();
	if (m_bRunning) {
		m_pEnv->PushLocalFrame (4);
#ifdef _UNICODE
		jstring jsUserName = m_pEnv->NewString ((jchar*)pszUserName, _SafeLen (pszUserName));
		jstring jsInputPipe = m_pEnv->NewString ((jchar*)pszInputPipe, _SafeLen (pszInputPipe));
		jstring jsOutputPipe = m_pEnv->NewString ((jchar*)pszOutputPipe, _SafeLen (pszOutputPipe));
		jstring jsLanguageID = m_pEnv->NewString ((jchar*)pszLanguageID, _SafeLen (pszLanguageID));
#else
		jstring jsUserName = m_pEnv->NewStringUTF (pszUserName);
		jstring jsInputPipe = m_pEnv->NewStringUTF (pszInputPipe);
		jstring jsOutputPipe = m_pEnv->NewStringUTF (pszOutputPipe);
		jstring jsLanguageID = m_pEnv->NewStringUTF (pszLanguageID);
#endif
#ifdef _DEBUG
#define DEBUG_FLAG true
#else /* ifdef _DEBUG */
#define DEBUG_FLAG false
#endif /* ifdef _DEBUG */
		if (InvokeBool (m_pEnv, "svcAccept", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)Z", jsUserName, jsInputPipe, jsOutputPipe, jsLanguageID, DEBUG_FLAG)) {
#undef DEBUG_FLAG
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

/// Tests if the OpenGamma Java stack has been stopped.
///
/// @return TRUE if it is stopped, FALSE if not or if the method couldn't be invoked
bool CJVM::IsStopped () const {
	return InvokeBool (m_pEnv, "svcIsStopped", "()Z");
}

/// Runs the configuration task. If the configuration was changed and the service should be
/// restarted returns TRUE.
///
/// @return TRUE if the service configuration was changed, FALSE otherwise
bool CJVM::Configure () {
	g_nWriteProperty = 0;
	if (InvokeBool ("svcConfigure")) {
		LOGINFO (TEXT ("Configuration callback updated ") << g_nWriteProperty << TEXT (" setting(s)"));
		return g_nWriteProperty > 0;
	} else {
		LOGERROR (TEXT ("Couldn't run configuration callback"));
		return FALSE;
	}
}

#ifndef _WIN32
/// Tests if the JVM library is the correct version. Problems with the library are written to the log at INFO level.
///
/// @param[in] pszLibraryPath path to the library
/// @return TRUE if the library is good, FALSE if there is a problem.
bool ServiceTestJVM (const TCHAR *pszLibraryPath) {
	LOGDEBUG (TEXT ("Testing JVM library ") << pszLibraryPath);
	bool bResult = false;
	CLibrary *poLibrary = NULL;
	JNIEnv *pEnv = NULL;
	bool bPop = false;
	do {
		poLibrary = _LoadJVMLibrary (pszLibraryPath);
		if (!poLibrary) {
    	    LOGWARN (TEXT ("Couldn't load ") << pszLibraryPath << TEXT (", error ") << GetLastError ());
        	break;
		}
		JNI_CREATEJAVAVMPROC procCreateVM = (JNI_CREATEJAVAVMPROC)poLibrary->GetAddress ("JNI_CreateJavaVM");
		if (!procCreateVM) {
        	LOGWARN (TEXT ("Couldn't find JNI_CreateJavaVM in ") << pszLibraryPath << TEXT (", error ") << GetLastError ());
			break;
		}
		JavaVM *pJVM;
		JavaVMInitArgs args;
		memset (&args, 0, sizeof (args));
		args.version = JNI_VERSION_1_6;
		jint err = procCreateVM (&pJVM, &pEnv, &args);
		if (err) {
			LOGWARN (TEXT ("Couldn't create JVM from ") << pszLibraryPath << TEXT (", error ") << err);
			break;
		}
		jclass cls = pEnv->FindClass ("java/lang/System");
		if (!cls) {
			LOGWARN (TEXT ("Couldn't find class java.lang.System in ") << pszLibraryPath);
			break;
		}
		jmethodID mtd = pEnv->GetStaticMethodID (cls, "getProperty", "(Ljava/lang/String;)Ljava/lang/String;");
		if (!mtd) {
			LOGWARN (TEXT ("Couldn't find method java.lang.String.getProperty in ") << pszLibraryPath);
			break;
		}
		pEnv->PushLocalFrame (2);
		bPop = true;
		jstring str = pEnv->NewStringUTF ("java.specification.version");
		if (!str) {
			LOGWARN (TEXT ("Couldn't create local string using ") << pszLibraryPath);
			break;
		}
		str = (jstring)pEnv->CallStaticObjectMethod (cls, mtd, str);
		if (!str) {
			LOGWARN (TEXT ("Couldn't get java.specification.version property from ") << pszLibraryPath);
			break;
		}
		const char *pstr = pEnv->GetStringUTFChars (str, NULL);
		if (!pstr) {
			LOGWARN (TEXT ("Couldn't get java.specification.version property from ") << pszLibraryPath);
			break;
		}
		LOGDEBUG ("Found v" << pstr << TEXT (" JVM at ") << pszLibraryPath);
		int nMajor = JavaVersionFragment (pstr, 0);
		int nMinor = JavaVersionFragment (pstr, 1);
		pEnv->ReleaseStringUTFChars (str, pstr);
		if ((nMajor > 1) || ((nMajor == 1) && (nMinor >= 7))) {
			LOGINFO (TEXT ("Version ") << nMajor << TEXT (".") << nMinor << TEXT (" found in ") << pszLibraryPath);
			bResult = true;
		} else {
			LOGINFO (TEXT ("Version ") << nMajor << TEXT (".") << nMinor << TEXT (" from ") << pszLibraryPath << TEXT (" not supported"));
			bResult = false;
		}
    } while (false);
	if (pEnv) {
		if (bPop) pEnv->PopLocalFrame (NULL);
	}
	if (poLibrary) delete poLibrary;
	return bResult;
}
#endif /* ifndef _WIN32 */

