/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import java.util.Map;

/**
 * @author emcleod
 * 
 */
public class MultipleRiskFactorResult implements RiskFactorResult<Map<Object, Double>> {
  private final Map<Object, Double> _result;

  public MultipleRiskFactorResult(final Map<Object, Double> result) {
    if (result == null)
      throw new IllegalArgumentException("Result map was null");
    if (result.isEmpty())
      throw new IllegalArgumentException("Result map was empty");
    _result = result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.riskfactor.RiskFactorResult#getResult()
   */
  @Override
  public Map<Object, Double> getResult() {
    return _result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.riskfactor.RiskFactorResult#isMultiValued()
   */
  @Override
  public boolean isMultiValued() {
    return true;
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
    final MultipleRiskFactorResult other = (MultipleRiskFactorResult) obj;
    if (_result == null) {
      if (other._result != null)
        return false;
    } else if (!_result.equals(other._result))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "MultipleRiskFactorResult[" + _result + "]";
  }
}
