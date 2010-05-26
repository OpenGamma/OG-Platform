/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class AutocorrelationFunctionCalculator extends Function1D<DoubleTimeSeries<?>, Double[]> {
  private final Function1D<DoubleTimeSeries<?>, Double[]> _autoCovariance = new AutocovarianceFunctionCalculator();

  @Override
  public Double[] evaluate(final DoubleTimeSeries<?> x) {
    ArgumentChecker.notNull(x, "x");
    if (x.isEmpty())
      throw new IllegalArgumentException("Time series was empty");
    final Double[] covariance = _autoCovariance.evaluate(x);
    final Double[] correlation = new Double[covariance.length];
    correlation[0] = 1.;
    final double divisor = covariance[0];
    for (int i = 1; i < covariance.length; i++) {
      correlation[i] = covariance[i] / divisor;
    }
    return correlation;
  }

}
