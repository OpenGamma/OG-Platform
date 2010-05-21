/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;

public class SingleGreekResult implements GreekResult<Double> {
  // REVIEW kirk 2010-05-21 -- This should probably be a double rather than a Double.
  private final Double _result;

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
    return _result.toString();
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
    return ObjectUtils.equals(_result, other._result);
  }

  @Override
  public int hashCode() {
    return _result.hashCode();
  }

}
