/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivity;

import java.util.HashMap;
import java.util.Map;

public class MultiplePositionGreekResult implements PositionGreekResult<Map<String, Double>> {
  private final Map<String, Double> _result;

  public MultiplePositionGreekResult(final Map<String, Double> result) {
    if (result == null)
      throw new IllegalArgumentException("Result map was null");
    if (result.isEmpty())
      throw new IllegalArgumentException("Result map was empty");
    _result = new HashMap<String, Double>();
    _result.putAll(result);
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
