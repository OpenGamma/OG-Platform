/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ConfidenceInterval {
  private final double _lower;
  private final double _upper;
  private final double _confidenceLevel;
  private final double _value;

  public ConfidenceInterval(final double value, final double lower, final double upper, final double confidenceLevel) {
    if (!ArgumentChecker.isInRangeInclusive(0, 1, confidenceLevel)) {
      throw new IllegalArgumentException("Confidence level must be in the range 0 <= x <= 1");
    }
    if (lower >= upper) {
      throw new IllegalArgumentException("Lower bound must be less than upper bound");
    }
    if (value < lower) {
      throw new IllegalArgumentException("Lower bound must be less than the value");
    }
    if (value > upper) {
      throw new IllegalArgumentException("Upper bound must be greater than the value");
    }
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

  public boolean isWithinInterval(final double x) {
    return x > _lower && x < _upper;
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
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ConfidenceInterval other = (ConfidenceInterval) obj;
    if (Double.doubleToLongBits(_confidenceLevel) != Double.doubleToLongBits(other._confidenceLevel)) {
      return false;
    }
    if (Double.doubleToLongBits(_lower) != Double.doubleToLongBits(other._lower)) {
      return false;
    }
    if (Double.doubleToLongBits(_upper) != Double.doubleToLongBits(other._upper)) {
      return false;
    }
    if (Double.doubleToLongBits(_value) != Double.doubleToLongBits(other._value)) {
      return false;
    }
    return true;
  }
}
