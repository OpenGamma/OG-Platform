/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import static org.junit.Assert.assertEquals;

import com.opengamma.math.function.Function1D;

public abstract class Integrator1DTestCase {
  private static final Function1D<Double, Double> F = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * x + 7 * x + 12;
    }

  };
  private static final Double LOWER = 0.;
  private static final Double UPPER = 12.;
  private static final double EPS = 1e-12;

  public void testNullFunction() {
    getIntegrator().integrate(null, LOWER, UPPER);
  }

  public void testNullLowerBound() {
    getIntegrator().integrate(F, null, UPPER);
  }

  public void testNullUpperBound() {
    getIntegrator().integrate(F, LOWER, null);
  }

  public void test() {
    assertEquals(getIntegrator().integrate(F, LOWER, UPPER), 1224., EPS);
    assertEquals(getIntegrator().integrate(F, UPPER, LOWER), -getIntegrator().integrate(F, LOWER, UPPER), EPS);
  }

  public abstract Integrator1D<Double, Function1D<Double, Double>, Double> getIntegrator();
}
