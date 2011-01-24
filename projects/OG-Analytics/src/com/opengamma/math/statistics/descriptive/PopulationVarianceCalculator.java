/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class PopulationVarianceCalculator extends Function1D<double[], Double> {
  private final Function1D<double[], Double> _variance = new SampleVarianceCalculator();

  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    if (x.length < 2) {
      throw new IllegalArgumentException("Need at least two points to calculate variance");
    }
    final int n = x.length;
    return _variance.evaluate(x) * (n - 1) / n;
  }
}
