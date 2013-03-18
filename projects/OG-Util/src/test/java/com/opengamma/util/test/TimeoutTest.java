/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

/**
 * Tests the Timeout benchmark.
 */
@Test(groups = TestGroup.INTEGRATION)
public class TimeoutTest {

  public void testTimeoutMillis() {
    assertTrue(Timeout.standardTimeoutMillis() > 0);
  }

  public void testTimeoutNanos() {
    assertTrue(Timeout.standardTimeoutNanos() > 0);
  }

  public void testTimeoutSeconds() {
    assertTrue(Timeout.standardTimeoutSeconds() > 0);
  }

}
