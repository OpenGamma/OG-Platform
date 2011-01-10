/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests the Timeout benchmark.
 */
public class TimeoutTest {

  @Test
  public void testTimeoutMillis() {
    assertTrue(Timeout.standardTimeoutMillis() > 0);
  }

  @Test
  public void testTimeoutNanos() {
    assertTrue(Timeout.standardTimeoutNanos() > 0);
  }

  @Test
  public void testTimeoutSeconds() {
    assertTrue(Timeout.standardTimeoutSeconds() > 0);
  }

}
