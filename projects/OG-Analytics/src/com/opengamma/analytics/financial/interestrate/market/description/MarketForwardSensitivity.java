/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.market.description;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

/**
 * Object representing a one point sensitivity to a forward curve. It is stored as a Triple of Double and a double. 
 * The Triple represents the reference point as the start time, end time and accrual factor; the double is the value at that point.
 */
public class MarketForwardSensitivity {
  /**
   * The point (start time, end time and accrual factor).
   */
  private final Triple<Double, Double, Double> _startEndFactor;
  /**
   * The value.
   */
  private final double _value;

  /**
   * Constructor
   * @param point The pair.
   * @param value The sensitivity value.
   */
  public MarketForwardSensitivity(Triple<Double, Double, Double> point, Double value) {
    ArgumentChecker.notNull(point, "Point");
    _startEndFactor = point;
    _value = value;
  }

  public Triple<Double, Double, Double> getPoint() {
    return _startEndFactor;
  }

  public double getValue() {
    return _value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _startEndFactor.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_value);
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
    MarketForwardSensitivity other = (MarketForwardSensitivity) obj;
    if (!ObjectUtils.equals(_startEndFactor, other._startEndFactor)) {
      return false;
    }
    if (Double.doubleToLongBits(_value) != Double.doubleToLongBits(other._value)) {
      return false;
    }
    return true;
  }

}
