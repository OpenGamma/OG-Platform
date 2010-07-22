/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class InterestRateFutureTest {
  public static final String CURVE_NAME = "test";

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeStart() {
    new InterestRateFuture(-2, 2, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeEnd() {
    new InterestRateFuture(2, -2, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStartAfterEnd() {
    new InterestRateFuture(3, 2, CURVE_NAME);
  }

  @Test
  public void test() {
    final double start = 12;
    final double end = 15;
    final InterestRateFuture fra = new InterestRateFuture(start, end, CURVE_NAME);
    assertEquals(fra.getStartTime(), start, 0);
    assertEquals(fra.getEndTime(), end, 0);
    InterestRateFuture other = new InterestRateFuture(start, end, CURVE_NAME);
    assertEquals(fra, other);
    assertEquals(fra.hashCode(), other.hashCode());
    other = new InterestRateFuture(start, end + 1, CURVE_NAME);
    assertFalse(other.equals(fra));
    other = new InterestRateFuture(start + 1, end, CURVE_NAME);
    assertFalse(other.equals(fra));
  }
}
