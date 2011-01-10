/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class PopulationStandardDeviationCalculator extends Function1D<double[], Double> {
  private final Function1D<double[], Double> _variance = new PopulationVarianceCalculator();

  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    if (x.length < 2) {
      throw new IllegalArgumentException("Need at least two points to calculate standard deviation");
    }
    return Math.sqrt(_variance.evaluate(x));
  }

}
