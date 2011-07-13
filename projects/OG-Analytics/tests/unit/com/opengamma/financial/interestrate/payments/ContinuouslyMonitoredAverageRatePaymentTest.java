/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ContinuouslyMonitoredAverageRatePaymentTest {
  private static final double PAYMENT_TIME = 1;
  private static final double PAYMENT_YEAR_FRACTION = 0.98;
  private static final double RATE_YEAR_FRACTION = 0.99;
  private static final double START_TIME = 0.8;
  private static final double END_TIME = 0.9;
  private static final double SPREAD = 0.001;
  private static final double NOTIONAL = 1000;
  private static final String FUNDING_CURVE = "A";
  private static final String FORWARD_CURVE = "B";
  private static final Currency CUR = Currency.USD;

  CouponOIS CONT = new CouponOIS(CUR, PAYMENT_TIME, FUNDING_CURVE, PAYMENT_YEAR_FRACTION, NOTIONAL, RATE_YEAR_FRACTION, START_TIME,
      END_TIME, SPREAD, FORWARD_CURVE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePaymentTime() {
    new CouponOIS(CUR, -PAYMENT_TIME, FUNDING_CURVE, PAYMENT_YEAR_FRACTION, NOTIONAL, RATE_YEAR_FRACTION, START_TIME, END_TIME, SPREAD, FORWARD_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeStartTime() {
    new CouponOIS(CUR, PAYMENT_TIME, FUNDING_CURVE, PAYMENT_YEAR_FRACTION, NOTIONAL, RATE_YEAR_FRACTION, -START_TIME, END_TIME, SPREAD, FORWARD_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEndBeforeStart() {
    new CouponOIS(CUR, PAYMENT_TIME, FUNDING_CURVE, PAYMENT_YEAR_FRACTION, NOTIONAL, RATE_YEAR_FRACTION, END_TIME, START_TIME, SPREAD, FORWARD_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEndTimeBeforePaymentTime() {
    new CouponOIS(CUR, END_TIME, FUNDING_CURVE, PAYMENT_YEAR_FRACTION, NOTIONAL, RATE_YEAR_FRACTION, START_TIME, PAYMENT_TIME, SPREAD, FORWARD_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFundingCurveName() {
    new CouponOIS(CUR, PAYMENT_TIME, null, PAYMENT_YEAR_FRACTION, NOTIONAL, RATE_YEAR_FRACTION, START_TIME, END_TIME, SPREAD, FORWARD_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullForwardCurveName() {
    new CouponOIS(CUR, PAYMENT_TIME, FUNDING_CURVE, PAYMENT_YEAR_FRACTION, NOTIONAL, RATE_YEAR_FRACTION, START_TIME, END_TIME, SPREAD, null);
  }

  @Test
  public void testGetters() {
    final CouponOIS payment = new CouponOIS(CUR, PAYMENT_TIME, FUNDING_CURVE, PAYMENT_YEAR_FRACTION, NOTIONAL, RATE_YEAR_FRACTION,
        START_TIME, END_TIME, SPREAD, FORWARD_CURVE);
    assertEquals(payment.getEndTime(), END_TIME, 0);
    assertEquals(payment.getFundingCurveName(), FUNDING_CURVE);
    assertEquals(payment.getIndexCurveName(), FORWARD_CURVE);
    assertEquals(payment.getNotional(), NOTIONAL, 0);
    assertEquals(payment.getPaymentTime(), PAYMENT_TIME, 0);
    assertEquals(payment.getPaymentYearFraction(), PAYMENT_YEAR_FRACTION, 0);
    assertEquals(payment.getRateYearFraction(), RATE_YEAR_FRACTION, 0);
    assertEquals(payment.getSpread(), SPREAD, 0);
    assertEquals(payment.getStartTime(), START_TIME, 0);
  }

  @Test
  public void testHashCodeAndEquals() {
    final CouponOIS payment = new CouponOIS(CUR, PAYMENT_TIME, FUNDING_CURVE, PAYMENT_YEAR_FRACTION, NOTIONAL, RATE_YEAR_FRACTION,
        START_TIME, END_TIME, SPREAD, FORWARD_CURVE);
    CouponOIS other = new CouponOIS(CUR, PAYMENT_TIME, FUNDING_CURVE, PAYMENT_YEAR_FRACTION, NOTIONAL, RATE_YEAR_FRACTION, START_TIME,
        END_TIME, SPREAD, FORWARD_CURVE);
    assertEquals(payment, other);
    assertEquals(payment.hashCode(), other.hashCode());
    other = new CouponOIS(CUR, PAYMENT_TIME + 0.01, FUNDING_CURVE, PAYMENT_YEAR_FRACTION, NOTIONAL, RATE_YEAR_FRACTION, START_TIME, END_TIME, SPREAD, FORWARD_CURVE);
    assertFalse(other.equals(payment));
    other = new CouponOIS(CUR, PAYMENT_TIME, FUNDING_CURVE, PAYMENT_YEAR_FRACTION + 0.01, NOTIONAL, RATE_YEAR_FRACTION, START_TIME, END_TIME, SPREAD, FORWARD_CURVE);
    assertFalse(other.equals(payment));
    other = new CouponOIS(CUR, PAYMENT_TIME, FUNDING_CURVE, PAYMENT_YEAR_FRACTION, NOTIONAL + 10.0, RATE_YEAR_FRACTION, START_TIME, END_TIME, SPREAD, FORWARD_CURVE);
    assertFalse(other.equals(payment));
    other = new CouponOIS(CUR, PAYMENT_TIME, FUNDING_CURVE, PAYMENT_YEAR_FRACTION, NOTIONAL, RATE_YEAR_FRACTION + 0.1, START_TIME, END_TIME, SPREAD, FORWARD_CURVE);
    assertFalse(other.equals(payment));
    other = new CouponOIS(CUR, PAYMENT_TIME, FUNDING_CURVE, PAYMENT_YEAR_FRACTION, NOTIONAL, RATE_YEAR_FRACTION, START_TIME + 0.01, END_TIME, SPREAD, FORWARD_CURVE);
    assertFalse(other.equals(payment));
    other = new CouponOIS(CUR, PAYMENT_TIME, FUNDING_CURVE, PAYMENT_YEAR_FRACTION, NOTIONAL, RATE_YEAR_FRACTION, START_TIME, END_TIME + 0.1, SPREAD, FORWARD_CURVE);
    assertFalse(other.equals(payment));
    other = new CouponOIS(CUR, PAYMENT_TIME, FUNDING_CURVE, PAYMENT_YEAR_FRACTION, NOTIONAL, RATE_YEAR_FRACTION, START_TIME, END_TIME, SPREAD + 0.1, FORWARD_CURVE);
    assertFalse(other.equals(payment));
    other = new CouponOIS(CUR, PAYMENT_TIME, "false", PAYMENT_YEAR_FRACTION, NOTIONAL, RATE_YEAR_FRACTION, START_TIME, END_TIME, SPREAD, FORWARD_CURVE);
    assertFalse(other.equals(payment));
    other = new CouponOIS(CUR, PAYMENT_TIME, FUNDING_CURVE, PAYMENT_YEAR_FRACTION, NOTIONAL, RATE_YEAR_FRACTION, START_TIME, END_TIME, SPREAD, "false");
    assertFalse(other.equals(payment));
  }
}
