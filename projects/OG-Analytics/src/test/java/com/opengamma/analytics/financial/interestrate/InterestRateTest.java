/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class InterestRateTest {
  private static final double RATE = 0.05;
  private static final InterestRate ANNUAL = new PeriodicInterestRate(RATE, 1);
  private static final ContinuousInterestRate CONTINUOUS = new ContinuousInterestRate(RATE);
  private static final PeriodicInterestRate QUARTERLY = new PeriodicInterestRate(RATE, 4);
  private static final InterestRateSimpleMoneyMarketBasis SIMPLE_MM = new InterestRateSimpleMoneyMarketBasis(RATE);
  private static final InterestRateSimpleDiscountBasis SIMPLE_DSC = new InterestRateSimpleDiscountBasis(RATE);
  private static final double EPS = 1e-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeCompoundingPeriodsPerYear() {
    new PeriodicInterestRate(RATE, -12);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoCompoundingPeriodsPerYear() {
    new PeriodicInterestRate(RATE, 0);
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
  public void testNullArgument5() {
    CONTINUOUS.fromContinuous(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArgument6() {
    CONTINUOUS.fromPeriodic(null);
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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void simpleDiscountingNegativeDf() {
    SIMPLE_DSC.getDiscountFactor(10000);
  }

  @Test
  public void testEqualsAndHashCode() {
    final InterestRate annual = new PeriodicInterestRate(RATE, 1);
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
    assertFalse(new PeriodicInterestRate(RATE + 1, 1).equals(ANNUAL));
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
    double[] time = {0.01, 1.0, 2.5, 10.0};
    for (int looptime = 0; looptime < time.length; looptime++) {
      assertEquals(1. / ANNUAL.getDiscountFactor(time[looptime]), Math.pow(1 + RATE, time[looptime]), EPS);
      assertEquals(1. / CONTINUOUS.getDiscountFactor(time[looptime]), Math.exp(RATE * time[looptime]), EPS);
      assertEquals(1. / QUARTERLY.getDiscountFactor(time[looptime]), Math.pow(1 + RATE / 4, 4 * time[looptime]), EPS);
      assertEquals("InterestRate: simple money market basis - discount factor", SIMPLE_MM.getDiscountFactor(time[looptime]), 1.0 / (1.0 + RATE * time[looptime]), EPS);
      assertEquals("InterestRate: simple discounting basis - discount factor", SIMPLE_DSC.getDiscountFactor(time[looptime]), 1.0 - RATE * time[looptime], EPS);
    }
  }

  @Test
  public void testForwardAndBackwardConversion() {
    assertEquals(ANNUAL.fromContinuous(ANNUAL.toContinuous()).getRate(), RATE, EPS);
    assertEquals(ANNUAL.fromPeriodic(ANNUAL.toPeriodic(4)).getRate(), RATE, EPS);
    assertEquals(CONTINUOUS.toContinuous().getRate(), RATE, EPS);
    assertEquals(CONTINUOUS.fromContinuous(CONTINUOUS).getRate(), RATE, EPS);
    assertEquals(CONTINUOUS.fromPeriodic(CONTINUOUS.toPeriodic(4)).getRate(), RATE, EPS);
    assertEquals(QUARTERLY.toPeriodic(4).getRate(), RATE, EPS);
    assertEquals(QUARTERLY.fromPeriodic(QUARTERLY).getRate(), RATE, EPS);
    assertEquals(QUARTERLY.fromContinuous(QUARTERLY.toContinuous()).getRate(), RATE, EPS);
  }
}
