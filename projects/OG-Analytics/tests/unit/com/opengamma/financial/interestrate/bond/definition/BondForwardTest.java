/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class BondForwardTest {
  private static final double COUPON = 0.01;
  private static final double[] PAYMENT_TIMES = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
  private static final String YIELD_CURVE_NAME = "A";
  private static final Bond BOND = new Bond(PAYMENT_TIMES, COUPON, YIELD_CURVE_NAME);
  private static final double FORWARD_DATE = 0.12;
  private static final BondForward FORWARD = new BondForward(BOND, FORWARD_DATE);

  @Test(expected = IllegalArgumentException.class)
  public void testNullBond() {
    new BondForward(null, FORWARD_DATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeForwardDate() {
    new BondForward(BOND, -FORWARD_DATE);
  }

  @Test
  public void test() {
    assertEquals(BOND, FORWARD.getBond());
    assertEquals(FORWARD_DATE, FORWARD.getForwardTime(), 0);
    BondForward other = new BondForward(BOND, FORWARD_DATE);
    assertEquals(other, FORWARD);
    assertEquals(other.hashCode(), FORWARD.hashCode());
    other = new BondForward(new Bond(new double[] {1, 2, 3, 4}, COUPON, YIELD_CURVE_NAME), FORWARD_DATE);
    assertFalse(other.equals(FORWARD));
    other = new BondForward(BOND, FORWARD_DATE + 1);
    assertFalse(other.equals(FORWARD));
  }
}
