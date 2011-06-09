/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Semaphore.h"

#ifndef _WIN32
/// Critical section to allow maximum signal count to be implemented
CMutex CSemaphore::s_oMaxValue;
#endif
