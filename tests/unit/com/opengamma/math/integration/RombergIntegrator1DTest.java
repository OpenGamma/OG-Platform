/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;

public class RombergIntegrator1DTest {
  private static final Integrator1D<Double, Function1D<Double, Double>, Double> INTEGRATOR = new RombergIntegrator1D();
  private static final Function1D<Double, Double> F = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * x + 7 * x + 12;
    }

  };
  private static final Double LOWER = 0.;
  private static final Double UPPER = 12.;
  private static final double EPS = 1e-12;

  @Test(expected = IllegalArgumentException.class)
  public void testNullFunction() {
    INTEGRATOR.integrate(null, LOWER, UPPER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullLowerBound() {
    INTEGRATOR.integrate(F, null, UPPER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullUpperBound() {
    INTEGRATOR.integrate(F, LOWER, null);
  }

  @Test
  public void test() {
    assertEquals(INTEGRATOR.integrate(F, LOWER, UPPER), 1224., EPS);
    assertEquals(INTEGRATOR.integrate(F, UPPER, LOWER), -INTEGRATOR.integrate(F, LOWER, UPPER), EPS);
  }
}
