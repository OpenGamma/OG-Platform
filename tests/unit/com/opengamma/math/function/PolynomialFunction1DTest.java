/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

/**
 * 
 * @author emcleod
 */
public class PolynomialFunction1DTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final Double[] C = new Double[] { 3.4, 5.6, 1., -4. };
  private static final Function1D<Double, Double> F = new PolynomialFunction1D(C);
  private static final double EPS = 1e-12;

  @Test
  public void testInputs() {
    try {
      new PolynomialFunction1D(null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new PolynomialFunction1D(new Double[0]);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new PolynomialFunction1D(new Double[] { 1., null });
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void test() {
    final double x = RANDOM.nextDouble();
    assertEquals(C[3] * Math.pow(x, 3) + C[2] * Math.pow(x, 2) + C[1] * x + C[0], F.evaluate(x), EPS);
  }
}
