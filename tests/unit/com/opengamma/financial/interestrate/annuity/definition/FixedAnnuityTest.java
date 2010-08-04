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
  private static final double[] PAYMENTS = new double[] {0.05, 0.05, 0.05, 0.05};
  private static final double[] YEAR_FRACTIONS = new double[] {0.5, 0.5, 0.5, 0.5};
  private static final double NOTIONAL = 1;
  private static final double[] COUPONS = new double[] {.1, .1, .1, .1};
  private static final double[] T = new double[] {0.5, 1, 1.5, 2};
  private static final String CURVE_NAME = "Name";
  // private static final FixedAnnuity ANNUITY1 = new FixedAnnuity(T, CURVE_NAME);
  // private static final FixedAnnuity ANNUITY2 = new FixedAnnuity(T, NOTIONAL, CURVE_NAME);
  private static final FixedAnnuity ANNUITY3 = new FixedAnnuity(T, PAYMENTS, CURVE_NAME);
  private static final FixedAnnuity ANNUITY4 = new FixedAnnuity(T, NOTIONAL, COUPONS, CURVE_NAME);
  private static final FixedAnnuity ANNUITY5 = new FixedAnnuity(T, NOTIONAL, COUPONS, YEAR_FRACTIONS, CURVE_NAME);

  // @Test(expected = IllegalArgumentException.class)
  // public void testNullPayment1() {
  // new FixedAnnuity(null, CURVE_NAME);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testNullPayment2() {
  // new FixedAnnuity(null, NOTIONAL, CURVE_NAME);
  // }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPayment3() {
    new FixedAnnuity(null, PAYMENTS, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPayment4() {
    new FixedAnnuity(null, NOTIONAL, COUPONS, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPayment5() {
    new FixedAnnuity(null, NOTIONAL, COUPONS, YEAR_FRACTIONS, CURVE_NAME);
  }

  // @Test(expected = IllegalArgumentException.class)
  // public void testNullCurveName1() {
  // new FixedAnnuity(T, null);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testNullCurveName2() {
  // new FixedAnnuity(T, NOTIONAL, null);
  // }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurveName3() {
    new FixedAnnuity(T, PAYMENTS, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurveName4() {
    new FixedAnnuity(T, NOTIONAL, COUPONS, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurveName5() {
    new FixedAnnuity(T, NOTIONAL, COUPONS, YEAR_FRACTIONS, null);
  }

  // @Test(expected = IllegalArgumentException.class)
  // public void testEmptyPayment1() {
  // new FixedAnnuity(new double[0], CURVE_NAME);
  // }
  //
  // @Test(expected = IllegalArgumentException.class)
  // public void testEmptyPayment2() {
  // new FixedAnnuity(new double[0], NOTIONAL, CURVE_NAME);
  // }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPayment3() {
    new FixedAnnuity(new double[0], PAYMENTS, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPayment4() {
    new FixedAnnuity(new double[0], NOTIONAL, COUPONS, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPayment5() {
    new FixedAnnuity(new double[0], NOTIONAL, COUPONS, YEAR_FRACTIONS, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongPaymentTimes1() {
    new FixedAnnuity(new double[] {1}, PAYMENTS, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongPaymentTimes2() {
    new FixedAnnuity(new double[] {1}, NOTIONAL, COUPONS, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongPaymentTimes3() {
    new FixedAnnuity(new double[] {1}, NOTIONAL, COUPONS, YEAR_FRACTIONS, CURVE_NAME);
  }

  @Test
  public void testGetters() {
    assertEquals(ANNUITY3.getFundingCurveName(), CURVE_NAME);
    assertEquals(ANNUITY3.getNumberOfPayments(), PAYMENTS.length);
    assertArrayEquals(ANNUITY3.getPaymentAmounts(), PAYMENTS, 0);
    assertArrayEquals(ANNUITY3.getPaymentTimes(), T, 0);
    assertArrayEquals(ANNUITY3.getYearFractions(), new double[] {.5, .5, .5, .5}, 0);
  }

  @Test
  public void testEqualsAndHashCode() {
    FixedAnnuity other = new FixedAnnuity(T, PAYMENTS, CURVE_NAME);
    assertEquals(other, ANNUITY3);
    assertEquals(other.hashCode(), ANNUITY3.hashCode());
    // assertEquals(ANNUITY1, new FixedAnnuity(T, 1, CURVE_NAME));
    // assertEquals(ANNUITY2, new FixedAnnuity(T, new double[] {0.5, 0.5, 0.5, 0.5}, CURVE_NAME));
    assertEquals(ANNUITY4, ANNUITY3);
    assertEquals(ANNUITY5, ANNUITY3);
    // assertEquals(ANNUITY1, ANNUITY2.toUnitCouponFixedAnnuity(NOTIONAL));
    // assertEquals(ANNUITY1, ANNUITY3.toUnitCouponFixedAnnuity(NOTIONAL));
    // assertEquals(ANNUITY1, ANNUITY4.toUnitCouponFixedAnnuity(NOTIONAL));
    // assertEquals(ANNUITY1, ANNUITY5.toUnitCouponFixedAnnuity(NOTIONAL));
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
