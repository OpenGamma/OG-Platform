/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Util/NamedPipe.cpp

#include "Util/NamedPipe.h"

LOGGING (com.opengamma.language.util.NamedPipeTest);

static void ClientToServerComplete () {
	TODO (__FUNCTION__);
}

static void ClientToServerTimeout () {
	TODO (__FUNCTION__);
}

static void ServerToClientComplete () {
	TODO (__FUNCTION__);
}

static void ServerToClientTimeout () {
	TODO (__FUNCTION__);
}

static void ClientToServerClose () {
	TODO (__FUNCTION__);
}

static void ServerToClientClose () {
	TODO (__FUNCTION__);
}

static void ClientToServerLazyClose () {
	TODO (__FUNCTION__);
}

static void ServerToClientLazyClose () {
	TODO (__FUNCTION__);
}

static void ClientToServerCancelLazyClose () {
	TODO (__FUNCTION__);
}

static void ServerToClientCancelLazyClose () {
	TODO (__FUNCTION__);
}

BEGIN_TESTS (NamedPipeTest)
	TEST (ClientToServerComplete)
	TEST (ClientToServerTimeout)
	TEST (ServerToClientComplete)
	TEST (ServerToClientTimeout)
	TEST (ClientToServerClose)
	TEST (ServerToClientClose)
	TEST (ClientToServerLazyClose)
	TEST (ServerToClientLazyClose)
	TEST (ClientToServerCancelLazyClose)
	TEST (ServerToClientCancelLazyClose)
END_TESTS