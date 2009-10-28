/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class ConfidenceIntervalTest {

  @Test
  public void test() {
    try {
      new ConfidenceInterval(1., 0., 2., -0.95);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new ConfidenceInterval(1., 0., 2., 1.95);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new ConfidenceInterval(1., 1.1, 2., 0.95);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new ConfidenceInterval(1., 0., 0.9, 0.95);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final ConfidenceInterval interval = new ConfidenceInterval(2., 1., 3., 0.95);
    assertFalse(interval.isWithinInterval(4.5));
    assertFalse(interval.isWithinInterval(-1));
    assertTrue(interval.isWithinInterval(2.5));
  }
}
