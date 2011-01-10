/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
  private static final double T = 3;
  private static final double R = 0.04;
  private static final double TRADE_T = 0;
  private static final double FRACTION = 3;
  private static final String CURVE_NAME = "test";

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeTime1() {
    new Cash(-T, R, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeTime2() {
    new Cash(-T, R, TRADE_T, FRACTION, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeRate1() {
    new Cash(T, -R, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeRate2() {
    new Cash(T, -R, TRADE_T, FRACTION, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullName1() {
    new Cash(T, R, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullName2() {
    new Cash(T, R, TRADE_T, FRACTION, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadTradeTime() {
    new Cash(T, R, T + 1, FRACTION, null);
  }

  @Test
  public void test() {
    final double time = 1 / 52.;
    final double rate = 0.05;
    final Cash cash = new Cash(time, rate, CURVE_NAME);
    assertEquals(cash.getMaturity(), time, 0);
    assertEquals(cash.getYearFraction(), time, 0);
    assertEquals(cash.getRate(), rate, 0);
    assertEquals(cash.getTradeTime(), 0, 0);
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
    other = new Cash(time, rate, 0, time, CURVE_NAME);
    assertEquals(other, cash);
  }

}
