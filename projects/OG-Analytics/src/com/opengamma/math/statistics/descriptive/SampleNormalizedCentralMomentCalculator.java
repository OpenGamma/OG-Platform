/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
public class SampleNormalizedCentralMomentCalculator extends Function1D<double[], Double> {
  private final int _n;
  private final Function1D<double[], Double> _moment;
  private final Function1D<double[], Double> _stdDev = new SampleStandardDeviationCalculator();

  public SampleNormalizedCentralMomentCalculator(final int n) {
    ArgumentChecker.notNegative(n, "n");
    _n = n;
    _moment = new SampleCentralMomentCalculator(n);
  }

  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x);
    if (x.length < 2) {
      throw new IllegalArgumentException("Need at least 2 data points to calculate moment");
    }
    if (_n == 0) {
      return 1.;
    }
    return _moment.evaluate(x) / Math.pow(_stdDev.evaluate(x), _n);
  }

}
