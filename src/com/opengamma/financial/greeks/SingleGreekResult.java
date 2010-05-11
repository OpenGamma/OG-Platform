/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;


public class SingleGreekResult implements GreekResult<Double> {
  private final Double _result;

  public SingleGreekResult(final Double result) {
    if (result == null)
      throw new IllegalArgumentException("Result was null");
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
