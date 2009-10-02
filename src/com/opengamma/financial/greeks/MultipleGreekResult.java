package com.opengamma.financial.greeks;

import java.util.Map;

public class MultipleGreekResult implements GreekResult<Map<String, Double>> {

  private Map<String, Double> _result;

  public MultipleGreekResult(Map<String, Double> result) {
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
