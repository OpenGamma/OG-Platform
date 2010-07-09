/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class BondTest {
  private static final double[] TIMES = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
  private static final double[] PAYMENTS = new double[] {.2, .2, .2, .2, .2, .2, .2, .2, .2, 1.2};
  private static final Bond BOND = new Bond(TIMES, PAYMENTS);

  @Test(expected = IllegalArgumentException.class)
  public void testNullTimes() {
    new Bond(null, PAYMENTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPayments() {
    new Bond(TIMES, null);
  }

  @Test
  public void test() {
    assertArrayEquals(BOND.getPaymentTimes(), TIMES, 0);
    assertArrayEquals(BOND.getPayments(), PAYMENTS, 0);
    Bond other = new Bond(TIMES, PAYMENTS);
    assertEquals(other, BOND);
    assertEquals(other.hashCode(), BOND.hashCode());
    other = new Bond(PAYMENTS, PAYMENTS);
    assertFalse(other.equals(BOND));
    other = new Bond(TIMES, TIMES);
    assertFalse(other.equals(BOND));
  }
}
