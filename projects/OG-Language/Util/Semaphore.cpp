/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Semaphores using Win32 or POSIX semaphores

#include "Semaphore.h"

#ifndef _WIN32
CMutex CSemaphore::s_oMaxValue;
#endif
