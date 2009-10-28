/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class ExtendedTrapezoidalIntegrator1DTest {
  private static final Function1D<Double, Double> DF = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return Math.exp(-x) + 1;
    }

  };
  private static final Function1D<Double, Double> F = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x - Math.exp(-x);
    }

  };

  @Test
  public void testInputs() {
    try {
      new ExtendedTrapezoidalIntegrator1D(-1);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final Integrator1D<Double, Function1D<Double, Double>, Double> integrator = new ExtendedTrapezoidalIntegrator1D();
    try {
      integrator.integrate(null, 0., 2.);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      integrator.integrate(DF, null, 10.);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      integrator.integrate(DF, 0., null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void test() {
    final double lower = -0.35;
    final double upper = 1.13;
    final double eps = 1e-6;
    final Integrator1D<Double, Function1D<Double, Double>, Double> integrator = new ExtendedTrapezoidalIntegrator1D(25);
    assertEquals(F.evaluate(upper) - F.evaluate(lower), integrator.integrate(DF, lower, upper), eps);
  }
}
