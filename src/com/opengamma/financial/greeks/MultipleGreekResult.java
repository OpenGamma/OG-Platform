/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import java.util.Map;

public class MultipleGreekResult implements GreekResult<Map<String, Double>> {
  private final Map<String, Double> _result;

  public MultipleGreekResult(final Map<String, Double> result) {
    if (result == null)
      throw new IllegalArgumentException("Result was null");
    if (result.isEmpty())
      throw new IllegalArgumentException("Result was empty");
    _result = result;
  }

  @Override
  public Map<String, Double> getResult() {
    return _result;
  }

  @Override
  public boolean isMultiValued() {
    return true;
  }

}
