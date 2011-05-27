/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_service_settings_h
#define __inc_og_language_service_settings_h

// Runtime configuration options

#include "Public.h"

#define SETTINGS_ANNOTATION_CACHE			TEXT ("annotationCache")
#define SETTINGS_BUSY_TIMEOUT				TEXT ("busyTimeout")
#define SETTINGS_CONNECTION_PIPE			SERVICE_SETTINGS_CONNECTION_PIPE
#define SETTINGS_CONNECTION_TIMEOUT			TEXT ("connectionTimeout")
#define SETTINGS_IDLE_TIMEOUT				TEXT ("idleTimeout")
#define SETTINGS_JAR_PATH					TEXT ("jars")
#ifdef _WIN32
#define SETTINGS_JVM_LIBRARY				TEXT ("jvmDLL")
#else
#define SETTINGS_JVM_LIBRARY				TEXT ("jvmLibrary")
#endif
#define SETTINGS_LOG_CONFIGURATION			TEXT ("serviceLogConfiguration")
#define SETTINGS_SERVICE_NAME				SERVICE_SETTINGS_SERVICE_NAME
#ifdef _WIN32
#define SETTINGS_SERVICE_SDDL				TEXT ("serviceSDDL")
#endif

class CSettings : public CAbstractSettings {
private:
	const TCHAR *GetAnnotationCache (const TCHAR *pszDefault) const { return Get (SETTINGS_ANNOTATION_CACHE, pszDefault); }
	unsigned long GetBusyTimeout (unsigned long dwDefault) const { return Get (SETTINGS_BUSY_TIMEOUT, dwDefault); }
	const TCHAR *GetConnectionPipe (const TCHAR *pszDefault) const { return Get (SETTINGS_CONNECTION_PIPE, pszDefault); }
	unsigned long GetConnectionTimeout (unsigned long dwDefault) const { return Get (SETTINGS_CONNECTION_TIMEOUT, dwDefault); }
	unsigned long GetIdleTimeout (unsigned long dwDefault) const { return Get (SETTINGS_IDLE_TIMEOUT, dwDefault); }
	const TCHAR *GetJarPath (const CAbstractSettingProvider *poDefault) const { return Get (SETTINGS_JAR_PATH, poDefault); }
	const TCHAR *GetJvmLibrary (const CAbstractSettingProvider *poDefault) const { return Get (SETTINGS_JVM_LIBRARY, poDefault); }
	const TCHAR *GetLogConfiguration (const TCHAR *pszDefault) const { return Get (SETTINGS_LOG_CONFIGURATION, pszDefault); }
	const TCHAR *GetServiceName (const TCHAR *pszDefault) const { return Get (SETTINGS_SERVICE_NAME, pszDefault); }
#ifdef _WIN32
	const TCHAR *GetServiceSDDL (const TCHAR *pszDefault) const { return Get (SETTINGS_SERVICE_SDDL, pszDefault); }
#endif
public:
	const TCHAR *GetAnnotationCache () const;
	unsigned  long GetBusyTimeout () const;
	const TCHAR *GetConnectionPipe () const;
	unsigned long GetConnectionTimeout () const;
	unsigned long GetIdleTimeout () const;
	const TCHAR *GetJarPath () const;
	const TCHAR *GetJvmLibrary () const;
	const TCHAR *GetLogConfiguration () const;
	const TCHAR *GetServiceName () const;
#ifdef _WIN32
	const TCHAR *GetServiceSDDL () const;
#endif /* ifdef _WIN32 */
};

#endif /* ifndef __inc_og_language_service_settings_h */
