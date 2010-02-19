/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import com.opengamma.math.function.Function1D;

/**
 * @author emcleod
 * 
 */
public class SkewKurtosisStatistics<T> extends NormalStatistics<T> {
  private final double _skew;
  private final double _kurtosis;

  public SkewKurtosisStatistics(final Function1D<T, Double> meanCalculator, final Function1D<T, Double> stdCalculator, final Function1D<T, Double> skewCalculator,
      final Function1D<T, Double> kurtosisCalculator, final T data) {
    super(meanCalculator, stdCalculator, data);
    if (skewCalculator == null)
      throw new IllegalArgumentException("Skew calculator was null");
    if (kurtosisCalculator == null)
      throw new IllegalArgumentException("Kurtosis calculator was null");
    if (data == null)
      throw new IllegalArgumentException("Data were null");
    _skew = skewCalculator.evaluate(data);
    _kurtosis = kurtosisCalculator.evaluate(data);
  }

  public double getSkew() {
    return _skew;
  }

  public double getKurtosis() {
    return _kurtosis;
  }

}
