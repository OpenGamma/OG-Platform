/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.regression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.opengamma.math.function.Function2D;

/**
 * 
 * @author emcleod
 */
public class NamedVariableLeastSquaresRegressionResultTest {
  private static final double EPS = 1e-12;

  @Test(expected = IllegalArgumentException.class)
  public void testNullNames() {
    new NamedVariableLeastSquaresRegressionResult(null, new LeastSquaresRegressionResult(null, null, null, null, null, null, null, null, false));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullRegression() {
    new NamedVariableLeastSquaresRegressionResult(new ArrayList<String>(), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonMatchingInputs() {
    final List<String> names = Arrays.asList("A", "B");
    final Double[] array = new Double[] { 1. };
    final LeastSquaresRegressionResult result = new LeastSquaresRegressionResult(array, array, 0., array, 0., 0., array, array, false);
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
    final Double[][] x = new Double[n][2];
    final Double[] y1 = new Double[n];
    final Double[] y2 = new Double[n];
    for (int i = 0; i < n; i++) {
      x[i][0] = Math.random();
      x[i][1] = Math.random();
      y1[i] = f1.evaluate(x[i]);
      y2[i] = f2.evaluate(x[i]);
    }
    final LeastSquaresRegression ols = new OrdinaryLeastSquaresRegression();
    final List<String> names = Arrays.asList("1", "2");
    final NamedVariableLeastSquaresRegressionResult result1 = new NamedVariableLeastSquaresRegressionResult(names, ols.regress(x, null, y1, false));
    final NamedVariableLeastSquaresRegressionResult result2 = new NamedVariableLeastSquaresRegressionResult(names, ols.regress(x, null, y2, true));
    try {
      result1.getPredictedValue((Map<String, Double>) null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    assertEquals(result1.getPredictedValue(Collections.<String, Double> emptyMap()), 0., 1e-16);
    try {
      final Map<String, Double> map = new HashMap<String, Double>();
      map.put("1", 0.);
      result1.getPredictedValue(map);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    Double x1, x2, x3;
    final Map<String, Double> var = new HashMap<String, Double>();
    for (int i = 0; i < 10; i++) {
      x1 = Math.random();
      x2 = Math.random();
      x3 = Math.random();
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
