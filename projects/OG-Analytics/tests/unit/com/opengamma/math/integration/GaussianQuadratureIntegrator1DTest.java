/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
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
  private static final double EPS = 1e-6;

  @Test
  public void testGaussLegendre() {
    double upper = 1;
    double lower = -1;
    final QuadratureWeightAndAbscissaFunction generator = new GaussLegendreOrthogonalPolynomialGeneratingFunction();
    final Integrator1D<Double, Double> integrator = new GaussianQuadratureIntegrator1D(6, generator);
    assertEquals(F1.evaluate(upper) - F1.evaluate(lower), integrator.integrate(DF1, lower, upper), EPS);
    lower = -0.56;
    upper = 1.4;
    assertEquals(F1.evaluate(upper) - F1.evaluate(lower), integrator.integrate(DF1, lower, upper), EPS);
  }

  @Test
  public void testGaussLaguerre() {
    final QuadratureWeightAndAbscissaFunction generator = new GaussLaguerreOrthogonalPolynomialGeneratingFunction();
    final Integrator1D<Double, Double> integrator = new GaussianQuadratureIntegrator1D(16, generator);
    assertEquals(1. / 3, integrator.integrate(DF2, 0., 1.), EPS);
    assertEquals(1. / 3, integrator.integrate(DF2, -1000., 1000.), EPS);
  }

  @Test
  public void testRungeKutta() {
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D();
    final double lower = -1;
    final double upper = 1;
    assertEquals(F1.evaluate(upper) - F1.evaluate(lower), integrator.integrate(DF1, lower, upper), EPS);
  }

  @Test
  public void testGaussJacobi() {
    final double upper = 1;
    final double lower = -1;
    final QuadratureWeightAndAbscissaFunction generator = new GaussJacobiOrthogonalPolynomialGeneratingFunction(0, 0);
    final Integrator1D<Double, Double> integrator = new GaussianQuadratureIntegrator1D(7, generator);
    assertEquals(F1.evaluate(upper) - F1.evaluate(lower), integrator.integrate(DF1, lower, upper), EPS);
  }

  //  @Test
  //  public void testGaussHermite() {
  //    final double upper = 1;
  //    final double lower = -1;
  //    final QuadratureWeightAndAbscissaFunction generator = new GaussHermiteOrthogonalPolynomialGeneratingFunction();
  //    final Integrator1D<Double, Double> integrator = new GaussianQuadratureIntegrator1D(10, generator);
  //    assertEquals(F1.evaluate(upper) - F1.evaluate(lower), integrator.integrate(DF1, lower, upper), EPS);
  //  }
}
