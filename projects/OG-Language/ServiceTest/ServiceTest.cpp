/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Service/Service.cpp

#include "Service/Public.h"
#include "Service/Service.h"
#include "Service/Settings.h"

LOGGING (com.opengamma.language.service.ServiceTest);

#define TIMEOUT_START		30000
#define TIMEOUT_STOP		1000
#define TIMEOUT_CONNECT		1000
#define TIMEOUT_JOIN		3000

class CServiceRunThread : public CThread {
public:
	CServiceRunThread () : CThread () {
		ASSERT (Start ());
	}
	void Run () {
		LOGINFO (TEXT ("Starting service"));
		ServiceRun (SERVICE_RUN_INLINE);
		LOGINFO (TEXT ("Service stopped"));
	}
};

class CServiceClientThread : public CThread {
public:
	CServiceClientThread () : CThread () {
		ASSERT (Start ());
	}
	void Run () {
		CSettings settings;
		ASSERT (settings.GetConnectionPipe ());
		LOGDEBUG (TEXT ("Connecting to ") << settings.GetConnectionPipe ());
		CNamedPipe *poPipe = CNamedPipe::ClientWrite (settings.GetConnectionPipe ());
		ASSERT (poPipe);
		LOGDEBUG (TEXT ("Client connected"));
		PJAVACLIENT_CONNECT pjcc = JavaClientCreate (TEXT ("User"), TEXT ("Foo"), TEXT ("Bar"));
		LOGDEBUG (TEXT ("Writing connection packet"));
		ASSERT (poPipe->Write (pjcc, pjcc->cbSize, TIMEOUT_CONNECT) == (size_t)pjcc->cbSize);
		LOGDEBUG (TEXT ("Connection packet written"));
		free (pjcc);
		LOGDEBUG (TEXT ("Disconnecting"));
		delete poPipe;
	}
};

static void RunConnectStop () {
	CThread *poService = new CServiceRunThread ();
	int n;
	LOGDEBUG (TEXT ("Waiting for service to enter running state"));
	for (n = 0; !ServiceRunning () && (n < TIMEOUT_START / 100); n++) {
		CThread::Sleep (100);
	}
	ASSERT (ServiceRunning ());
	LOGDEBUG (TEXT ("Simulating client connection"));
	CThread *poClient = new CServiceClientThread ();
	ASSERT (CThread::WaitAndRelease (poClient, TIMEOUT_JOIN));
	// Need to pause for the JVM to reject the connection
	CThread::Sleep (TIMEOUT_CONNECT);
	LOGDEBUG (TEXT ("Stopping service"));
	ServiceStop (true);
	LOGDEBUG (TEXT ("Waiting for service to leave running state"));
	for (n = 0; ServiceRunning () && (n < TIMEOUT_STOP / 100); n++) {
		CThread::Sleep (100);
	}
	ASSERT (!ServiceRunning ());
	ASSERT (poService->Wait (TIMEOUT_JOIN));
	CThread::Release (poService);
}

//#define IGNORE_TEST // Comment out this line to run these tests. JVM limitations mean that JVMTest will not pass if ServiceTest is run

#ifndef IGNORE_TEST
BEGIN_TESTS (ServiceTest)
	TEST (RunConnectStop)
END_TESTS
#endif /* ifndef IGNORE_TEST */
