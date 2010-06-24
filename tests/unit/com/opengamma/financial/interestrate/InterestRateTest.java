/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class InterestRateTest {
  private static final double RATE = 0.05;
  private static final AnnualInterestRate ANNUAL = new AnnualInterestRate(RATE);
  private static final ContinuousInterestRate CONTINUOUS = new ContinuousInterestRate(RATE);
  private static final double EPS = 1e-15;

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeCompoundingPeriodsPerYear() {
    new PeriodicInterestRate(RATE, -12, 2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoCompoundingPeriodsPerYear() {
    new PeriodicInterestRate(RATE, 0, 2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeRatePeriodsPerYear() {
    new PeriodicInterestRate(RATE, 4, -4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoRatePeriodsPerYear() {
    new PeriodicInterestRate(RATE, 1, -2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullArgument1() {
    ANNUAL.fromAnnual(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullArgument2() {
    ANNUAL.fromContinuous(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullArgument3() {
    ANNUAL.fromPeriodic(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullArgument4() {
    CONTINUOUS.fromAnnual(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullArgument5() {
    CONTINUOUS.fromContinuous(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullArgument6() {
    CONTINUOUS.fromPeriodic(null);
  }

  @Test
  public void testEqualsAndHashCode() {
    final InterestRate annual = new AnnualInterestRate(RATE);
    final InterestRate continuous = new ContinuousInterestRate(RATE);
    assertEquals(annual, ANNUAL);
    assertEquals(annual.hashCode(), ANNUAL.hashCode());
    assertEquals(continuous, CONTINUOUS);
    assertEquals(continuous.hashCode(), CONTINUOUS.hashCode());
    assertFalse(annual.equals(continuous));
    assertFalse(new AnnualInterestRate(RATE + 1).equals(ANNUAL));
    assertFalse(new ContinuousInterestRate(RATE + 1).equals(CONTINUOUS));
  }

  @Test
  public void testGetters() {
    assertEquals(RATE, ANNUAL.getRate(), 0);
    assertEquals(RATE, CONTINUOUS.getRate(), 0);
  }

  @Test
  public void testGetDiscountFactor() {
    assertEquals(1. / ANNUAL.getDiscountFactor(1), 1 + RATE, EPS);
    assertEquals(1. / CONTINUOUS.getDiscountFactor(1), Math.exp(RATE), EPS);
  }

  @Test
  public void testForwardAndBackwardConversion() {
    assertEquals(ANNUAL.toAnnual().getRate(), ANNUAL.getRate(), EPS);
    assertEquals(ANNUAL.fromAnnual(ANNUAL).getRate(), ANNUAL.getRate(), EPS);
    assertEquals(ANNUAL.fromContinuous(ANNUAL.toContinuous()).getRate(), ANNUAL.getRate(), EPS);
    assertEquals(CONTINUOUS.toContinuous().getRate(), CONTINUOUS.getRate(), EPS);
    assertEquals(CONTINUOUS.fromContinuous(CONTINUOUS).getRate(), CONTINUOUS.getRate(), EPS);
    assertEquals(CONTINUOUS.fromAnnual(CONTINUOUS.toAnnual()).getRate(), CONTINUOUS.getRate(), EPS);
  }
}
