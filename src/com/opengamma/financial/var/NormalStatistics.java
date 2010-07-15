/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * @param <T> Type of the data
 */
public class NormalStatistics<T> {
  private final Double _mean;
  private final double _standardDeviation;

  // TODO data shouldn't go here - need to have ability to change and
  // recalculate
  public NormalStatistics(final Function1D<T, Double> meanCalculator, final Function1D<T, Double> stdCalculator, final T data) {
    Validate.notNull(meanCalculator, "mean calculator");
    Validate.notNull(stdCalculator, "standard deviation calculator");
    Validate.notNull(data, "data");
    _mean = meanCalculator.evaluate(data);
    _standardDeviation = stdCalculator.evaluate(data);
  }

  public Double getMean() {
    return _mean;
  }

  public double getStandardDeviation() {
    return _standardDeviation;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_mean == null) ? 0 : _mean.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_standardDeviation);
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
    NormalStatistics<?> other = (NormalStatistics<?>) obj;
    if (!ObjectUtils.equals(_mean, other._mean)) {
      return false;
    }
    if (Double.doubleToLongBits(_standardDeviation) != Double.doubleToLongBits(other._standardDeviation)) {
      return false;
    }
    return true;
  }

}
