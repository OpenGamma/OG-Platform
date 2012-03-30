/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.AnnualInterestRate;
import com.opengamma.analytics.financial.interestrate.ContinuousInterestRate;
import com.opengamma.analytics.financial.interestrate.InterestRate;
import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;

/**
 * 
 */
public class InterestRateTest {
  private static final double RATE = 0.05;
  private static final AnnualInterestRate ANNUAL = new AnnualInterestRate(RATE);
  private static final ContinuousInterestRate CONTINUOUS = new ContinuousInterestRate(RATE);
  private static final PeriodicInterestRate QUARTERLY = new PeriodicInterestRate(RATE, 4);
  private static final double EPS = 1e-15;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeCompoundingPeriodsPerYear() {
    new PeriodicInterestRate(RATE, -12);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoCompoundingPeriodsPerYear() {
    new PeriodicInterestRate(RATE, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArgument1() {
    ANNUAL.fromAnnual(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArgument2() {
    ANNUAL.fromContinuous(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArgument3() {
    ANNUAL.fromPeriodic(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArgument4() {
    CONTINUOUS.fromAnnual(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArgument5() {
    CONTINUOUS.fromContinuous(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArgument6() {
    CONTINUOUS.fromPeriodic(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArgument7() {
    QUARTERLY.fromAnnual(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArgument8() {
    QUARTERLY.fromContinuous(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArgument9() {
    QUARTERLY.fromPeriodic(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeNumberOfPeriods1() {
    ANNUAL.toPeriodic(-3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeNumberOfPeriods2() {
    CONTINUOUS.toPeriodic(-3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeNumberOfPeriods3() {
    QUARTERLY.toPeriodic(-3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroNumberOfPeriods1() {
    ANNUAL.toPeriodic(-3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroNumberOfPeriods2() {
    CONTINUOUS.toPeriodic(-3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroNumberOfPeriods3() {
    QUARTERLY.toPeriodic(-3);
  }

  @Test
  public void testEqualsAndHashCode() {
    final InterestRate annual = new AnnualInterestRate(RATE);
    final InterestRate continuous = new ContinuousInterestRate(RATE);
    final InterestRate periodic = new PeriodicInterestRate(RATE, 4);
    assertEquals(annual, ANNUAL);
    assertEquals(annual.hashCode(), ANNUAL.hashCode());
    assertEquals(continuous, CONTINUOUS);
    assertEquals(continuous.hashCode(), CONTINUOUS.hashCode());
    assertEquals(periodic, QUARTERLY);
    assertEquals(periodic.hashCode(), QUARTERLY.hashCode());
    assertFalse(annual.equals(continuous));
    assertFalse(annual.equals(periodic));
    assertFalse(continuous.equals(periodic));
    assertFalse(new AnnualInterestRate(RATE + 1).equals(ANNUAL));
    assertFalse(new ContinuousInterestRate(RATE + 1).equals(CONTINUOUS));
    assertFalse(new PeriodicInterestRate(RATE + 1, 2).equals(QUARTERLY));
    assertFalse(new PeriodicInterestRate(RATE, 3).equals(QUARTERLY));
  }

  @Test
  public void testGetters() {
    assertEquals(RATE, ANNUAL.getRate(), 0);
    assertEquals(RATE, CONTINUOUS.getRate(), 0);
    assertEquals(RATE, QUARTERLY.getRate(), 0);
  }

  @Test
  public void testGetDiscountFactor() {
    assertEquals(1. / ANNUAL.getDiscountFactor(1), 1 + RATE, EPS);
    assertEquals(1. / CONTINUOUS.getDiscountFactor(1), Math.exp(RATE), EPS);
    assertEquals(1. / QUARTERLY.getDiscountFactor(1), Math.pow(1 + RATE / 4, 4), EPS);
  }

  @Test
  public void testForwardAndBackwardConversion() {
    assertEquals(ANNUAL.toAnnual().getRate(), RATE, EPS);
    assertEquals(ANNUAL.fromAnnual(ANNUAL).getRate(), RATE, EPS);
    assertEquals(ANNUAL.fromContinuous(ANNUAL.toContinuous()).getRate(), RATE, EPS);
    assertEquals(ANNUAL.fromPeriodic(ANNUAL.toPeriodic(4)).getRate(), RATE, EPS);
    assertEquals(CONTINUOUS.toContinuous().getRate(), RATE, EPS);
    assertEquals(CONTINUOUS.fromContinuous(CONTINUOUS).getRate(), RATE, EPS);
    assertEquals(CONTINUOUS.fromAnnual(CONTINUOUS.toAnnual()).getRate(), RATE, EPS);
    assertEquals(CONTINUOUS.fromPeriodic(CONTINUOUS.toPeriodic(4)).getRate(), RATE, EPS);
    assertEquals(QUARTERLY.toPeriodic(4).getRate(), RATE, EPS);
    assertEquals(QUARTERLY.fromPeriodic(QUARTERLY).getRate(), RATE, EPS);
    assertEquals(QUARTERLY.fromAnnual(QUARTERLY.toAnnual()).getRate(), RATE, EPS);
    assertEquals(QUARTERLY.fromContinuous(QUARTERLY.toContinuous()).getRate(), RATE, EPS);
  }
}
