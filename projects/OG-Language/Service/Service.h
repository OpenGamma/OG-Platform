/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_service_service_h
#define __inc_og_language_service_service_h

// Main service control functions

#define SERVICE_RUN_INLINE	1
#ifdef _WIN32
#define SERVICE_RUN_SCM		2
#endif /* ifdef _WIN32 */

void ServiceInit ();
void ServiceDone ();
void ServiceStop (bool bForce);
void ServiceSuspend ();
void ServiceRun (int nReason);

#endif /* ifndef __inc_og_language_service_service_h */