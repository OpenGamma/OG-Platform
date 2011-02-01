/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Util/TimeoutIO.cpp

#include "Util/TimeoutIO.h"

LOGGING (com.opengamma.language.util.TimeoutIOTest);

static void WriteComplete () {
	TODO (__FUNCTION__);
}

static void WriteTimeout () {
	TODO (__FUNCTION__);
}

static void ReadComplete () {
	TODO (__FUNCTION__);
}

static void ReadTimeout () {
	TODO (__FUNCTION__);
}

static void ReaderClose () {
	TODO (__FUNCTION__);
}

static void WriterClose () {
	TODO (__FUNCTION__);
}

static void ReaderLazyClose () {
	TODO (__FUNCTION__);
}

static void WriterLazyClose () {
	TODO (__FUNCTION__);
}

static void ReaderCancelLazyClose () {
	TODO (__FUNCTION__);
}

static void WriterCancelLazyClose () {
	TODO (__FUNCTION__);
}

BEGIN_TESTS (TimeoutIOTest)
	TEST (WriteComplete)
	TEST (WriteTimeout)
	TEST (ReadComplete)
	TEST (ReadTimeout)
	TEST (ReaderClose)
	TEST (WriterClose)
	TEST (ReaderLazyClose)
	TEST (WriterLazyClose)
	TEST (ReaderCancelLazyClose)
	TEST (WriterCancelLazyClose)
END_TESTS