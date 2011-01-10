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

  @Test(expected = IllegalArgumentException.class)
  public void testNegativePaymentTime() {
    new ContinuouslyMonitoredAverageRatePayment(-PAYMENT_TIME, NOTIONAL, START_TIME, END_TIME, PAYMENT_YEAR_FRACTION, RATE_YEAR_FRACTION, SPREAD, FUNDING_CURVE, FORWARD_CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeStartTime() {
    new ContinuouslyMonitoredAverageRatePayment(PAYMENT_TIME, NOTIONAL, -START_TIME, END_TIME, PAYMENT_YEAR_FRACTION, RATE_YEAR_FRACTION, SPREAD, FUNDING_CURVE, FORWARD_CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEndBeforeStart() {
    new ContinuouslyMonitoredAverageRatePayment(PAYMENT_TIME, NOTIONAL, END_TIME, START_TIME, PAYMENT_YEAR_FRACTION, RATE_YEAR_FRACTION, SPREAD, FUNDING_CURVE, FORWARD_CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEndTimeBeforePaymentTime() {
    new ContinuouslyMonitoredAverageRatePayment(END_TIME, NOTIONAL, START_TIME, PAYMENT_TIME, PAYMENT_YEAR_FRACTION, RATE_YEAR_FRACTION, SPREAD, FUNDING_CURVE, FORWARD_CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingCurveName() {
    new ContinuouslyMonitoredAverageRatePayment(PAYMENT_TIME, NOTIONAL, START_TIME, END_TIME, PAYMENT_YEAR_FRACTION, RATE_YEAR_FRACTION, SPREAD, null, FORWARD_CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullForwardCurveName() {
    new ContinuouslyMonitoredAverageRatePayment(PAYMENT_TIME, NOTIONAL, START_TIME, END_TIME, PAYMENT_YEAR_FRACTION, RATE_YEAR_FRACTION, SPREAD, FUNDING_CURVE, null);
  }

  @Test
  public void testGetters() {
    final ContinuouslyMonitoredAverageRatePayment payment = new ContinuouslyMonitoredAverageRatePayment(PAYMENT_TIME, NOTIONAL, START_TIME, END_TIME, PAYMENT_YEAR_FRACTION, RATE_YEAR_FRACTION,
        SPREAD, FUNDING_CURVE, FORWARD_CURVE);
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
    final ContinuouslyMonitoredAverageRatePayment payment = new ContinuouslyMonitoredAverageRatePayment(PAYMENT_TIME, NOTIONAL, START_TIME, END_TIME, PAYMENT_YEAR_FRACTION, RATE_YEAR_FRACTION,
        SPREAD, FUNDING_CURVE, FORWARD_CURVE);
    ContinuouslyMonitoredAverageRatePayment other = new ContinuouslyMonitoredAverageRatePayment(PAYMENT_TIME, NOTIONAL, START_TIME, END_TIME, PAYMENT_YEAR_FRACTION, RATE_YEAR_FRACTION, SPREAD,
        FUNDING_CURVE, FORWARD_CURVE);
    assertEquals(payment, other);
    assertEquals(payment.hashCode(), other.hashCode());
    other = new ContinuouslyMonitoredAverageRatePayment(PAYMENT_TIME + 0.001, NOTIONAL, START_TIME, END_TIME, PAYMENT_YEAR_FRACTION, RATE_YEAR_FRACTION, SPREAD, FUNDING_CURVE, FORWARD_CURVE);
    assertFalse(other.equals(payment));
    other = new ContinuouslyMonitoredAverageRatePayment(PAYMENT_TIME, NOTIONAL + 1, START_TIME, END_TIME, PAYMENT_YEAR_FRACTION, RATE_YEAR_FRACTION, SPREAD, FUNDING_CURVE, FORWARD_CURVE);
    assertFalse(other.equals(payment));
    other = new ContinuouslyMonitoredAverageRatePayment(PAYMENT_TIME, NOTIONAL, START_TIME + 0.001, END_TIME, PAYMENT_YEAR_FRACTION, RATE_YEAR_FRACTION, SPREAD, FUNDING_CURVE, FORWARD_CURVE);
    assertFalse(other.equals(payment));
    other = new ContinuouslyMonitoredAverageRatePayment(PAYMENT_TIME, NOTIONAL, START_TIME, END_TIME + 0.001, PAYMENT_YEAR_FRACTION, RATE_YEAR_FRACTION, SPREAD, FUNDING_CURVE, FORWARD_CURVE);
    assertFalse(other.equals(payment));
    other = new ContinuouslyMonitoredAverageRatePayment(PAYMENT_TIME, NOTIONAL, START_TIME, END_TIME, PAYMENT_YEAR_FRACTION + 0.001, RATE_YEAR_FRACTION, SPREAD, FUNDING_CURVE, FORWARD_CURVE);
    assertFalse(other.equals(payment));
    other = new ContinuouslyMonitoredAverageRatePayment(PAYMENT_TIME, NOTIONAL, START_TIME, END_TIME, PAYMENT_YEAR_FRACTION, RATE_YEAR_FRACTION + 0.001, SPREAD, FUNDING_CURVE, FORWARD_CURVE);
    assertFalse(other.equals(payment));
    other = new ContinuouslyMonitoredAverageRatePayment(PAYMENT_TIME, NOTIONAL, START_TIME, END_TIME, PAYMENT_YEAR_FRACTION, RATE_YEAR_FRACTION, SPREAD + 0.2, FUNDING_CURVE, FORWARD_CURVE);
    assertFalse(other.equals(payment));
    other = new ContinuouslyMonitoredAverageRatePayment(PAYMENT_TIME, NOTIONAL, START_TIME, END_TIME, PAYMENT_YEAR_FRACTION, RATE_YEAR_FRACTION, SPREAD, FORWARD_CURVE, FORWARD_CURVE);
    assertFalse(other.equals(payment));
    other = new ContinuouslyMonitoredAverageRatePayment(PAYMENT_TIME, NOTIONAL, START_TIME, END_TIME, PAYMENT_YEAR_FRACTION, RATE_YEAR_FRACTION, SPREAD, FUNDING_CURVE, FUNDING_CURVE);
    assertFalse(other.equals(payment));
  }
}
