/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.definition;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;

/**
 * 
 */
public class BondFutureTest {
  private static final String NAME = "A";
  private static final BondForward[] DELIVERABLES = new BondForward[] {new BondForward(new Bond(new double[] {1, 2, 3}, 0.05, NAME), 0.5, 0, 0, new FixedCouponPayment[0]),
      new BondForward(new Bond(new double[] {1, 2, 3, 4, 5, 6}, 0.06, NAME), 0.5, 0, 0, new FixedCouponPayment[0]),
      new BondForward(new Bond(new double[] {1, 2, 3, 4, 5}, 0.045, NAME), 0.5, 0, 0, new FixedCouponPayment[0])};
  private static final double[] CONVERSION_FACTORS = new double[] {1.23, 3.45, 5.67};
  private static final double PRICE = 130;

  @Test(expected = IllegalArgumentException.class)
  public void testNullBonds() {
    new BondFuture(null, CONVERSION_FACTORS, PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullConversionFactors() {
    new BondFuture(DELIVERABLES, null, PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullBond() {
    new BondFuture(new BondForward[] {null}, CONVERSION_FACTORS, PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyBonds() {
    new BondFuture(new BondForward[] {}, new double[] {}, PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongLengthArrays() {
    new BondFuture(DELIVERABLES, new double[] {1, 2, 3, 4, 5}, PRICE);
  }

  @Test
  public void test() {
    final BondFuture future = new BondFuture(DELIVERABLES, CONVERSION_FACTORS, PRICE);
    BondFuture other = new BondFuture(DELIVERABLES, CONVERSION_FACTORS, PRICE);
    assertEquals(future, other);
    assertEquals(future.hashCode(), other.hashCode());
    assertArrayEquals(future.getBondForwards(), DELIVERABLES);
    assertArrayEquals(future.getConversionFactors(), CONVERSION_FACTORS, 0);
    assertEquals(future.getPrice(), PRICE, 0);
    other = new BondFuture(DELIVERABLES, CONVERSION_FACTORS, PRICE);
    assertEquals(future, other);
    other = new BondFuture(new BondForward[] {new BondForward(new Bond(new double[] {1, 2, 3}, 0.054, NAME), 0.5, 0, 0, new FixedCouponPayment[0]), DELIVERABLES[1], DELIVERABLES[2]},
        CONVERSION_FACTORS, PRICE);
    assertFalse(future.equals(other));
    other = new BondFuture(DELIVERABLES, new double[] {1, 2, 3}, PRICE);
    assertFalse(future.equals(other));
    other = new BondFuture(DELIVERABLES, CONVERSION_FACTORS, PRICE + 1);
    assertFalse(future.equals(other));
  }
}
