/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.NewtonCotesIntegrator1D.RuleType;

/**
 * 
 * @author emcleod
 */
public class NewtonCotesIntegrator1DTest {
  private static final Function1D<Double, Double> CONCAVE = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * x * x * x;
    }

  };
  private static final Function1D<Double, Double> DF = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * x * x + Math.sin(x);
    }

  };
  private static final Function1D<Double, Double> F = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * x * x * x / 4 - Math.cos(x);
    }

  };

  @Test
  public void testInputs() {
    try {
      new NewtonCotesIntegrator1D(null, 1);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new NewtonCotesIntegrator1D(RuleType.BOOLES, -4);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final Integrator1D<Double, Function1D<Double, Double>, Double> integrator = new NewtonCotesIntegrator1D(RuleType.RIGHT_HAND);
    try {
      integrator.integrate(null, 0., 1.);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      integrator.integrate(F, null, 0.);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      integrator.integrate(F, 0., null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testConcaveFunction() {
    final int n = 5;
    final double lower = 0;
    final double upper = 10;
    final double result = 20000;
    Integrator1D<Double, Function1D<Double, Double>, Double> integrator = new NewtonCotesIntegrator1D(RuleType.RIGHT_HAND, n);
    assertTrue(integrator.integrate(CONCAVE, lower, upper) > result);
    integrator = new NewtonCotesIntegrator1D(RuleType.LEFT_HAND, n);
    assertTrue(integrator.integrate(CONCAVE, lower, upper) < result);
    integrator = new NewtonCotesIntegrator1D(RuleType.MID_POINT, n);
    assertTrue(integrator.integrate(CONCAVE, lower, upper) < result);
  }

  @Test
  public void test() {
    final double eps = 1e-3;
    final int n = 100000;
    final double lower = 0;
    final double upper = Math.PI / 4.;
    final double result = F.evaluate(upper) - F.evaluate(lower);
    Integrator1D<Double, Function1D<Double, Double>, Double> integrator = new NewtonCotesIntegrator1D(RuleType.RIGHT_HAND, n);
    assertEquals(result, integrator.integrate(DF, lower, upper), eps);
    integrator = new NewtonCotesIntegrator1D(RuleType.LEFT_HAND, n);
    assertEquals(result, integrator.integrate(DF, lower, upper), eps);
    integrator = new NewtonCotesIntegrator1D(RuleType.MID_POINT, n);
    assertEquals(result, integrator.integrate(DF, lower, upper), eps);
    integrator = new NewtonCotesIntegrator1D(RuleType.TRAPEZOIDAL, n);
    assertEquals(result, integrator.integrate(DF, lower, upper), eps);
    integrator = new NewtonCotesIntegrator1D(RuleType.SIMPSONS, n);
    assertEquals(result, integrator.integrate(DF, lower, upper), eps);
    integrator = new NewtonCotesIntegrator1D(RuleType.BOOLES, n);
    assertEquals(result, integrator.integrate(DF, lower, upper), eps);
  }
}
