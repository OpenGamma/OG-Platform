/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.payments.CouponFixed;
import com.opengamma.util.money.Currency;

/**
 * Test of fixed coupon class.
 */
public class CouponFixedTest {
  private static final double PAYMENT_TIME = 0.67;
  private static final double YEAR_FRACTION = 0.253;
  private static final double COUPON = 0.05;
  private static final String CURVE_NAME = "vfsmngsdjkflsadfk";
  private static final Currency CUR = Currency.USD;
  private static final CouponFixed PAYMENT = new CouponFixed(CUR, PAYMENT_TIME, CURVE_NAME, YEAR_FRACTION, COUPON);

  //TODO: test constructor with notional.

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePaymentTime() {
    new CouponFixed(CUR, -1, CURVE_NAME, YEAR_FRACTION, COUPON);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeYearFraction() {
    new CouponFixed(CUR, PAYMENT_TIME, CURVE_NAME, -0.25, COUPON);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurveName() {
    new CouponFixed(CUR, PAYMENT_TIME, null, YEAR_FRACTION, COUPON);
  }

  @Test
  public void testHashCodeAndEquals() {
    CouponFixed other = new CouponFixed(CUR, PAYMENT_TIME, CURVE_NAME, YEAR_FRACTION, COUPON);
    assertEquals(other, PAYMENT);
    assertEquals(other.hashCode(), PAYMENT.hashCode());
    other = new CouponFixed(CUR, PAYMENT_TIME + 0.01, CURVE_NAME, YEAR_FRACTION, COUPON);
    assertFalse(other.equals(PAYMENT));
    other = new CouponFixed(CUR, PAYMENT_TIME, CURVE_NAME, YEAR_FRACTION * 2, COUPON / 2);
    assertFalse(other.equals(PAYMENT));
    other = new CouponFixed(CUR, PAYMENT_TIME, CURVE_NAME, YEAR_FRACTION, COUPON * 2);
    assertFalse(other.equals(PAYMENT));
    other = new CouponFixed(CUR, PAYMENT_TIME, "dasdsgdfgf", YEAR_FRACTION, COUPON);
    assertFalse(other.equals(PAYMENT));
    other = new CouponFixed(CUR, PAYMENT_TIME, CURVE_NAME, YEAR_FRACTION, 1.1, COUPON);
    assertFalse(other.equals(PAYMENT));
  }

  @Test
  public void testGetters() {
    assertEquals(PAYMENT.getPaymentTime(), PAYMENT_TIME, 0);
    assertEquals(PAYMENT.getPaymentYearFraction(), YEAR_FRACTION, 0);
    assertEquals(PAYMENT.getFixedRate(), COUPON, 0);
    assertEquals(PAYMENT.getNotional(), 1.0, 0);
    assertEquals(PAYMENT.getAmount(), COUPON * YEAR_FRACTION, 0);
    assertEquals(PAYMENT.getFundingCurveName(), CURVE_NAME);
  }

}
