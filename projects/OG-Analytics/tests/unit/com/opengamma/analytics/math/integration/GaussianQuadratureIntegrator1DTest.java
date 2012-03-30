/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.integration;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.GaussHermiteQuadratureIntegrator1D;
import com.opengamma.analytics.math.integration.GaussJacobiQuadratureIntegrator1D;
import com.opengamma.analytics.math.integration.GaussLaguerreQuadratureIntegrator1D;
import com.opengamma.analytics.math.integration.GaussLegendreQuadratureIntegrator1D;
import com.opengamma.analytics.math.integration.Integrator1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;

/**
 * 
 */
public class GaussianQuadratureIntegrator1DTest {
  private static final Function1D<Double, Double> DF1 = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * x * x * (x - 4);
    }

  };
  private static final Function1D<Double, Double> F1 = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * x * x * x * (x / 5. - 1);
    }

  };
  private static final Function1D<Double, Double> DF2 = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return Math.exp(-2 * x);
    }

  };
  @SuppressWarnings("unused")
  private static final Function1D<Double, Double> DF3 = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return Math.exp(-x * x);
    }

  };
  private static final double EPS = 1e-6;

  @Test
  public void testGaussLegendre() {
    double upper = 2;
    double lower = -6;
    final Integrator1D<Double, Double> integrator = new GaussLegendreQuadratureIntegrator1D(6);
    assertEquals(F1.evaluate(upper) - F1.evaluate(lower), integrator.integrate(DF1, lower, upper), EPS);
    lower = -0.56;
    upper = 1.4;
    assertEquals(F1.evaluate(upper) - F1.evaluate(lower), integrator.integrate(DF1, lower, upper), EPS);
  }

  @Test
  public void testGaussLaguerre() {
    final double upper = Double.POSITIVE_INFINITY;
    final double lower = 0;
    final Integrator1D<Double, Double> integrator = new GaussLaguerreQuadratureIntegrator1D(15);
    assertEquals(0.5, integrator.integrate(DF2, lower, upper), EPS);
  }

  @Test
  public void testRungeKutta() {
    final RungeKuttaIntegrator1D integrator = new RungeKuttaIntegrator1D();
    final double lower = -1;
    final double upper = 2;
    assertEquals(F1.evaluate(upper) - F1.evaluate(lower), integrator.integrate(DF1, lower, upper), EPS);
  }

  @Test
  public void testGaussJacobi() {
    final double upper = 12;
    final double lower = -1;
    final Integrator1D<Double, Double> integrator = new GaussJacobiQuadratureIntegrator1D(7);
    assertEquals(F1.evaluate(upper) - F1.evaluate(lower), integrator.integrate(DF1, lower, upper), EPS);
  }

  @SuppressWarnings("unused")
  @Test
  public void testGaussHermite() {
    final double upper = Double.POSITIVE_INFINITY;
    final double lower = Double.NEGATIVE_INFINITY;
    final Integrator1D<Double, Double> integrator = new GaussHermiteQuadratureIntegrator1D(10);
    //assertEquals(1, integrator.integrate(DF3, lower, upper), EPS);
  }
}
