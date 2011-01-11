/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.regression;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function2D;

public class AdaptiveLeastSquaresRegressionTest {
  private static final RandomEngine RANDOM = new MersenneTwister(MersenneTwister64.DEFAULT_SEED);
  private static final double BETA_2 = -0.9;
  private static final double BETA_3 = 1.1;
  private static final int N = 100;
  private static final Double[][] X = new Double[N][3];
  private static final LeastSquaresRegression OLS = new OrdinaryLeastSquaresRegression();
  private static final LeastSquaresRegression ADAPTIVE = new AdaptiveLeastSquaresRegression(OLS, 0.025);
  private static final Function2D<Double, Double> F2 = new Function2D<Double, Double>() {

    @Override
    public Double evaluate(final Double x1, final Double x2) {
      return BETA_2 * x1 + BETA_3 * x2;
    }

  };
  private static final double EPS = 1e-2;

  @Test(expected = IllegalArgumentException.class)
  public void testInputs() {
    new AdaptiveLeastSquaresRegression(null, 0.05);
  }

  static {
    for (int i = 0; i < N; i++) {
      X[i][0] = RANDOM.nextDouble();
      X[i][1] = RANDOM.nextDouble();
      X[i][2] = RANDOM.nextDouble();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLevel() {
    new AdaptiveLeastSquaresRegression(new LeastSquaresRegression() {

      @Override
      public LeastSquaresRegressionResult regress(final double[][] x, final double[][] weights, final double[] y,
          final boolean useIntercept) {
        return null;
      }
    }, -0.5);
  }

  @Test
  public void test() {
    final double[] y = new double[N];
    final double[][] x = new double[N][3];
    for (int i = 0; i < N; i++) {
      y[i] = F2.evaluate(X[i]);
      for (int j = 1; j < 3; j++) {
        x[i][j] = X[i][j - 1];
      }
      x[i][0] = i % 2 == 0 ? -1. : 1.;
    }
    final LeastSquaresRegressionResult result = ADAPTIVE.regress(x, null, y, false);
    final double[] betas = result.getBetas();
    assertEquals(betas.length, 2);
    assertEquals(betas[0], BETA_2, EPS);
    assertEquals(betas[1], BETA_3, EPS);
  }
}
