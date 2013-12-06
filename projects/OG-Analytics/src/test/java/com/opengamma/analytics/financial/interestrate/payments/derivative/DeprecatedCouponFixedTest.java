/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test of fixed coupon class.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class DeprecatedCouponFixedTest {
  private static final double PAYMENT_TIME = 0.67;
  private static final double YEAR_FRACTION = 0.253;
  private static final double COUPON = 0.05;
  private static final String CURVE_NAME = "vfsmngsdjkflsadfk";
  private static final Currency CUR = Currency.EUR;
  private static final CouponFixed PAYMENT = new CouponFixed(CUR, PAYMENT_TIME, CURVE_NAME, YEAR_FRACTION, COUPON);

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
  public void testWithNotional() {
    final double notional = 1000000;
    final CouponFixed coupon = new CouponFixed(CUR, PAYMENT_TIME, CURVE_NAME, YEAR_FRACTION, notional, COUPON);
    final CouponFixed expected = new CouponFixed(CUR, PAYMENT_TIME, CURVE_NAME, YEAR_FRACTION, notional + 100, COUPON);
    assertEquals(expected, coupon.withNotional(notional + 100));
  }

  @Test
  public void testWithRate() {
    final double notional = 10000;
    final double rate = COUPON + 1;
    final CouponFixed coupon = new CouponFixed(CUR, PAYMENT_TIME, CURVE_NAME, YEAR_FRACTION, notional, COUPON);
    final CouponFixed expected = new CouponFixed(CUR, PAYMENT_TIME, CURVE_NAME, YEAR_FRACTION, notional, rate);
    assertEquals(expected, coupon.withRate(rate));
  }

  @Test
  public void testWithRateShifted() {
    final double notional = 10000;
    final double spread = 0.01;
    final double rate = COUPON + spread;
    final CouponFixed coupon = new CouponFixed(CUR, PAYMENT_TIME, CURVE_NAME, YEAR_FRACTION, notional, COUPON);
    final CouponFixed expected = new CouponFixed(CUR, PAYMENT_TIME, CURVE_NAME, YEAR_FRACTION, notional, rate);
    assertEquals(expected, coupon.withRateShifted(spread));
  }

  @Test
  public void testWithUnitCoupon() {
    final double notional = 1000000;
    final CouponFixed coupon = new CouponFixed(CUR, PAYMENT_TIME, CURVE_NAME, YEAR_FRACTION, notional, COUPON);
    final CouponFixed expected = new CouponFixed(CUR, PAYMENT_TIME, CURVE_NAME, YEAR_FRACTION, notional, 1);
    assertEquals(expected, coupon.withUnitCoupon());
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
