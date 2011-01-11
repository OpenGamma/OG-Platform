/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.NewtonCotesIntegrator1D.RuleType;

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
  private static final Integrator1D<Double, Function1D<Double, Double>, Double> INTEGRATOR = new NewtonCotesIntegrator1D(RuleType.MID_POINT);
  private static final Double LOWER = 0.;
  private static final Double UPPER = 10.;
  private static final double EPS = 1e-3;

  @Test(expected = IllegalArgumentException.class)
  public void testNullRuleType1() {
    new NewtonCotesIntegrator1D(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullRuleType2() {
    new NewtonCotesIntegrator1D(null, 20);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeDivisions() {
    new NewtonCotesIntegrator1D(RuleType.MID_POINT, -10);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFunction() {
    INTEGRATOR.integrate(null, LOWER, UPPER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullLower() {
    INTEGRATOR.integrate(F, null, UPPER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullUpper() {
    INTEGRATOR.integrate(F, LOWER, null);
  }

  @Test
  public void testLimits() {
    assertEquals(INTEGRATOR.integrate(CONCAVE, LOWER, UPPER), -INTEGRATOR.integrate(CONCAVE, UPPER, LOWER), EPS);
  }

  @Test
  public void testConcaveFunction() {
    final int n = 5;
    final double result = 20000;
    Integrator1D<Double, Function1D<Double, Double>, Double> integrator = new NewtonCotesIntegrator1D(RuleType.RIGHT_HAND, n);
    assertTrue(integrator.integrate(CONCAVE, LOWER, UPPER) > result);
    integrator = new NewtonCotesIntegrator1D(RuleType.LEFT_HAND, n);
    assertTrue(integrator.integrate(CONCAVE, LOWER, UPPER) < result);
    integrator = new NewtonCotesIntegrator1D(RuleType.MID_POINT, n);
    assertTrue(integrator.integrate(CONCAVE, LOWER, UPPER) < result);
  }

  @Test
  public void test() {
    final int n = 10000;
    final double lower = 0;
    final double upper = Math.PI / 4.;
    final double result = F.evaluate(upper) - F.evaluate(lower);
    Integrator1D<Double, Function1D<Double, Double>, Double> integrator = new NewtonCotesIntegrator1D(RuleType.RIGHT_HAND, n);
    assertEquals(result, integrator.integrate(DF, lower, upper), EPS);
    integrator = new NewtonCotesIntegrator1D(RuleType.LEFT_HAND, n);
    assertEquals(result, integrator.integrate(DF, lower, upper), EPS);
    integrator = new NewtonCotesIntegrator1D(RuleType.MID_POINT, n);
    assertEquals(result, integrator.integrate(DF, lower, upper), EPS);
    integrator = new NewtonCotesIntegrator1D(RuleType.TRAPEZOIDAL, n);
    assertEquals(result, integrator.integrate(DF, lower, upper), EPS);
    integrator = new NewtonCotesIntegrator1D(RuleType.SIMPSONS, n);
    assertEquals(result, integrator.integrate(DF, lower, upper), EPS);
    integrator = new NewtonCotesIntegrator1D(RuleType.BOOLES, n);
    assertEquals(result, integrator.integrate(DF, lower, upper), EPS);
  }
}
