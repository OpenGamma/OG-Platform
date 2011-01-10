/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * The sample standard deviation of a series of data is defined as the square root of the sample variance (see {@link SampleVarianceCalculator}).
 */
public class SampleStandardDeviationCalculator extends Function1D<double[], Double> {
  private final Function1D<double[], Double> _variance = new SampleVarianceCalculator();

  /**
   * @param x The array of data
   * @return The sample standard deviation
   * @throws IllegalArgumentException If the array is null or contains fewer than two elements
   */
  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    if (x.length < 2) {
      throw new IllegalArgumentException("Need at least two points to calculate standard deviation");
    }
    return Math.sqrt(_variance.evaluate(x));
  }

}
