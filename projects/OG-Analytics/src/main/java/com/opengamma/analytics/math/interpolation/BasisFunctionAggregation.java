/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * 
 * @param <T> The domain type of the function (e.g. Double, double[], DoubleMatrix1D etc) 
 */
public class BasisFunctionAggregation<T> extends Function1D<T, Double> {
  private final List<Function1D<T, Double>> _f;
  private final double[] _w;

  public BasisFunctionAggregation(final List<Function1D<T, Double>> functions, final double[] weights) {
    Validate.notEmpty(functions, "no functions");
    Validate.notNull(weights, "no weights");
    Validate.isTrue(functions.size() == weights.length);
    _f = functions;
    _w = weights;
  }

  @Override
  public Double evaluate(final T x) {
    double sum = 0;
    final int n = _w.length;
    for (int i = 0; i < n; i++) {
      sum += _w[i] * _f.get(i).evaluate(x);
    }
    return sum;
  }

  /**
   * The sensitivity of the value at a point x to the weights of the basis functions 
   * @param x value to be evaluated 
   * @return sensitivity w
   */
  public DoubleMatrix1D weightSensitivity(final T x) {
    final int n = _w.length;
    final double[] temp = new double[n];
    for (int i = 0; i < n; i++) {
      temp[i] = _f.get(i).evaluate(x);
    }
    return new DoubleMatrix1D(temp);
  }

}
