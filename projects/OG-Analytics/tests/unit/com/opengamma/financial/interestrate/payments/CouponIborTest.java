/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
public class CouponIborTest {
  //CouponIbor(PAYMENT_TIME, RESET_TIME, MATURITY, PAYMENT_YEAR_FRACTION, FORWARD_YEAR_FRACTION, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
  private static final double PAYMENT_TIME = 0.67;
  private static final double RESET_TIME = 0.25;
  private static final double MATURITY = 0.52;
  private static final double PAYMENT_YEAR_FRACTION = 0.25;
  private static final double FORWARD_YEAR_FRACTION = 0.27;
  private static final double NOTIONAL = 10000.0;
  private static final String FUNDING_CURVE_NAME = "funding";
  private static final String LIBOR_CURVE_NAME = "libor";
  private static final CouponIbor PAYMENT = new CouponIbor(PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME, RESET_TIME, MATURITY, FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativePaymentTime() {
    new CouponIbor(-1, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME, RESET_TIME, MATURITY, FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeResetTime() {
    new CouponIbor(PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, -0.1, RESET_TIME, MATURITY, FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMaturityBeforereset() {
    new CouponIbor(PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME, RESET_TIME, RESET_TIME - 0.1, FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeYearFraction1() {
    new CouponIbor(PAYMENT_TIME, FUNDING_CURVE_NAME, -0.25, NOTIONAL, RESET_TIME, RESET_TIME, MATURITY, FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);
    ;
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeYearFraction2() {
    new CouponIbor(PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME, RESET_TIME, MATURITY, -0.25, LIBOR_CURVE_NAME);
    ;
  }

  //  @Test(expected = IllegalArgumentException.class)
  //  public void testWrongYearFraction1() {
  //    new CouponIbor(PAYMENT_TIME, RESET_TIME, MATURITY, 0.04, FORWARD_YEAR_FRACTION, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
  //  }
  //
  //  @Test(expected = IllegalArgumentException.class)
  //  public void testWrongYearFraction2() {
  //    new CouponIbor(PAYMENT_TIME, RESET_TIME, MATURITY, PAYMENT_YEAR_FRACTION, 1.5 * FORWARD_YEAR_FRACTION, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
  //  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingCurve() {
    new CouponIbor(PAYMENT_TIME, null, PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME, RESET_TIME, MATURITY, FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullLiborCurve() {
    new CouponIbor(PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME, RESET_TIME, MATURITY, FORWARD_YEAR_FRACTION, null);
  }

  @Test
  public void testHashCodeAndEquals() {
    CouponIbor other = new CouponIbor(PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME, RESET_TIME, MATURITY, FORWARD_YEAR_FRACTION, 0.0, LIBOR_CURVE_NAME);
    assertEquals(other, PAYMENT);
    assertEquals(other.hashCode(), PAYMENT.hashCode());
    other = new CouponIbor(PAYMENT_TIME - 0.1, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME, RESET_TIME, MATURITY, FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new CouponIbor(PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION + 0.01, NOTIONAL, RESET_TIME, RESET_TIME, MATURITY, FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new CouponIbor(PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL + 10, RESET_TIME, RESET_TIME, MATURITY, FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new CouponIbor(PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME + 0.01, RESET_TIME + 0.01, MATURITY, FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new CouponIbor(PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME, RESET_TIME + 0.01, MATURITY, FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new CouponIbor(PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME, RESET_TIME, MATURITY + 0.01, FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new CouponIbor(PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME, RESET_TIME, MATURITY, FORWARD_YEAR_FRACTION + 0.01, LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new CouponIbor(PAYMENT_TIME, "false", PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME, RESET_TIME, MATURITY, FORWARD_YEAR_FRACTION, LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new CouponIbor(PAYMENT_TIME, FUNDING_CURVE_NAME, PAYMENT_YEAR_FRACTION, NOTIONAL, RESET_TIME, RESET_TIME, MATURITY, FORWARD_YEAR_FRACTION, "false");
    assertFalse(other.equals(PAYMENT));
  }

  @Test
  public void testGetters() {
    assertEquals(PAYMENT.getPaymentTime(), PAYMENT_TIME, 0);
    assertEquals(PAYMENT.getFixingTime(), RESET_TIME, 0);
    assertEquals(PAYMENT.getFixingPeriodEndTime(), MATURITY, 0);
    assertEquals(PAYMENT.getPaymentYearFraction(), PAYMENT_YEAR_FRACTION, 0);
    assertEquals(PAYMENT.getFixingYearFraction(), FORWARD_YEAR_FRACTION, 0);
    assertEquals(PAYMENT.getSpread(), 0, 0);
    assertEquals(PAYMENT.getNotional(), NOTIONAL, 0);
    assertEquals(PAYMENT.getFundingCurveName(), FUNDING_CURVE_NAME);
    assertEquals(PAYMENT.getForwardCurveName(), LIBOR_CURVE_NAME);
  }

}
