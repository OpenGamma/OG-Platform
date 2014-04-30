/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.annuity.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FixedCouponAnnuityTest {
  private static final double[] PAYMENT_TIMES = new double[] {0.5, 1, 1.5, 2, 2.5, 3};
  private static final double NOTIONAL = 1000;
  private static final double COUPON_RATE = 0.05;
  private static final double[] YEAR_FRACTIONS = new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
  private static final CouponFixed[] PAYMENTS;
  private static final double DIFF = 0.02;
  private static final CouponFixed[] HIGHER;
  private static final Currency CUR = Currency.EUR;

  static {
    final int n = PAYMENT_TIMES.length;
    PAYMENTS = new CouponFixed[n];
    HIGHER = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      PAYMENTS[i] = new CouponFixed(CUR, PAYMENT_TIMES[i], YEAR_FRACTIONS[i], NOTIONAL, COUPON_RATE);
      HIGHER[i] = new CouponFixed(CUR, PAYMENT_TIMES[i], YEAR_FRACTIONS[i], NOTIONAL, COUPON_RATE + DIFF);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentTimes() {
    new AnnuityCouponFixed(CUR, null, NOTIONAL, COUPON_RATE, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyPaymentTimes() {
    new AnnuityCouponFixed(CUR, new double[0], NOTIONAL, COUPON_RATE, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYearFractions() {
    new AnnuityCouponFixed(CUR, PAYMENT_TIMES, NOTIONAL, COUPON_RATE, (double[]) null, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyYearFractions() {
    new AnnuityCouponFixed(CUR, PAYMENT_TIMES, NOTIONAL, COUPON_RATE, new double[0], true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongArrayLength() {
    new AnnuityCouponFixed(CUR, new double[] {1, 2, 3}, NOTIONAL, COUPON_RATE, YEAR_FRACTIONS, true);
  }

  @Test
  public void testConstructors() {
    final AnnuityCouponFixed annuity = new AnnuityCouponFixed(PAYMENTS);
    assertFalse(annuity.equals(new AnnuityCouponFixed(CUR, PAYMENT_TIMES, COUPON_RATE, false)));
    assertEquals(new AnnuityCouponFixed(CUR, PAYMENT_TIMES, 1, COUPON_RATE, true), new AnnuityCouponFixed(CUR, PAYMENT_TIMES, COUPON_RATE, true));
    assertEquals(annuity, new AnnuityCouponFixed(CUR, PAYMENT_TIMES, NOTIONAL, COUPON_RATE, false));
    assertEquals(annuity, new AnnuityCouponFixed(CUR, PAYMENT_TIMES, NOTIONAL, COUPON_RATE, YEAR_FRACTIONS, false));
  }
  //
  // @Test
  // public void testWithRate() {
  // final FixedCouponAnnuity annuity = new FixedCouponAnnuity(PAYMENTS);
  // assertFalse(annuity.withRate(COUPON_RATE) == annuity);
  // assertEquals(annuity.withRate(COUPON_RATE), annuity);
  // assertEquals(annuity.withRate(COUPON_RATE + DIFF), new FixedCouponAnnuity(HIGHER));
  // }
}
