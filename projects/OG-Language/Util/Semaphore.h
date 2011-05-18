/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_semaphore_h
#define __inc_og_language_util_semaphore_h

// Semaphores using Win32 or POSIX semaphores

#ifndef _WIN32
#include <semaphore.h>
#include <time.h>
#include <string.h>
#include "Mutex.h"
#include "Error.h"
#endif /* ifndef _WIN32 */

#define MAX_SEMAPHORE_COUNT	0x7FFFFFFF

class CSemaphore {
private:
#ifdef _WIN32
	HANDLE m_hSemaphore;
#else
	sem_t m_semaphore;
	int m_nMaxValue;
	static CMutex s_oMaxValue;
#endif
public:
	// Note that semaphore construction failure will go unnoticed. If you can't create
	// a semaphore, the system is fairly broken and will probably crash soon anyway.
	CSemaphore (int nInitialValue = 0, int nMaxValue = MAX_SEMAPHORE_COUNT) {
#ifdef _WIN32
		m_hSemaphore = CreateSemaphore (NULL, nInitialValue, nMaxValue, NULL);
		assert (m_hSemaphore);
#else
		if (sem_init (&m_semaphore, 0, nInitialValue)) {
			assert (0);
			memset (&m_semaphore, 0, sizeof (m_semaphore));
			m_nMaxValue = 0;
		} else {
			m_nMaxValue = nMaxValue;
		}
#endif
	}
	~CSemaphore () {
#ifdef _WIN32
		CloseHandle (m_hSemaphore);
#else
		sem_destroy (&m_semaphore);
#endif
	}
	bool Signal () {
#ifdef _WIN32
		return ReleaseSemaphore (m_hSemaphore, 1, NULL) ? true : false;
#else
		if (m_nMaxValue < MAX_SEMAPHORE_COUNT) {
			s_oMaxValue.Enter ();
			int value;
			sem_getvalue (&m_semaphore, &value);
			if (value >= m_nMaxValue) {
				s_oMaxValue.Leave ();
				return false;
			}
			sem_post (&m_semaphore);
			s_oMaxValue.Leave ();
		} else {
			sem_post (&m_semaphore);
		}
		return true;
#endif
	}
	bool Wait (unsigned long timeout = 0xFFFFFFFF) {
#ifdef _WIN32
		switch (WaitForSingleObject (m_hSemaphore, timeout)) {
		case WAIT_ABANDONED :
			SetLastError (ERROR_ABANDONED_WAIT_0);
			return false;
		case WAIT_OBJECT_0 :
			return true;
		case WAIT_TIMEOUT :
			SetLastError (ERROR_TIMEOUT);
			return false;
		default :
			return false;
		}
#else
		if (timeout != 0xFFFFFFFF) {
			struct timespec ts;
			clock_gettime (CLOCK_REALTIME, &ts);
			ts.tv_sec += timeout / 1000;
			timeout %= 1000;
			if ((ts.tv_nsec += timeout * 1000000) >= 1000000000) {
				ts.tv_nsec -= 1000000000;
				ts.tv_sec++;
			}
			return !sem_timedwait (&m_semaphore, &ts);
		} else {
			return !sem_wait (&m_semaphore);
		}
#endif
	}
};

#endif /* ifndef __inc_og_language_util_semaphore_h */
