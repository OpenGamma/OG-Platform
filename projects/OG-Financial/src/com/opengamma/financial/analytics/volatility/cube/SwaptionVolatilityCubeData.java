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
 * Object containing information about a volatility cube parameterised by swap tenor, swaption expiry and relative strike in bps.
 * @param <X> Type of the x-axis data
 * @param <Y> Type of the y-axis data
 * @param <Z> Type of the z-axis data
 */
public class SwaptionVolatilityCubeData<X, Y, Z> {
  private final Map<Triple<X, Y, Z>, Double> _volatilityPoints;

  public SwaptionVolatilityCubeData(final Map<Triple<X, Y, Z>, Double> volatilityPoints) {
    ArgumentChecker.notNull(volatilityPoints, "volatility points");
    _volatilityPoints = volatilityPoints;
  }

  public Map<Triple<X, Y, Z>, Double> getVolatilityPoints() {
    return _volatilityPoints;
  }

  public Double getVolatilityForPoint(final X x, final Y y, final Z z) {
    final Triple<X, Y, Z> coordinate = Triple.of(x, y, z);
    if (_volatilityPoints.containsKey(coordinate)) {
      return _volatilityPoints.get(coordinate);
    }
    return null;
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
    final SwaptionVolatilityCubeData<?, ?, ?> other = (SwaptionVolatilityCubeData<?, ?, ?>) obj;
    return ObjectUtils.equals(_volatilityPoints, other._volatilityPoints);
  }

}
