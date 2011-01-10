/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class StudentTTwoTailedCriticalValueCalculatorTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final double NU = 3;
  private static final Function1D<Double, Double> F = new StudentTTwoTailedCriticalValueCalculator(NU);
  private static final ProbabilityDistribution<Double> T = new StudentTDistribution(NU);

  @Test(expected = IllegalArgumentException.class)
  public void testNu1() {
    new StudentTTwoTailedCriticalValueCalculator(-3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNu2() {
    new StudentTTwoTailedCriticalValueCalculator(-3, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullEngine() {
    new StudentTDistribution(3, null);
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
    double x, y;
    final double eps = 1e-5;
    for (int i = 0; i < 100; i++) {
      x = RANDOM.nextDouble();
      y = 0.5 * (1 + x);
      assertEquals(y, T.getCDF(F.evaluate(x)), eps);
    }
  }
}
