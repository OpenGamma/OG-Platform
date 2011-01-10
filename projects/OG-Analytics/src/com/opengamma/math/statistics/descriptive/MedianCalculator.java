/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class MedianCalculator extends Function1D<double[], Double> {

  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x);
    ArgumentChecker.notEmpty(x, "x");
    if (x.length == 1) {
      return x[0];
    }
    final double[] x1 = Arrays.copyOf(x, x.length);
    Arrays.sort(x1);
    final int mid = x1.length / 2;
    if (x1.length % 2 == 1) {
      return x1[mid];
    }
    return (x1[mid] + x1[mid - 1]) / 2.;
  }

}
