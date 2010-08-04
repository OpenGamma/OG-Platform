/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.temp;

/**
 * 
 */
public class InterpolationResult {
  private final double _result;

  public InterpolationResult(final double result) {
    _result = result;
  }

  public double getResult() {
    return _result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_result);
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
    final InterpolationResult other = (InterpolationResult) obj;
    if (Double.doubleToLongBits(_result) != Double.doubleToLongBits(other._result)) {
      return false;
    }
    return true;
  }

}
