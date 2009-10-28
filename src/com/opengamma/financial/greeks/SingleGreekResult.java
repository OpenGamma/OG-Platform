package com.opengamma.financial.greeks;

import com.opengamma.util.CompareUtils;

public class SingleGreekResult implements GreekResult<Double> {
  private final Double _result;

  public SingleGreekResult(final Double result) {
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

  @Override
  public String toString() {
    return _result.toString();
  }

}
