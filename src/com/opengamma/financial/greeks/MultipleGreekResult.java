/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import java.util.Map;

import com.opengamma.util.ArgumentChecker;

public class MultipleGreekResult implements GreekResult<Map<String, Double>> {

  private final Map<String, Double> _result;

  public MultipleGreekResult(final Map<String, Double> result) {
    ArgumentChecker.checkNotNull(result, "Result map");
    // REVIEW kirk 2010-03-07 -- Is it okay to not take a copy of the result?
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
