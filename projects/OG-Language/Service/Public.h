/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_service_public_h
#define __inc_og_language_service_public_h

#define SERVICE_SETTINGS_CONNECTION_PIPE	TEXT ("connectionPipe")
#define SERVICE_SETTINGS_SERVICE_NAME		TEXT ("serviceName")
#ifndef _WIN32
#define SERVICE_SETTINGS_QUERY_CMD			TEXT ("serviceQuery")
#define SERVICE_SETTINGS_START_CMD			TEXT ("serviceStart")
#define SERVICE_SETTINGS_STOP_CMD			TEXT ("serviceStop")
#endif /* ifndef _WIN32 */

const TCHAR *ServiceDefaultConnectionPipe ();
const TCHAR *ServiceDefaultServiceName ();
#ifndef _WIN32
TCHAR *ServiceCreateQueryCmd (const TCHAR *pszName);
TCHAR *ServiceCreateStartCmd (const TCHAR *pszName);
TCHAR *ServiceCreateStopCmd (const TCHAR *pszName);
#endif /* ifndef _WIN32 */

#include "ClientConnect.h"

#endif /* ifndef __inc_og_language_service_public_h */
