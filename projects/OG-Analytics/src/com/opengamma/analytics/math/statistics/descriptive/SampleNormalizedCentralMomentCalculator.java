/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;

/**
 * Calculates the $n^th$ normalized central moment of a series of data. Given
 * the $n^th$ central moment $\mu_n$ of a series of data with standard
 * deviation $\sigma$, the normalized central moment is given by:
 * $$
 * \begin{align*}
 * \mu_n' = \frac{\mu_n}{\sigma^n}
 * \end{align*}
 * $$
 * The normalization gives a scale-invariant, dimensionless quantity. The
 * normalized central moment is also known as the _standardized moment_.
 */
public class SampleNormalizedCentralMomentCalculator extends Function1D<double[], Double> {
  private static final Function1D<double[], Double> STD_DEV = new SampleStandardDeviationCalculator();
  private final int _n;
  private final Function1D<double[], Double> _moment;

  /**
   * @param n The degree of the moment of calculate, cannot be negative
   */
  public SampleNormalizedCentralMomentCalculator(final int n) {
    Validate.isTrue(n >= 0, "n must be >= 0");
    _n = n;
    _moment = new SampleCentralMomentCalculator(n);
  }

  /**
   * @param x The array of data, not null. Must contain at least two data points.
   * @return The normalized sample central moment.
   */
  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x);
    Validate.isTrue(x.length >= 2, "Need at least 2 data points to calculate normalized central moment");
    if (_n == 0) {
      return 1.;
    }
    return _moment.evaluate(x) / Math.pow(STD_DEV.evaluate(x), _n);
  }

}
