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

import com.opengamma.financial.interestrate.annuity.definition.FixedAnnuity;

/**
 * 
 */
public class BondTest {
  private static final String BOND_CURVE = "A curve";
  private static final double COUPON = 0.2;
  private static final double[] TIMES = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
  private static final double[] YEAR_FRACTIONS = new double[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
  private static final double[] PAYMENTS = new double[] {.2, .2, .2, .2, .2, .2, .2, .2, .2, 1.2};
  private static final Bond BOND = new Bond(TIMES, PAYMENTS, BOND_CURVE);

  @Test(expected = IllegalArgumentException.class)
  public void testNullTimes1() {
    new Bond(null, COUPON, BOND_CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTimes2() {
    new Bond(null, PAYMENTS, BOND_CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPayments2() {
    new Bond(TIMES, null, BOND_CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyTimes2() {
    new Bond(new double[0], PAYMENTS, BOND_CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPayments2() {
    new Bond(TIMES, new double[0], BOND_CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTimes3() {
    new Bond(null, PAYMENTS, YEAR_FRACTIONS, BOND_CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPayments3() {
    new Bond(TIMES, null, YEAR_FRACTIONS, BOND_CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyTimes3() {
    new Bond(new double[0], PAYMENTS, YEAR_FRACTIONS, BOND_CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPayments3() {
    new Bond(TIMES, new double[0], YEAR_FRACTIONS, BOND_CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullName1() {
    new Bond(TIMES, COUPON, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullName2() {
    new Bond(TIMES, PAYMENTS, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullName3() {
    new Bond(TIMES, PAYMENTS, YEAR_FRACTIONS, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongCoupons1() {
    new Bond(TIMES, new double[] {1, 2, 3, 4, 5}, BOND_CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongCoupons2() {
    new Bond(TIMES, new double[] {1, 2, 3, 4, 5}, YEAR_FRACTIONS, BOND_CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullYearFractions() {
    new Bond(TIMES, PAYMENTS, null, BOND_CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyYearFractions() {
    new Bond(TIMES, PAYMENTS, new double[0], BOND_CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongYearFractions() {
    new Bond(TIMES, PAYMENTS, new double[] {1, 1, 1, 1}, BOND_CURVE);
  }

  @Test
  public void test() {
    assertArrayEquals(BOND.getPaymentTimes(), TIMES, 0);
    assertArrayEquals(BOND.getPayments(), PAYMENTS, 0);
    assertEquals(BOND.getCurveName(), BOND_CURVE);
    assertEquals(BOND.getFixedAnnuity(), new FixedAnnuity(TIMES, 1, PAYMENTS, BOND_CURVE));
    Bond other = new Bond(TIMES, PAYMENTS, BOND_CURVE);
    assertEquals(other, BOND);
    assertEquals(other.hashCode(), BOND.hashCode());
    other = new Bond(PAYMENTS, PAYMENTS, BOND_CURVE);
    assertFalse(other.equals(BOND));
    other = new Bond(TIMES, TIMES, BOND_CURVE);
    assertFalse(other.equals(BOND));
    other = new Bond(PAYMENTS, TIMES, "A different curve");
    assertFalse(other.equals(BOND));
    other = new Bond(TIMES, PAYMENTS, YEAR_FRACTIONS, BOND_CURVE);
    assertEquals(other, BOND);
  }
}
