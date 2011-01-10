/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
  public void testNegativeSettlementDate() {
    new InterestRateFuture(-2, 1.0, 0.25, PRICE, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeFixingDate() {
    new InterestRateFuture(0.423, -0.2332, 1.0, 0.25, 0.25, PRICE, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeMaturity() {
    new InterestRateFuture(0.423, 0.425, -1.0, 0.25, 0.25, PRICE, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testYearFraction() {
    new InterestRateFuture(1, 2, -0.25, PRICE, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeValueYearFrac() {
    new InterestRateFuture(0.423, 0.425, 1.0, 0.25, -0.25, PRICE, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativePrice() {
    new InterestRateFuture(3, 3.5, 0.25, -87, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidPrice() {
    new InterestRateFuture(3, 3.25, 0.25, 101, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSettlementBeforeFixing() {
    new InterestRateFuture(0.423, 0.424, 1.0, 0.25, 0.25, PRICE, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFixingAfterMaturity() {
    new InterestRateFuture(0.423, 0.425, 0.4, 0.25, 0.25, PRICE, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurveName() {
    new InterestRateFuture(0.423, 0.425, 1.0, 0.25, 0.25, PRICE, null);
  }

  @Test
  public void test() {
    final double settlement = 1.45;
    final double fixing = 1.44;
    final double maturity = 1.71;
    final double indexYearFrac = 0.2456;
    final double valueYearFrac = 0.25;
    final InterestRateFuture future = new InterestRateFuture(settlement, fixing, maturity, indexYearFrac, valueYearFrac, PRICE, CURVE_NAME);
    assertEquals(future.getSettlementDate(), settlement, 0);
    assertEquals(future.getFixingDate(), fixing, 0);
    assertEquals(future.getMaturity(), maturity, 0);
    assertEquals(future.getIndexYearFraction(), indexYearFrac, 0);
    assertEquals(future.getValueYearFraction(), valueYearFrac, 0);
    assertEquals(future.getPrice(), PRICE, 0);
    assertEquals(future.getCurveName(), CURVE_NAME);

    InterestRateFuture other = new InterestRateFuture(settlement, fixing, maturity, indexYearFrac, valueYearFrac, PRICE, CURVE_NAME);
    assertEquals(future, other);
    assertEquals(future.hashCode(), other.hashCode());
    other = new InterestRateFuture(settlement, maturity, indexYearFrac, PRICE, CURVE_NAME);
    assertFalse(other.equals(future));
    other = new InterestRateFuture(settlement - 0.01, fixing, maturity, indexYearFrac, valueYearFrac, PRICE, CURVE_NAME);
    assertFalse(other.equals(future));
    other = new InterestRateFuture(settlement, fixing + 0.01, maturity, indexYearFrac, valueYearFrac, PRICE, CURVE_NAME);
    assertFalse(other.equals(future));
    other = new InterestRateFuture(settlement, fixing, maturity + 0.01, indexYearFrac, valueYearFrac, PRICE, CURVE_NAME);
    assertFalse(other.equals(future));
    other = new InterestRateFuture(settlement, fixing, maturity, indexYearFrac + 0.001, valueYearFrac, PRICE, CURVE_NAME);
    assertFalse(other.equals(future));
    other = new InterestRateFuture(settlement, fixing, maturity, indexYearFrac, valueYearFrac - 0.001, PRICE, CURVE_NAME);
    assertFalse(other.equals(future));
    other = new InterestRateFuture(settlement, fixing, maturity, indexYearFrac, valueYearFrac, PRICE - 1, CURVE_NAME);
    assertFalse(other.equals(future));
    other = new InterestRateFuture(settlement, fixing, maturity, indexYearFrac, valueYearFrac, PRICE, "different curve");
    assertFalse(other.equals(future));
  }
}
