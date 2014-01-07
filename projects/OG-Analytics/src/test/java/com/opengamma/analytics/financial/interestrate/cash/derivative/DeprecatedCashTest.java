/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cash.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests related to the construction of Cash instruments.
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class DeprecatedCashTest {

  private static final Currency CCY = Currency.AUD;
  private static final double T = 3;
  private static final double NOTIONAL = 10000;
  private static final double R = 0.04;
  private static final double TRADE_T = 0;
  private static final double FRACTION = 3;
  private static final String CURVE_NAME = "test";

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency2() {
    new Cash(null, TRADE_T, T, NOTIONAL, R, FRACTION, CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeTime2() {
    new Cash(CCY, TRADE_T, -T, NOTIONAL, R, FRACTION, CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName2() {
    new Cash(CCY, TRADE_T, T, NOTIONAL, R, FRACTION, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadTradeTime() {
    new Cash(CCY, T + 1, T, NOTIONAL, R, FRACTION, null);
  }

  @Test
  /**
   * Tests the equal and hash code methods.
   */
  public void equalHash() {
    final double startTime = 0.01;
    final double endTime = 1.02;
    final double af = 1.00;
    final Cash c1 = new Cash(CCY, startTime, endTime, NOTIONAL, R, af, CURVE_NAME);
    assertEquals("Cash - equal/hash code", c1, c1);
    final Cash c2 = new Cash(CCY, startTime, endTime, NOTIONAL, R, af, CURVE_NAME);
    assertEquals("Cash - equal/hash code", c1, c2);
    assertEquals("Cash - equal/hash code", c1.hashCode(), c2.hashCode());
    Cash other;
    other = new Cash(Currency.EUR, startTime, endTime, NOTIONAL, R, af, CURVE_NAME);
    assertFalse("Cash - equal/hash code", c1.equals(other));
    other = new Cash(CCY, startTime + 0.01, endTime, NOTIONAL, R, af, CURVE_NAME);
    assertFalse("Cash - equal/hash code", c1.equals(other));
    other = new Cash(CCY, startTime, endTime + 0.01, NOTIONAL, R, af, CURVE_NAME);
    assertFalse("Cash - equal/hash code", c1.equals(other));
    other = new Cash(CCY, startTime, endTime, NOTIONAL + 10.0, R, af, CURVE_NAME);
    assertFalse("Cash - equal/hash code", c1.equals(other));
    other = new Cash(CCY, startTime, endTime, NOTIONAL, R + 0.0001, af, CURVE_NAME);
    assertFalse("Cash - equal/hash code", c1.equals(other));
    other = new Cash(CCY, startTime, endTime, NOTIONAL, R, af + 0.0001, CURVE_NAME);
    assertFalse("Cash - equal/hash code", c1.equals(other));
    other = new Cash(CCY, startTime, endTime, NOTIONAL, R, af, "No");
    assertFalse("Cash - equal/hash code", c1.equals(other));
  }

}
