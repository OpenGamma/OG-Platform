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
  public static final double PRICE = 96;

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeSettlmentDate() {
    new InterestRateFuture(-2, 0.25, PRICE, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testYearFraction() {
    new InterestRateFuture(2, -0.25, PRICE, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativePrice() {
    new InterestRateFuture(3, 0.25, -87, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidePrice() {
    new InterestRateFuture(3, 0.25, 101, CURVE_NAME);
  }

  @Test
  public void test() {
    final double settlement = 1.45;
    final double yearFrac = 1. / 12;
    final InterestRateFuture fra = new InterestRateFuture(settlement, yearFrac, PRICE, CURVE_NAME);
    assertEquals(fra.getSettlementDate(), settlement, 0);
    assertEquals(fra.getYearFraction(), yearFrac, 0);
    assertEquals(fra.getPrice(), PRICE, 0);
    InterestRateFuture other = new InterestRateFuture(settlement, yearFrac, PRICE, CURVE_NAME);
    assertEquals(fra, other);
    assertEquals(fra.hashCode(), other.hashCode());
    other = new InterestRateFuture(settlement + 0.01, yearFrac, PRICE, CURVE_NAME);
    assertFalse(other.equals(fra));
    other = new InterestRateFuture(settlement, yearFrac - 0.01, PRICE, CURVE_NAME);
    assertFalse(other.equals(fra));
    other = new InterestRateFuture(settlement, yearFrac, PRICE - 1, CURVE_NAME);
    assertFalse(other.equals(fra));
    other = new InterestRateFuture(settlement, yearFrac, PRICE, "different curve");
    assertFalse(other.equals(fra));
  }
}
