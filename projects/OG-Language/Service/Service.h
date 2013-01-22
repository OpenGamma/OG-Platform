/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_service_service_h
#define __inc_og_language_service_service_h

#define SERVICE_RUN_INLINE	1
#ifdef _WIN32
#define SERVICE_RUN_SCM		2
#else /* ifdef _WIN32 */
#define SERVICE_RUN_DAEMON	3
#endif /* ifdef _WIN32 */

void ServiceStop (bool bForce);
void ServiceSuspend ();
void ServiceRun (int nReason);
bool ServiceRunning ();
void ServiceConfigure ();
#ifndef _WIN32
bool ServiceTestJVM (const TCHAR *pszLibraryPath);
#endif /* ifndef _WIN32 */

#endif /* ifndef __inc_og_language_service_service_h */
