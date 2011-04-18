/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive.robust;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.MedianCalculator;

/**
 * 
 */
public class SampleMedianAbsoluteDeviationCalculator extends Function1D<double[], Double> {
  private static final Function1D<double[], Double> MEDIAN = new MedianCalculator();

  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    final int n = x.length;
    Validate.isTrue(n > 1, "Need at least two data points to calculate MAD");
    final double median = MEDIAN.evaluate(x);
    final double[] diff = new double[n];
    for (int i = 0; i < n; i++) {
      diff[i] = Math.abs(x[i] - median);
    }
    return MEDIAN.evaluate(diff);
  }

}
