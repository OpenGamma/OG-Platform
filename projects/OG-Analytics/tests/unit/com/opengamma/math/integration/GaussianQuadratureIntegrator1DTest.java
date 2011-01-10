/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class GaussianQuadratureIntegrator1DTest {
  private static final Function1D<Double, Double> DF1 = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * x * x * x - 10;
    }

  };
  private static final Function1D<Double, Double> F1 = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * x * x * x * x / 5. - 10 * x;
    }

  };
  private static final Function1D<Double, Double> DF2 = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return Math.exp(-2 * x);
    }

  };
  private static final Function1D<Double, Double> DF3 = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return Math.cos(x);
    }

  };
  private static final double EPS = 1e-3;

  @Test
  public void testGaussLegendre() {
    double upper = 1;
    double lower = -1;
    final OrthogonalPolynomialGeneratingFunction generator = new GaussLegendreOrthogonalPolynomialGeneratingFunction();
    final Integrator1D<Double, Function1D<Double, Double>, Double> integrator = new GaussianQuadratureIntegrator1D(100, generator);
    assertEquals(F1.evaluate(upper) - F1.evaluate(lower), integrator.integrate(DF1, lower, upper), EPS);
    lower = -0.56;
    upper = 1.4;
    assertEquals(F1.evaluate(upper) - F1.evaluate(lower), integrator.integrate(DF1, lower, upper), EPS);
  }

  @Test
  public void testGaussLaguerre() {
    final OrthogonalPolynomialGeneratingFunction generator = new GaussLaguerreOrthogonalPolynomialGeneratingFunction();
    final Integrator1D<Double, Function1D<Double, Double>, Double> integrator = new GaussianQuadratureIntegrator1D(100, generator);
    assertEquals(1. / 3, integrator.integrate(DF2, 0., 1.), EPS);
    assertEquals(1. / 3, integrator.integrate(DF2, -1000., 1000.), EPS);
  }

  @Test
  public void testGaussJacobi() {
    final double upper = 1;
    final double lower = -1;
    final OrthogonalPolynomialGeneratingFunction generator = new GaussJacobiOrthogonalPolynomialGeneratingFunction(0, 0);
    final Integrator1D<Double, Function1D<Double, Double>, Double> integrator = new GaussianQuadratureIntegrator1D(100, generator);
    assertEquals(F1.evaluate(upper) - F1.evaluate(lower), integrator.integrate(DF1, lower, upper), EPS);
  }

  @Test
  public void testGaussHermite() {
    final OrthogonalPolynomialGeneratingFunction generator = new GaussHermiteOrthogonalPolynomialGeneratingFunction();
    final Integrator1D<Double, Function1D<Double, Double>, Double> integrator = new GaussianQuadratureIntegrator1D(100, generator);
    final double result = Math.sqrt(Math.PI) * Math.exp(-0.25);
    assertEquals(result, integrator.integrate(DF3, 0., 1.), EPS);
  }
}
