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
    new Cash(-3, CURVE_NAME);
  }

  @Test
  public void test() {
    final double time = 3;
    final Cash cash = new Cash(time, CURVE_NAME);
    assertEquals(cash.getPaymentTime(), time, 0);
    Cash other = new Cash(time, CURVE_NAME);
    assertEquals(other, cash);
    assertEquals(other.hashCode(), cash.hashCode());
    other = new Cash(time + 1, CURVE_NAME);
    assertFalse(other.equals(cash));
  }

}
