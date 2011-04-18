/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive.robust;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.MedianCalculator;

/**
 * 
 */
public class InterquartileRangeCalculator extends Function1D<double[], Double> {
  private static final Function1D<double[], Double> MEDIAN = new MedianCalculator();

  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    final int n = x.length;
    Validate.isTrue(n > 3, "Need at least four points to calculate IQR");
    final double[] copy = Arrays.copyOf(x, n);
    Arrays.sort(copy);
    double[] lower, upper;
    if (n % 2 == 0) {
      lower = Arrays.copyOfRange(copy, 0, n / 2);
      upper = Arrays.copyOfRange(copy, n / 2, n);
    } else {
      lower = Arrays.copyOfRange(copy, 0, n / 2 + 1);
      upper = Arrays.copyOfRange(copy, n / 2, n);
    }
    return MEDIAN.evaluate(upper) - MEDIAN.evaluate(lower);
  }
}
