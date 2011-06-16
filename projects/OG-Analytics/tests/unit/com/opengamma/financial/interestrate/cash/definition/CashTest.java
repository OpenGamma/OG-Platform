/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.cash.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CashTest {
  private static final Currency CCY = Currency.AUD;
  private static final double T = 3;
  private static final double NOTIONAL = 10000;
  private static final double R = 0.04;
  private static final double TRADE_T = 0;
  private static final double FRACTION = 3;
  private static final String CURVE_NAME = "test";

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency1() {
    new Cash(null, T, NOTIONAL, R, CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency2() {
    new Cash(null, T, NOTIONAL, R, TRADE_T, FRACTION, CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeTime1() {
    new Cash(CCY, -T, NOTIONAL, R, CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeTime2() {
    new Cash(CCY, -T, NOTIONAL, R, TRADE_T, FRACTION, CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeRate1() {
    new Cash(CCY, T, NOTIONAL, -R, CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeRate2() {
    new Cash(CCY, T, NOTIONAL, -R, TRADE_T, FRACTION, CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName1() {
    new Cash(CCY, T, NOTIONAL, R, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName2() {
    new Cash(CCY, T, NOTIONAL, R, TRADE_T, FRACTION, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadTradeTime() {
    new Cash(CCY, T, NOTIONAL, R, T + 1, FRACTION, null);
  }

  @Test
  public void test() {
    final double time = 1 / 52.;
    final double rate = 0.05;
    final Cash cash = new Cash(CCY, time, NOTIONAL, rate, CURVE_NAME);
    assertEquals(cash.getCurrency(), CCY);
    assertEquals(cash.getMaturity(), time, 0);
    assertEquals(cash.getYearFraction(), time, 0);
    assertEquals(cash.getRate(), rate, 0);
    assertEquals(cash.getTradeTime(), 0, 0);
    assertEquals(cash.getNotional(), NOTIONAL);
    assertEquals(cash.getYieldCurveName(), CURVE_NAME);
    Cash other = new Cash(CCY, time, NOTIONAL, rate, CURVE_NAME);
    assertEquals(other, cash);
    assertEquals(other.hashCode(), cash.hashCode());
    other = new Cash(CCY, time, NOTIONAL, rate, 0.0, time, CURVE_NAME);
    assertEquals(other, cash);
    other = new Cash(Currency.CAD, time, NOTIONAL, rate, CURVE_NAME);
    assertFalse(other.equals(cash));
    other = new Cash(CCY, time, NOTIONAL, rate, 0.0, time, CURVE_NAME);
    assertEquals(other, cash);
    other = new Cash(CCY, time + 1, NOTIONAL, rate, CURVE_NAME);
    assertFalse(other.equals(cash));
    other = new Cash(CCY, time, NOTIONAL, 0.04, CURVE_NAME);
    assertFalse(other.equals(cash));
    other = new Cash(CCY, time, NOTIONAL, rate, "");
    assertFalse(other.equals(cash));
    other = new Cash(CCY, time, NOTIONAL, rate, 0.0, 7 / 365., CURVE_NAME);
    assertFalse(other.equals(cash));
    other = new Cash(CCY, time, NOTIONAL, rate, 1 / 365., time, CURVE_NAME);
    assertFalse(other.equals(cash));
    other = new Cash(CCY, time, NOTIONAL + 1000, rate, 1. / 365, time, CURVE_NAME);
    assertFalse(other.equals(cash));
    other = new Cash(CCY, time, NOTIONAL, rate, 0, time, CURVE_NAME);
    assertEquals(other, cash);
  }

}
