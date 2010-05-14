/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import java.util.Map;

import com.opengamma.financial.sensitivity.PositionGreek;
import com.opengamma.financial.sensitivity.PositionGreekResult;

/**
 * 
 */
public class PositionGreekDataBundle {
  private final Map<PositionGreek, PositionGreekResult<?>> _riskFactors;
  private final Map<Object, Double> _underlyingData;

  public PositionGreekDataBundle(final Map<PositionGreek, PositionGreekResult<?>> riskFactors, final Map<Object, Double> underlyingData) {
    if (riskFactors == null)
      throw new IllegalArgumentException("RiskFactorResultCollection was null");
    if (riskFactors.isEmpty())
      throw new IllegalArgumentException("RiskFactorResultCollection was empty");
    if (underlyingData == null)
      throw new IllegalArgumentException("Underlying data map was null");
    if (underlyingData.isEmpty())
      throw new IllegalArgumentException("Underlying data map was empty");
    _riskFactors = riskFactors;
    _underlyingData = underlyingData;
  }

  public Map<PositionGreek, PositionGreekResult<?>> getRiskFactorResults() {
    return _riskFactors;
  }

  public Map<Object, Double> getUnderlyingData() {
    return _underlyingData;
  }

  public double getUnderlyingDataForObject(final Object o) {
    if (_underlyingData.containsKey(o))
      return _underlyingData.get(o);
    throw new IllegalArgumentException("Underlying data map did not contain a value for " + o);
  }

  public PositionGreekResult<?> getRiskFactorValueForRiskFactor(final PositionGreek riskFactor) {
    if (_riskFactors.containsKey(riskFactor))
      return _riskFactors.get(riskFactor);
    throw new IllegalArgumentException("Risk factor result collection did not contain a value for " + riskFactor);
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
    result = prime * result + ((_riskFactors == null) ? 0 : _riskFactors.hashCode());
    result = prime * result + ((_underlyingData == null) ? 0 : _underlyingData.hashCode());
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
    final PositionGreekDataBundle other = (PositionGreekDataBundle) obj;
    if (_riskFactors == null) {
      if (other._riskFactors != null)
        return false;
    } else if (!_riskFactors.equals(other._riskFactors))
      return false;
    if (_underlyingData == null) {
      if (other._underlyingData != null)
        return false;
    } else if (!_underlyingData.equals(other._underlyingData))
      return false;
    return true;
  }

}
