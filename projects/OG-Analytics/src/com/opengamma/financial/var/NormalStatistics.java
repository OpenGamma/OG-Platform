/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function;

/**
 * @param <T> Type of the data
 */
public class NormalStatistics<T> {
  private final double _mean;
  private final double _standardDeviation;

  // TODO data shouldn't go here - need to have ability to change and
  // recalculate
  public NormalStatistics(final Function<T, Double> meanCalculator, final Function<T, Double> stdCalculator, final T... data) {
    Validate.notNull(stdCalculator, "standard deviation calculator");
    Validate.notNull(data, "data");
    if (meanCalculator != null) {
      _mean = meanCalculator.evaluate(data);
    } else {
      _mean = 0;
    }
    _standardDeviation = stdCalculator.evaluate(data);
  }

  public double getMean() {
    return _mean;
  }

  public double getStandardDeviation() {
    return _standardDeviation;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_mean);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_standardDeviation);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final NormalStatistics other = (NormalStatistics) obj;
    if (Double.doubleToLongBits(_mean) != Double.doubleToLongBits(other._mean)) {
      return false;
    }
    if (Double.doubleToLongBits(_standardDeviation) != Double.doubleToLongBits(other._standardDeviation)) {
      return false;
    }
    return true;
  }

}
