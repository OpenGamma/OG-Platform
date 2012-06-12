/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

/**
 * Object containing information about a volatility cube parameterised by delta.
 * @param <X> Type of the x-axis data
 * @param <Y> Type of the y-axis data
 * @param <Z> Type of the z-axis data
 */
public class VolatilityDeltaCubeData<X, Y, Z> {
  private final Map<Triple<X, Y, Z>, Double> _volatilityPoints;

  public VolatilityDeltaCubeData(final Map<Triple<X, Y, Z>, Double> volatilityPoints) {
    ArgumentChecker.notNull(volatilityPoints, "volatility points");
    _volatilityPoints = volatilityPoints;
  }

  public Map<Triple<X, Y, Z>, Double> getVolatilityPoints() {
    return _volatilityPoints;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _volatilityPoints.hashCode();
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
    final VolatilityDeltaCubeData<?, ?, ?> other = (VolatilityDeltaCubeData<?, ?, ?>) obj;
    return ObjectUtils.equals(_volatilityPoints, other._volatilityPoints);
  }

}
