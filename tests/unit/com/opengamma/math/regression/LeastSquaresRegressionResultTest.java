/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.regression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function2D;

public class LeastSquaresRegressionResultTest {
  private static final RandomEngine RANDOM = new MersenneTwister(MersenneTwister64.DEFAULT_SEED);
  private static final double EPS = 1e-2;

  @Test(expected = IllegalArgumentException.class)
  public void testInputs() {
    new LeastSquaresRegressionResult(null);
  }

  @Test
  public void testPredictedValue() {
    final double beta0 = 3.9;
    final double beta1 = -1.4;
    final double beta2 = 4.6;
    final Function2D<Double, Double> f1 = new Function2D<Double, Double>() {

      @Override
      public Double evaluate(final Double x1, final Double x2) {
        return x1 * beta1 + x2 * beta2;
      }

    };
    final Function2D<Double, Double> f2 = new Function2D<Double, Double>() {

      @Override
      public Double evaluate(final Double x1, final Double x2) {
        return beta0 + x1 * beta1 + x2 * beta2;
      }

    };
    final int n = 100;
    final double[][] x = new double[n][2];
    final double[] y1 = new double[n];
    final double[] y2 = new double[n];
    for (int i = 0; i < n; i++) {
      x[i][0] = RANDOM.nextDouble();
      x[i][1] = RANDOM.nextDouble();
      y1[i] = f1.evaluate(x[i][0], x[i][1]);
      y2[i] = f2.evaluate(x[i][0], x[i][1]);
    }
    final LeastSquaresRegression regression = new OrdinaryLeastSquaresRegression();
    final LeastSquaresRegressionResult result1 = regression.regress(x, null, y1, false);
    final LeastSquaresRegressionResult result2 = regression.regress(x, null, y2, true);
    try {
      result1.getPredictedValue(null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      result1.getPredictedValue(new double[] { 2.4, 2.5, 3.4 });
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      result2.getPredictedValue(new double[] { 1.3 });
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    double[] z;
    for (int i = 0; i < 10; i++) {
      z = new double[] { RANDOM.nextDouble(), RANDOM.nextDouble() };
      assertEquals(f1.evaluate(z[0], z[1]), result1.getPredictedValue(z), EPS);
      assertEquals(f2.evaluate(z[0], z[1]), result2.getPredictedValue(z), EPS);
    }
  }
}
