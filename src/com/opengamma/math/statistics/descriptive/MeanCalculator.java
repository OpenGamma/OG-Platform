/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class MeanCalculator extends Function1D<double[], Double> {

  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x);
    ArgumentChecker.notEmpty(x, "x");
    if (x.length == 1) {
      return x[0];
    }
    double sum = 0;
    for (final Double d : x) {
      sum += d;
    }
    return sum / x.length;
  }

}
