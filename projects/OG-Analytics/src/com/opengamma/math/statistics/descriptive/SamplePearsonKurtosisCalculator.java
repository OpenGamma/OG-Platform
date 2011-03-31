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
public class SamplePearsonKurtosisCalculator extends Function1D<double[], Double> {
  private static final Function1D<double[], Double> KURTOSIS = new SampleFisherKurtosisCalculator();

  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    Validate.isTrue(x.length >= 4, "Need at least four points to calculate kurtosis");
    return KURTOSIS.evaluate(x) + 3;
  }
}
