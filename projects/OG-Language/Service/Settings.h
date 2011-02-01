/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_service_settings_h
#define __inc_og_language_service_settings_h

// Runtime configuration options

#include "Public.h"

#ifdef _WIN32
#define SETTINGS_JVM_LIBRARY				TEXT ("jvmDLL")
#else
#define SETTINGS_JVM_LIBRARY				TEXT ("jvmSO")
#endif
#define SETTINGS_CONNECTION_PIPE			SERVICE_SETTINGS_CONNECTION_PIPE
#define SETTINGS_CONNECTION_TIMEOUT			TEXT ("connectionTimeout")
#define SETTINGS_JAR_PATH					TEXT ("jars")
#define SETTINGS_ANNOTATION_CACHE			TEXT ("annotationCache")
#define SETTINGS_LOG_CONFIGURATION			TEXT ("serviceLogConfiguration")
#define SETTINGS_IDLE_TIMEOUT				TEXT ("idleTimeout")
#define SETTINGS_SERVICE_NAME				TEXT ("serviceName")
#ifdef _WIN32
#define SETTINGS_SERVICE_SDDL				TEXT ("serviceSDDL")
#endif

class CSettings : public CAbstractSettings {
private:
	TCHAR *m_pszDefaultJvmLibrary;
	TCHAR *m_pszDefaultJarPath;
	const TCHAR *GetJvmLibrary (const TCHAR *pszDefault) { return Get (SETTINGS_JVM_LIBRARY, pszDefault); }
	const TCHAR *GetConnectionPipe (const TCHAR *pszDefault) { return Get (SETTINGS_CONNECTION_PIPE, pszDefault); }
	unsigned long GetConnectionTimeout (unsigned long dwDefault) { return Get (SETTINGS_CONNECTION_TIMEOUT, dwDefault); }
	const TCHAR *GetJarPath (const TCHAR *pszDefault) { return Get (SETTINGS_JAR_PATH, pszDefault); }
	const TCHAR *GetAnnotationCache (const TCHAR *pszDefault) { return Get (SETTINGS_ANNOTATION_CACHE, pszDefault); }
	const TCHAR *GetLogConfiguration (const TCHAR *pszDefault) { return Get (SETTINGS_LOG_CONFIGURATION, pszDefault); }
	unsigned long GetIdleTimeout (unsigned long dwDefault) { return Get (SETTINGS_IDLE_TIMEOUT, dwDefault); }
	const TCHAR *GetServiceName (const TCHAR *pszDefault) { return Get (SETTINGS_SERVICE_NAME, pszDefault); }
#ifdef _WIN32
	const TCHAR *GetServiceSDDL (const TCHAR *pszDefault) { return Get (SETTINGS_SERVICE_SDDL, pszDefault); }
#endif
public:
	CSettings ();
	~CSettings ();
	const TCHAR *GetJvmLibrary ();
	const TCHAR *GetConnectionPipe ();
	unsigned long GetConnectionTimeout ();
	const TCHAR *GetJarPath ();
	const TCHAR *GetAnnotationCache ();
	const TCHAR *GetLogConfiguration ();
	unsigned long GetIdleTimeout ();
	const TCHAR *GetServiceName ();
#ifdef _WIN32
	const TCHAR *GetServiceSDDL ();
#endif /* ifdef _WIN32 */
};

#endif /* ifndef __inc_og_language_service_settings_h */