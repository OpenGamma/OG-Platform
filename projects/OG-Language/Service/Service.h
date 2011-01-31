/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_service_service_h
#define __inc_og_language_service_service_h

// Main service control functions

void ServiceInit ();
void ServiceDone ();
void ServiceStop (bool bForce);
void ServiceSuspend ();
void ServiceRun ();

#endif /* ifndef __inc_og_language_service_service_h */