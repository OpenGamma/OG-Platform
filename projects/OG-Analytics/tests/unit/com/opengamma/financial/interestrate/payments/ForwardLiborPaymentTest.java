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
public class ForwardLiborPaymentTest {

  private static final double PAYMENT_TIME = 0.67;
  private static final double RESET_TIME = 0.25;
  private static final double MATURITY = 0.52;
  private static final double PAYMENT_YEAR_FRACTION = 0.25;
  private static final double FORWARD_YEAR_FRACTION = 0.27;
  private static final String FUNDING_CURVE_NAME = "funding";
  private static final String LIBOR_CURVE_NAME = "libor";
  private static final ForwardLiborPayment PAYMENT = new ForwardLiborPayment(PAYMENT_TIME, RESET_TIME, MATURITY, PAYMENT_YEAR_FRACTION, FORWARD_YEAR_FRACTION, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativePaymentTime() {
    new ForwardLiborPayment(-1, RESET_TIME, MATURITY, PAYMENT_YEAR_FRACTION, FORWARD_YEAR_FRACTION, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeResetTime() {
    new ForwardLiborPayment(PAYMENT_TIME, -0.1, MATURITY, PAYMENT_YEAR_FRACTION, FORWARD_YEAR_FRACTION, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMaturityBeforereset() {
    new ForwardLiborPayment(PAYMENT_TIME, RESET_TIME, RESET_TIME - 0.1, PAYMENT_YEAR_FRACTION, FORWARD_YEAR_FRACTION, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeYearFraction1() {
    new ForwardLiborPayment(PAYMENT_TIME, RESET_TIME, MATURITY, -0.25, FORWARD_YEAR_FRACTION, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeYearFraction2() {
    new ForwardLiborPayment(PAYMENT_TIME, RESET_TIME, MATURITY, PAYMENT_YEAR_FRACTION, -0.25, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongYearFraction1() {
    new ForwardLiborPayment(PAYMENT_TIME, RESET_TIME, MATURITY, 0.04, FORWARD_YEAR_FRACTION, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongYearFraction2() {
    new ForwardLiborPayment(PAYMENT_TIME, RESET_TIME, MATURITY, PAYMENT_YEAR_FRACTION, 1.5 * FORWARD_YEAR_FRACTION, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingCurve() {
    new ForwardLiborPayment(PAYMENT_TIME, RESET_TIME, MATURITY, PAYMENT_YEAR_FRACTION, FORWARD_YEAR_FRACTION, null, LIBOR_CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullLiborCurve() {
    new ForwardLiborPayment(PAYMENT_TIME, RESET_TIME, MATURITY, PAYMENT_YEAR_FRACTION, FORWARD_YEAR_FRACTION, FUNDING_CURVE_NAME, null);
  }

  @Test
  public void testHashCodeAndEquals() {
    ForwardLiborPayment other = new ForwardLiborPayment(PAYMENT_TIME, 1.0, RESET_TIME, MATURITY, PAYMENT_YEAR_FRACTION, FORWARD_YEAR_FRACTION, 0.0, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    assertEquals(other, PAYMENT);
    assertEquals(other.hashCode(), PAYMENT.hashCode());
    other = new ForwardLiborPayment(PAYMENT_TIME - 0.01, RESET_TIME, MATURITY, PAYMENT_YEAR_FRACTION, FORWARD_YEAR_FRACTION, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new ForwardLiborPayment(PAYMENT_TIME, RESET_TIME + 0.01, MATURITY, PAYMENT_YEAR_FRACTION, FORWARD_YEAR_FRACTION, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new ForwardLiborPayment(PAYMENT_TIME, RESET_TIME, MATURITY - 0.01, PAYMENT_YEAR_FRACTION, FORWARD_YEAR_FRACTION, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new ForwardLiborPayment(PAYMENT_TIME, RESET_TIME, MATURITY, PAYMENT_YEAR_FRACTION + 0.01, FORWARD_YEAR_FRACTION, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new ForwardLiborPayment(PAYMENT_TIME, RESET_TIME, MATURITY, PAYMENT_YEAR_FRACTION, FORWARD_YEAR_FRACTION + 0.01, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new ForwardLiborPayment(PAYMENT_TIME, RESET_TIME, MATURITY, PAYMENT_YEAR_FRACTION, FORWARD_YEAR_FRACTION, "dfdfg", LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new ForwardLiborPayment(PAYMENT_TIME, RESET_TIME, MATURITY, PAYMENT_YEAR_FRACTION, FORWARD_YEAR_FRACTION, FUNDING_CURVE_NAME, "dfffgdfs");
    assertFalse(other.equals(PAYMENT));
    other = new ForwardLiborPayment(PAYMENT_TIME, 1.0, RESET_TIME, MATURITY, PAYMENT_YEAR_FRACTION, FORWARD_YEAR_FRACTION, 0.05, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new ForwardLiborPayment(PAYMENT_TIME, 1.1, RESET_TIME, MATURITY, PAYMENT_YEAR_FRACTION, FORWARD_YEAR_FRACTION, 0.0, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
  }

  @Test
  public void testGetters() {
    assertEquals(PAYMENT.getPaymentTime(), PAYMENT_TIME, 0);
    assertEquals(PAYMENT.getLiborFixingTime(), RESET_TIME, 0);
    assertEquals(PAYMENT.getLiborMaturityTime(), MATURITY, 0);
    assertEquals(PAYMENT.getPaymentYearFraction(), PAYMENT_YEAR_FRACTION, 0);
    assertEquals(PAYMENT.getForwardYearFraction(), FORWARD_YEAR_FRACTION, 0);
    assertEquals(PAYMENT.getSpread(), 0, 0);
    assertEquals(PAYMENT.getNotional(), 1.0, 0);
    assertEquals(PAYMENT.getFundingCurveName(), FUNDING_CURVE_NAME);
    assertEquals(PAYMENT.getLiborCurveName(), LIBOR_CURVE_NAME);
  }

}
