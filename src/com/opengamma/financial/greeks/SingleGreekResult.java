package com.opengamma.financial.greeks;

import com.opengamma.util.CompareUtils;

public class SingleGreekResult implements GreekResult<Double> {

  private Double _result;

  public SingleGreekResult(Double result) {
    CompareUtils.checkForNull(result);
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
