/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.sensitivity.PositionGreek;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class PositionGreekDataBundle {
  private final Map<PositionGreek, Double> _riskFactors;
  private final Map<Object, Double> _underlyingData;

  public PositionGreekDataBundle(final Map<PositionGreek, Double> riskFactors, final Map<Object, Double> underlyingData) {
    Validate.notNull(riskFactors, "Risk factors");
    Validate.notNull(underlyingData, "Underlying data");
    ArgumentChecker.notEmpty(riskFactors, "Risk factors");
    ArgumentChecker.notEmpty(underlyingData, "Underlying data");
    _riskFactors = riskFactors;
    _underlyingData = underlyingData;
  }

  public Map<PositionGreek, Double> getRiskFactorResults() {
    return _riskFactors;
  }

  public Map<Object, Double> getUnderlyingData() {
    return _underlyingData;
  }

  public double getUnderlyingDataForObject(final Object o) {
    if (_underlyingData.containsKey(o)) {
      return _underlyingData.get(o);
    }
    throw new IllegalArgumentException("Underlying data map did not contain a value for " + o);
  }

  public Double getRiskFactorValueForRiskFactor(final PositionGreek riskFactor) {
    if (_riskFactors.containsKey(riskFactor)) {
      return _riskFactors.get(riskFactor);
    }
    throw new IllegalArgumentException("Risk factor result collection did not contain a value for " + riskFactor);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_riskFactors == null) ? 0 : _riskFactors.hashCode());
    result = prime * result + ((_underlyingData == null) ? 0 : _underlyingData.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PositionGreekDataBundle other = (PositionGreekDataBundle) obj;
    return ObjectUtils.equals(_riskFactors, other._riskFactors) && ObjectUtils.equals(_underlyingData, other._underlyingData);
  }

}
