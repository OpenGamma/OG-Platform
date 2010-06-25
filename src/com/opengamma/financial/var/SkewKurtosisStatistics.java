/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * @param <T> Type of data
 */
public class SkewKurtosisStatistics<T> extends NormalStatistics<T> {
  private final double _skew;
  private final double _kurtosis;

  public SkewKurtosisStatistics(final Function1D<T, Double> meanCalculator, final Function1D<T, Double> stdCalculator, final Function1D<T, Double> skewCalculator,
      final Function1D<T, Double> kurtosisCalculator, final T data) {
    super(meanCalculator, stdCalculator, data);
    Validate.notNull(skewCalculator, "skew calculator");
    Validate.notNull(kurtosisCalculator, "kurtosis calculator");
    Validate.notNull(data, "data");
    _skew = skewCalculator.evaluate(data);
    _kurtosis = kurtosisCalculator.evaluate(data);
  }

  public double getSkew() {
    return _skew;
  }

  public double getKurtosis() {
    return _kurtosis;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_kurtosis);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_skew);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SkewKurtosisStatistics<?> other = (SkewKurtosisStatistics<?>) obj;
    if (Double.doubleToLongBits(_kurtosis) != Double.doubleToLongBits(other._kurtosis)) {
      return false;
    }
    if (Double.doubleToLongBits(_skew) != Double.doubleToLongBits(other._skew)) {
      return false;
    }
    return true;
  }

}
