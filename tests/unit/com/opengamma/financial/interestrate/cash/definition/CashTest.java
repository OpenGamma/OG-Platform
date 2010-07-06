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

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeTime() {
    new Cash(-3);
  }

  @Test
  public void test() {
    final double time = 3;
    final Cash cash = new Cash(time);
    assertEquals(cash.getPaymentTime(), time, 0);
    Cash other = new Cash(time);
    assertEquals(other, cash);
    assertEquals(other.hashCode(), cash.hashCode());
    other = new Cash(time + 1);
    assertFalse(other.equals(cash));
  }

}
