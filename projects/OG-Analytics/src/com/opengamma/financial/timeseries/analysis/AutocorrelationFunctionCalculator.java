/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class AutocorrelationFunctionCalculator extends Function1D<DoubleTimeSeries<?>, double[]> {
  private final Function1D<DoubleTimeSeries<?>, double[]> _autoCovariance = new AutocovarianceFunctionCalculator();

  @Override
  public double[] evaluate(final DoubleTimeSeries<?> x) {
    Validate.notNull(x, "x");
    if (x.isEmpty()) {
      throw new IllegalArgumentException("Time series was empty");
    }
    final double[] covariance = _autoCovariance.evaluate(x);
    final double[] correlation = new double[covariance.length];
    correlation[0] = 1.;
    final double divisor = covariance[0];
    for (int i = 1; i < covariance.length; i++) {
      correlation[i] = covariance[i] / divisor;
    }
    return correlation;
  }

}
