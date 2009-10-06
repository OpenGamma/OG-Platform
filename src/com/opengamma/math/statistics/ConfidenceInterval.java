/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics;

/**
 * 
 * @author emcleod
 */
public class ConfidenceInterval {
  private final double _lower;
  private final double _upper;
  private final double _confidenceLevel;
  private final double _value;

  public ConfidenceInterval(final double value, final double lower, final double upper, final double confidenceLevel) {
    _value = value;
    _lower = lower;
    _upper = upper;
    _confidenceLevel = confidenceLevel;
  }

  public double getValue() {
    return _value;
  }

  public double getLowerInterval() {
    return _lower;
  }

  public double getUpperInterval() {
    return _upper;
  }

  public double getConfidenceLevel() {
    return _confidenceLevel;
  }

  public boolean isWithinInterval(final double value) {
    return value > _lower || value < _upper;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_confidenceLevel);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_lower);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_upper);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_value);
    result = prime * result + (int) (temp ^ temp >>> 32);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final ConfidenceInterval other = (ConfidenceInterval) obj;
    if (Double.doubleToLongBits(_confidenceLevel) != Double.doubleToLongBits(other._confidenceLevel))
      return false;
    if (Double.doubleToLongBits(_lower) != Double.doubleToLongBits(other._lower))
      return false;
    if (Double.doubleToLongBits(_upper) != Double.doubleToLongBits(other._upper))
      return false;
    if (Double.doubleToLongBits(_value) != Double.doubleToLongBits(other._value))
      return false;
    return true;
  }
}
