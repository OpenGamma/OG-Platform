/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class FixedAnnuityTest {
  private static final double[] YEAR_FRACTIONS = new double[] {0.5, 0.5, 0.5, 0.5};
  private static final double NOTIONAL = 1000;
  private static final double COUPON_RATE = 0.05;
  private static final double[] COUPONS = new double[] {.05, .06, .07, .08};
  private static final double[] T = new double[] {0.5, 1, 1.5, 2};
  private static final String CURVE_NAME = "Name";
  // private static final FixedAnnuity ANNUITY1 = new FixedAnnuity(T, CURVE_NAME);
  // private static final FixedAnnuity ANNUITY2 = new FixedAnnuity(T, NOTIONAL, CURVE_NAME);
  private static final FixedAnnuity ANNUITY1 = new FixedAnnuity(T, NOTIONAL, new double[] {1, 1, 1, 1}, YEAR_FRACTIONS, CURVE_NAME);
  private static final FixedAnnuity ANNUITY2 = new FixedAnnuity(T, NOTIONAL, COUPON_RATE, CURVE_NAME);
  private static final FixedAnnuity ANNUITY3 = new FixedAnnuity(T, NOTIONAL, COUPON_RATE, YEAR_FRACTIONS, CURVE_NAME);
  private static final FixedAnnuity ANNUITY4 = new FixedAnnuity(T, NOTIONAL, COUPONS, CURVE_NAME);
  private static final FixedAnnuity ANNUITY5 = new FixedAnnuity(T, NOTIONAL, COUPONS, YEAR_FRACTIONS, CURVE_NAME);

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentTimes1() {
    new FixedAnnuity(null, NOTIONAL, COUPON_RATE, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentTimes2() {
    new FixedAnnuity(null, NOTIONAL, COUPON_RATE, YEAR_FRACTIONS, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentTimes3() {
    new FixedAnnuity(null, NOTIONAL, COUPONS, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentTimes4() {
    new FixedAnnuity(null, NOTIONAL, COUPONS, YEAR_FRACTIONS, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCoupons1() {
    new FixedAnnuity(T, NOTIONAL, null, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCoupons2() {
    new FixedAnnuity(T, NOTIONAL, null, YEAR_FRACTIONS, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullYearFrac1() {
    new FixedAnnuity(T, NOTIONAL, COUPON_RATE, null, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullYearFrac2() {
    new FixedAnnuity(T, NOTIONAL, COUPONS, null, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullName1() {
    new FixedAnnuity(T, NOTIONAL, COUPON_RATE, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullName2() {
    new FixedAnnuity(T, NOTIONAL, COUPON_RATE, YEAR_FRACTIONS, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullName3() {
    new FixedAnnuity(T, NOTIONAL, COUPONS, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullName4() {
    new FixedAnnuity(T, NOTIONAL, COUPONS, YEAR_FRACTIONS, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPaymentTimes1() {
    new FixedAnnuity(new double[0], NOTIONAL, COUPON_RATE, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPaymentTimes2() {
    new FixedAnnuity(new double[0], NOTIONAL, COUPON_RATE, YEAR_FRACTIONS, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPaymentTimes3() {
    new FixedAnnuity(new double[0], NOTIONAL, COUPONS, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPaymentTimes4() {
    new FixedAnnuity(new double[0], NOTIONAL, COUPONS, YEAR_FRACTIONS, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyCoupons1() {
    new FixedAnnuity(T, NOTIONAL, new double[0], CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyCoupons2() {
    new FixedAnnuity(T, NOTIONAL, new double[0], YEAR_FRACTIONS, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyYearFrac1() {
    new FixedAnnuity(T, NOTIONAL, COUPON_RATE, new double[0], CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyYearFrac2() {
    new FixedAnnuity(T, NOTIONAL, COUPONS, new double[0], CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongPaymentTimes2() {
    new FixedAnnuity(new double[] {1}, NOTIONAL, COUPON_RATE, YEAR_FRACTIONS, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongPaymentTimes3() {
    new FixedAnnuity(new double[] {1}, NOTIONAL, COUPONS, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongPaymentTimes4() {
    new FixedAnnuity(new double[] {1}, NOTIONAL, COUPONS, YEAR_FRACTIONS, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongCoupons1() {
    new FixedAnnuity(T, NOTIONAL, new double[] {1}, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongCoupons2() {
    new FixedAnnuity(T, NOTIONAL, new double[] {1}, YEAR_FRACTIONS, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongYearFrac1() {
    new FixedAnnuity(T, NOTIONAL, COUPON_RATE, new double[] {1}, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongYearFrac2() {
    new FixedAnnuity(T, NOTIONAL, COUPONS, new double[] {1}, CURVE_NAME);
  }

  @Test
  public void testGetters() {
    double[] payments = new double[T.length];
    for (int i = 0; i < T.length; i++) {
      payments[i] = NOTIONAL * COUPON_RATE * YEAR_FRACTIONS[i];
    }
    assertEquals(ANNUITY3.getFundingCurveName(), CURVE_NAME);
    assertEquals(ANNUITY3.getNumberOfPayments(), T.length);
    assertArrayEquals(ANNUITY2.getPaymentAmounts(), payments, 0);
    assertArrayEquals(ANNUITY3.getPaymentAmounts(), payments, 0);
    assertArrayEquals(ANNUITY3.getPaymentTimes(), T, 0);
    assertArrayEquals(ANNUITY3.getYearFractions(), new double[] {.5, .5, .5, .5}, 0);
    assertEquals(ANNUITY3.getNotional(), NOTIONAL, 0);
    assertEquals(ANNUITY3.getFundingCurveName(), CURVE_NAME);
  }

  @Test
  public void testEqualsAndHashCode() {
    FixedAnnuity other = new FixedAnnuity(T, NOTIONAL, COUPON_RATE, YEAR_FRACTIONS, CURVE_NAME);
    assertEquals(other, ANNUITY3);
    assertEquals(other.hashCode(), ANNUITY3.hashCode());
    assertEquals(ANNUITY2, ANNUITY2.withZeroSpread());
    assertEquals(ANNUITY1, ANNUITY5.withUnitCoupons());
    assertEquals(ANNUITY2, ANNUITY3);
    assertEquals(ANNUITY4, ANNUITY5);

    final double[] data = new double[] {1, 2, 3, 4};
    other = new FixedAnnuity(data, NOTIONAL, COUPONS, YEAR_FRACTIONS, CURVE_NAME);
    assertFalse(other.equals(ANNUITY5));
    other = new FixedAnnuity(T, NOTIONAL + 1, COUPONS, YEAR_FRACTIONS, CURVE_NAME);
    assertFalse(other.equals(ANNUITY5));
    other = new FixedAnnuity(T, NOTIONAL, data, YEAR_FRACTIONS, CURVE_NAME);
    assertFalse(other.equals(ANNUITY5));
    other = new FixedAnnuity(T, NOTIONAL, COUPONS, data, CURVE_NAME);
    assertFalse(other.equals(ANNUITY5));
    other = new FixedAnnuity(T, NOTIONAL, COUPONS, YEAR_FRACTIONS, "X");
    assertFalse(other.equals(ANNUITY5));
  }
}
