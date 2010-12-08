/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.financial.interestrate.payments.FixedCouponPayment;

/**
 * 
 */
public class FixedCouponAnnuityTest {
  private static final double[] PAYMENT_TIMES = new double[] {0.5, 1, 1.5, 2, 2.5, 3};
  private static final double NOTIONAL = 1000;
  private static final double COUPON_RATE = 0.05;
  private static final String CURVE_NAME = "A";
  private static final double[] YEAR_FRACTIONS = new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
  private static final FixedCouponPayment[] PAYMENTS;
  private static final double DIFF = 0.02;
  private static final FixedCouponPayment[] HIGHER;

  static {
    final int n = PAYMENT_TIMES.length;
    PAYMENTS = new FixedCouponPayment[n];
    HIGHER = new FixedCouponPayment[n];
    for (int i = 0; i < n; i++) {
      PAYMENTS[i] = new FixedCouponPayment(PAYMENT_TIMES[i], NOTIONAL, YEAR_FRACTIONS[i], COUPON_RATE, CURVE_NAME);
      HIGHER[i] = new FixedCouponPayment(PAYMENT_TIMES[i], NOTIONAL, YEAR_FRACTIONS[i], COUPON_RATE + DIFF, CURVE_NAME);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentTimes() {
    new FixedCouponAnnuity(null, NOTIONAL, COUPON_RATE, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPaymentTimes() {
    new FixedCouponAnnuity(new double[0], NOTIONAL, COUPON_RATE, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullYearFractions() {
    new FixedCouponAnnuity(PAYMENT_TIMES, NOTIONAL, COUPON_RATE, null, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyYearFractions() {
    new FixedCouponAnnuity(PAYMENT_TIMES, NOTIONAL, COUPON_RATE, new double[0], CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurveName() {
    new FixedCouponAnnuity(PAYMENT_TIMES, NOTIONAL, COUPON_RATE, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongArrayLength() {
    new FixedCouponAnnuity(new double[] {1, 2, 3}, NOTIONAL, COUPON_RATE, YEAR_FRACTIONS, CURVE_NAME);
  }

  @Test
  public void testConstructors() {
    final FixedCouponAnnuity annuity = new FixedCouponAnnuity(PAYMENTS);
    assertFalse(annuity.equals(new FixedCouponAnnuity(PAYMENT_TIMES, COUPON_RATE, CURVE_NAME)));
    assertEquals(new FixedCouponAnnuity(PAYMENT_TIMES, 1, COUPON_RATE, CURVE_NAME), new FixedCouponAnnuity(PAYMENT_TIMES, COUPON_RATE, CURVE_NAME));
    assertEquals(annuity, new FixedCouponAnnuity(PAYMENT_TIMES, NOTIONAL, COUPON_RATE, CURVE_NAME));
    assertEquals(annuity, new FixedCouponAnnuity(PAYMENT_TIMES, NOTIONAL, COUPON_RATE, YEAR_FRACTIONS, CURVE_NAME));
  }

  @Test
  public void testWithRate() {
    final FixedCouponAnnuity annuity = new FixedCouponAnnuity(PAYMENTS);
    assertFalse(annuity.withRate(COUPON_RATE) == annuity);
    assertEquals(annuity.withRate(COUPON_RATE), annuity);
    assertEquals(annuity.withRate(COUPON_RATE + DIFF), new FixedCouponAnnuity(HIGHER));
  }
}
