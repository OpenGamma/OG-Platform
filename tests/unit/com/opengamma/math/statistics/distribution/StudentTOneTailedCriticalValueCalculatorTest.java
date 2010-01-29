/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;

/**
 * @author emcleod
 * 
 */
public class StudentTOneTailedCriticalValueCalculatorTest {
  private static final double NU = 5;
  private static final Function1D<Double, Double> F = new StudentTOneTailedCriticalValueCalculator(NU);
  private static final ProbabilityDistribution<Double> T = new StudentTDistribution(NU);

  @Test(expected = IllegalArgumentException.class)
  public void testNu() {
    new StudentTOneTailedCriticalValueCalculator(-3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    F.evaluate((Double) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegative() {
    F.evaluate(-4.);
  }

  @Test
  public void test() {
    double x;
    final double eps = 1e-5;
    for (int i = 0; i < 100; i++) {
      x = Math.random();
      assertEquals(x, F.evaluate(T.getPDF(x)), eps);
    }
  }
}
