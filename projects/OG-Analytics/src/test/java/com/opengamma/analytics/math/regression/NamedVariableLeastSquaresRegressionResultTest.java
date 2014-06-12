/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.regression;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NamedVariableLeastSquaresRegressionResultTest {
  private static final RandomEngine RANDOM = new MersenneTwister(MersenneTwister.DEFAULT_SEED);
  private static final double EPS = 1e-2;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNames() {
    new NamedVariableLeastSquaresRegressionResult(null, new LeastSquaresRegressionResult(null, null, 0, null, 0, 0,
        null, null, false));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRegression() {
    new NamedVariableLeastSquaresRegressionResult(new ArrayList<String>(), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNonMatchingInputs() {
    final List<String> names = Arrays.asList("A", "B");
    final double[] array = new double[] {1.};
    final LeastSquaresRegressionResult result = new LeastSquaresRegressionResult(array, array, 0., array, 0., 0.,
        array, array, false);
    new NamedVariableLeastSquaresRegressionResult(names, result);
  }

  @Test
  public void test() {
    final int n = 100;
    final double beta0 = 0.3;
    final double beta1 = 2.5;
    final double beta2 = -0.3;
    final Function2D<Double, Double> f1 = new Function2D<Double, Double>() {

      @Override
      public Double evaluate(final Double x1, final Double x2) {
        return beta1 * x1 + beta2 * x2;
      }

    };
    final Function2D<Double, Double> f2 = new Function2D<Double, Double>() {

      @Override
      public Double evaluate(final Double x1, final Double x2) {
        return beta0 + beta1 * x1 + beta2 * x2;
      }

    };
    final double[][] x = new double[n][2];
    final double[] y1 = new double[n];
    final double[] y2 = new double[n];
    for (int i = 0; i < n; i++) {
      x[i][0] = RANDOM.nextDouble();
      x[i][1] = RANDOM.nextDouble();
      y1[i] = f1.evaluate(x[i][0], x[i][1]);
      y2[i] = f2.evaluate(x[i][0], x[i][1]);
    }
    final LeastSquaresRegression ols = new OrdinaryLeastSquaresRegression();
    final List<String> names = Arrays.asList("1", "2");
    final NamedVariableLeastSquaresRegressionResult result1 = new NamedVariableLeastSquaresRegressionResult(names, ols
        .regress(x, null, y1, false));
    final NamedVariableLeastSquaresRegressionResult result2 = new NamedVariableLeastSquaresRegressionResult(names, ols
        .regress(x, null, y2, true));
    try {
      result1.getPredictedValue((Map<String, Double>) null);
      Assert.fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    assertEquals(result1.getPredictedValue(Collections.<String, Double> emptyMap()), 0., 1e-16);
    try {
      final Map<String, Double> map = new HashMap<>();
      map.put("1", 0.);
      result1.getPredictedValue(map);
      Assert.fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    double x1, x2, x3;
    final Map<String, Double> var = new HashMap<>();
    for (int i = 0; i < 10; i++) {
      x1 = RANDOM.nextDouble();
      x2 = RANDOM.nextDouble();
      x3 = RANDOM.nextDouble();
      var.put("1", x1);
      var.put("2", x2);
      assertEquals(result1.getPredictedValue(var), f1.evaluate(x1, x2), EPS);
      assertEquals(result2.getPredictedValue(var), f2.evaluate(x1, x2), EPS);
      var.put("3", x3);
      assertEquals(result1.getPredictedValue(var), f1.evaluate(x1, x2), EPS);
      assertEquals(result2.getPredictedValue(var), f2.evaluate(x1, x2), EPS);
    }
  }
}
