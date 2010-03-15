/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

/**
 * @author emcleod
 * 
 */
public class SingleRiskFactorResult implements RiskFactorResult<Double> {
  private final Double _result;

  public SingleRiskFactorResult(final Double result) {
    if (result == null)
      throw new IllegalArgumentException("Risk factor was null");
    _result = result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekResult#getResult()
   */
  @Override
  public Double getResult() {
    return _result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekResult#isMultiValued()
   */
  @Override
  public boolean isMultiValued() {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_result == null) ? 0 : _result.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final SingleRiskFactorResult other = (SingleRiskFactorResult) obj;
    if (_result == null) {
      if (other._result != null)
        return false;
    } else if (!_result.equals(other._result))
      return false;
    return true;
  }

}
