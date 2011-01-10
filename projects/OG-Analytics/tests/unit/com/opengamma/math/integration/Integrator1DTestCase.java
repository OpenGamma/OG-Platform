/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.opengamma.math.function.Function1D;

public abstract class Integrator1DTestCase {
  private static final Function1D<Double, Double> DF = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 1 + Math.exp(-x);
    }

  };
  private static final Function1D<Double, Double> F = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x - Math.exp(-x);
    }

  };
  private static final Double LOWER = 0.;
  private static final Double UPPER = 12.;
  private static final double EPS = 1e-5;

  public void testNullFunction() {
    try {
      getIntegrator().integrate(null, LOWER, UPPER);
      fail();
    } catch (final IllegalArgumentException e) {
    }
  }

  public void testNullLowerBound() {
    try {
      getIntegrator().integrate(DF, null, UPPER);
      fail();
    } catch (final IllegalArgumentException e) {
    }
  }

  public void testNullUpperBound() {
    try {
      getIntegrator().integrate(DF, LOWER, null);
      fail();
    } catch (final IllegalArgumentException e) {
    }
  }

  public void test() {
    assertEquals(getIntegrator().integrate(DF, LOWER, UPPER), F.evaluate(UPPER) - F.evaluate(LOWER), EPS);
    assertEquals(getIntegrator().integrate(DF, UPPER, LOWER), -getIntegrator().integrate(DF, LOWER, UPPER), EPS);
  }

  public abstract Integrator1D<Double, Function1D<Double, Double>, Double> getIntegrator();
}
