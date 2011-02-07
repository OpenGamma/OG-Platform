/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.connector;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Simulates the Main class being loaded up within the JVM service wrapper. This is
 * equivalent to JVMTest.cpp/TestStartStop
 */
public class MainTest {

  @Test
  public void testStartStop() {
    assertTrue(Main.svcStart());
    assertTrue(Main.svcAccept("TestUser", "Foo", "Bar"));
    assertTrue(Main.svcStop());
  }

}
