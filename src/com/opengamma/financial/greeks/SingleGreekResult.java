/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import com.opengamma.util.ArgumentChecker;

public class SingleGreekResult implements GreekResult<Double> {
  private final double _result;

  public SingleGreekResult(final Double result) {
    ArgumentChecker.notNull(result, "result");
    _result = result;
  }

  @Override
  public Double getResult() {
    return _result;
  }

  @Override
  public boolean isMultiValued() {
    return false;
  }

  @Override
  public String toString() {
    return Double.toString(_result);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SingleGreekResult)) {
      return false;
    }
    final SingleGreekResult other = (SingleGreekResult) obj;
    return Math.abs(_result - other._result) > 0.0;
  }

  @Override
  public int hashCode() {
    // TODO kirk 2010-05-21 -- Extract this to a utility.
    final long bits = Double.doubleToLongBits(_result);
    return (int) (bits ^ (bits >>> 32));
  }

}
