/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

/**
 * 
 */
public class RealPolynomialFunction1DTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final double[] C = new double[] {3.4, 5.6, 1., -4.};
  private static final Function1D<Double, Double> F = new RealPolynomialFunction1D(C);
  private static final double EPS = 1e-12;

  @Test(expected = IllegalArgumentException.class)
  public void testNullCoefficients() {
    new RealPolynomialFunction1D(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyCoefficients() {
    new RealPolynomialFunction1D(new double[0]);
  }

  @Test
  public void test() {
    final double x = RANDOM.nextDouble();
    assertEquals(C[3] * Math.pow(x, 3) + C[2] * Math.pow(x, 2) + C[1] * x + C[0], F.evaluate(x), EPS);
  }
}
