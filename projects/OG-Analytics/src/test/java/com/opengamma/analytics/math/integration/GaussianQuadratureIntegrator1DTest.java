/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.integration;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class GaussianQuadratureIntegrator1DTest {

  private static final Function1D<Double, Double> ONE = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 1.0;
    }
  };

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

  private static final Function1D<Double, Double> DF3 = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return Math.exp(-x * x);
    }

  };

  private static final Function1D<Double, Double> COS = new Function1D<Double, Double>() {
    @Override
    public Double evaluate(final Double x) {
      return Math.cos(x);
    }
  };

  private static final Function1D<Double, Double> COS_EXP = new Function1D<Double, Double>() {
    @Override
    public Double evaluate(final Double x) {
      return Math.cos(x) * Math.exp(-x * x);
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

  @Test
  public void testGaussHermite() {
    final double rootPI = Math.sqrt(Math.PI);
    final double upper = Double.POSITIVE_INFINITY;
    final double lower = Double.NEGATIVE_INFINITY;
    final GaussHermiteQuadratureIntegrator1D integrator = new GaussHermiteQuadratureIntegrator1D(10);
    assertEquals(rootPI, integrator.integrateFromPolyFunc(ONE), 1e-15);
    assertEquals(rootPI, integrator.integrate(DF3, lower, upper), EPS);
  }

  @Test
  public void testGaussHermite2() {
    final RungeKuttaIntegrator1D rk = new RungeKuttaIntegrator1D(1e-15);
    final Double expected = 2 * rk.integrate(COS_EXP, 0., 10.);
    final GaussHermiteQuadratureIntegrator1D gh = new GaussHermiteQuadratureIntegrator1D(11);
    final double res1 = gh.integrateFromPolyFunc(COS);
    assertEquals(expected, res1, 1e-15); //11 points gets you machine precision
    final double res2 = gh.integrate(COS_EXP, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    assertEquals(expected, res2, 1e-15);
  }

  /**
   * This test demonstrates why it is a bad idea to use quadrature methods for non-smooth functions 
   */
  @Test
  public void testBlackFormula() {
    final double fwd = 5;
    final double strike = 6.5;
    final double t = 1.5;
    final double vol = 0.35;
    final double expected = BlackFormulaRepository.price(fwd, strike, t, vol, true);

    final Function1D<Double, Double> func = getBlackIntergrand(fwd, strike, t, vol);

    final Function1D<Double, Double> fullIntergrand = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        return func.evaluate(x) * Math.exp(-x * x);
      }
    };

    final RungeKuttaIntegrator1D rk = new RungeKuttaIntegrator1D(1e-15);
    final double resRK = rk.integrate(fullIntergrand, 0., 10.); //The strike > fwd, so can start the integration at z=0 (i.e. s = fwd)
    assertEquals("Runge Kutta", expected, resRK, 1e-15);

    final GaussHermiteQuadratureIntegrator1D gh = new GaussHermiteQuadratureIntegrator1D(40);
    final double resGH = gh.integrateFromPolyFunc(func);
    assertEquals("Gauss Hermite", expected, resGH, 1e-2); //terrible accuracy even with 40 points 
  }

  private Function1D<Double, Double> getBlackIntergrand(final double fwd, final double k, final double t, final double vol) {
    final double rootPI = Math.sqrt(Math.PI);
    final double sigmaSqrTO2 = vol * vol * t / 2;
    final double sigmaRoot2T = vol * Math.sqrt(2 * t);

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        final double s = fwd * Math.exp(-sigmaSqrTO2 + sigmaRoot2T * x);
        return Math.max(s - k, 0) / rootPI;
      }
    };
  }
}
