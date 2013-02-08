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
#include <Util/Thread.h>
#include <Util/NamedPipe.h>

LOGGING (com.opengamma.language.service.ServiceTest);

#define TEST_USERNAME		TEXT ("Username")
#define TEST_CPP2JAVA		TEXT ("\\\\.\\pipe\\Foo")
#define TEST_JAVA2CPP		TEXT ("\\\\.\\pipe\\Bar")
#define TEST_LANGUAGE		TEXT ("test")

#define TIMEOUT_START		60000
#define TIMEOUT_STOP		10000
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
		ClientConnect cc;
		memset (&cc, 0, sizeof (cc));
		cc._userName = TEST_USERNAME;
		cc._CPPToJavaPipe = TEST_CPP2JAVA;
		cc._JavaToCPPPipe = TEST_JAVA2CPP;
		cc._languageID = TEST_LANGUAGE;
#ifdef _DEBUG
		cc._debug = FUDGE_TRUE;
#endif /* ifdef _DEBUG */
		FudgeMsg msg;
		ASSERT (ClientConnect_toFudgeMsg (&cc, &msg) == FUDGE_OK);
		FudgeMsgEnvelope env;
		ASSERT (FudgeMsgEnvelope_create (&env, 0, 0, 0, msg) == FUDGE_OK);
		fudge_byte *ptrBuffer;
		fudge_i32 cbBuffer;
		ASSERT (FudgeCodec_encodeMsg (env, &ptrBuffer, &cbBuffer) == FUDGE_OK);
		FudgeMsgEnvelope_release (env);
		FudgeMsg_release (msg);
		LOGDEBUG (TEXT ("Writing connection packet"));
		ASSERT (poPipe->Write (ptrBuffer, cbBuffer, TIMEOUT_CONNECT) == (size_t)cbBuffer);
		LOGDEBUG (TEXT ("Connection packet written"));
		delete ptrBuffer;
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
	// Need to pause for the JVM to acknowledge the connection
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

#define RUN_TESTS // JVM limitations mean that JVMTest will not pass if ServiceTest is run

#ifndef RUN_TESTS
#undef BEGIN_TESTS
#define BEGIN_TESTS MANUAL_TESTS
#endif /* ifndef RUN_TESTS */

BEGIN_TESTS (ServiceTest)
	TEST (RunConnectStop)
END_TESTS
