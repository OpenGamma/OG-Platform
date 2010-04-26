/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import com.opengamma.util.ArgumentChecker;

public class SingleGreekResult implements GreekResult<Double> {
  private final Double _result;

  public SingleGreekResult(final Double result) {
    ArgumentChecker.notNull(result, "Result");
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

}
