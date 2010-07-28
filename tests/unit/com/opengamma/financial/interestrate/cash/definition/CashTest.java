/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.cash.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class CashTest {
  public static final String CURVE_NAME = "test";

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeTime() {
    new Cash(-3, 0.05, CURVE_NAME);
  }

  @Test
  public void test() {
    final double time = 1 / 52.;
    final double rate = 0.05;
    final Cash cash = new Cash(time, rate, CURVE_NAME);
    assertEquals(cash.getPaymentTime(), time, 0);
    assertEquals(cash.getYearFraction(), time, 0);
    assertEquals(cash.getRate(), rate, 0);
    assertEquals(cash.getYieldCurveName(), CURVE_NAME);
    Cash other = new Cash(time, rate, CURVE_NAME);
    assertEquals(other, cash);
    assertEquals(other.hashCode(), cash.hashCode());
    other = new Cash(time, rate, 0.0, time, CURVE_NAME);
    assertEquals(other, cash);
    other = new Cash(time + 1, rate, CURVE_NAME);
    assertFalse(other.equals(cash));
    other = new Cash(time, 0.04, CURVE_NAME);
    assertFalse(other.equals(cash));
    other = new Cash(time, rate, "");
    assertFalse(other.equals(cash));
    other = new Cash(time, rate, 0.0, 7 / 365., CURVE_NAME);
    assertFalse(other.equals(cash));
    other = new Cash(time, rate, 1 / 365., time, CURVE_NAME);
    assertFalse(other.equals(cash));
  }

}
