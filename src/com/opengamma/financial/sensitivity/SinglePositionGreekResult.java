/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivity;

public class SinglePositionGreekResult implements PositionGreekResult<Double> {
  private final Double _result;

  public SinglePositionGreekResult(final Double result) {
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

}
