/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class BasisFunctionAggregation extends Function1D<Double, Double> {
  private final List<Function1D<Double, Double>> _f;
  private final double[] _w;

  public BasisFunctionAggregation(List<Function1D<Double, Double>> functions, double[] weights) {
    Validate.notEmpty(functions, "no functions");
    Validate.notNull(weights, "no weights");
    Validate.isTrue(functions.size() == weights.length);
    _f = functions;
    _w = weights;
  }

  @Override
  public Double evaluate(Double x) {
    double sum = 0;
    int n = _w.length;
    for (int i = 0; i < n; i++) {
      sum += _w[i] * _f.get(i).evaluate(x);
    }
    return sum;
  }

}
