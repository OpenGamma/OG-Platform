/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class FixedCouponPaymentTest {
  private static final double PAYMENT_TIME = 0.67;
  private static final double YEAR_FRACTION = 0.253;
  private static final double COUPON = 0.05;
  private static final String CURVE_NAME = "vfsmngsdjkflsadfk";
  private static final FixedCouponPayment PAYMENT = new FixedCouponPayment(PAYMENT_TIME, YEAR_FRACTION, COUPON, CURVE_NAME);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativePaymentTime() {
    new FixedCouponPayment(-1, YEAR_FRACTION, COUPON, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeYearFraction() {
    new FixedCouponPayment(PAYMENT_TIME, -0.25, COUPON, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurveName() {
    new FixedCouponPayment(PAYMENT_TIME, YEAR_FRACTION, COUPON, null);
  }

  @Test
  public void testHashCodeAndEquals() {
    FixedCouponPayment other = new FixedCouponPayment(PAYMENT_TIME, YEAR_FRACTION, COUPON, CURVE_NAME);
    assertEquals(other, PAYMENT);
    assertEquals(other.hashCode(), PAYMENT.hashCode());
    other = new FixedCouponPayment(PAYMENT_TIME + 0.01, YEAR_FRACTION, COUPON, CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new FixedCouponPayment(PAYMENT_TIME, YEAR_FRACTION * 2, COUPON / 2, CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new FixedCouponPayment(PAYMENT_TIME, YEAR_FRACTION, COUPON * 2, CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new FixedCouponPayment(PAYMENT_TIME, YEAR_FRACTION, COUPON, "dasdsgdfgf");
    assertFalse(other.equals(PAYMENT));
    other = new FixedCouponPayment(PAYMENT_TIME, 1.1, YEAR_FRACTION, COUPON, CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
  }

  @Test
  public void testGetters() {
    assertEquals(PAYMENT.getPaymentTime(), PAYMENT_TIME, 0);
    assertEquals(PAYMENT.getYearFraction(), YEAR_FRACTION, 0);
    assertEquals(PAYMENT.getCoupon(), COUPON, 0);
    assertEquals(PAYMENT.getNotional(), 1.0, 0);
    assertEquals(PAYMENT.getAmount(), COUPON * YEAR_FRACTION, 0);
    assertEquals(PAYMENT.getFundingCurveName(), CURVE_NAME);
  }

}
