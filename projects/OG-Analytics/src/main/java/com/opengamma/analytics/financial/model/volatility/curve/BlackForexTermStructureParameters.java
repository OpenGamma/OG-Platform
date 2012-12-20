/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.curve;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.volatility.VolatilityModel;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Class describing the Black volatility term structure used in option pricing.
 */
public class BlackForexTermStructureParameters implements VolatilityModel<Double> {

  /**
   * The volatility term structure. The dimension is the expiration. Not null.
   */
  private final DoublesCurve _volatility;

  /**
   * Constructor from a curve.
   * @param volatility The term structure of implied volatility. Not null.
   */
  public BlackForexTermStructureParameters(final DoublesCurve volatility) {
    ArgumentChecker.notNull(volatility, "Volatility");
    _volatility = volatility;
  }

  /**
   * Returns the volatility term structure curve.
   * @return The curve.
   */
  public DoublesCurve getVolatilityCurve() {
    return _volatility;
  }

  /**
   * Returns the time sensitivity of the volatility
   * @param t The time to expiration.
   * @return The volatility.
   */
  public Double[] getVolatilityTimeSensitivity(final double t) {
    return _volatility.getYValueParameterSensitivity(t);
  }

  /**
   * Returns the implied volatility for a given expiration.
   * @param time The time to expiration, not null
   * @return The volatility.
   */
  @Override
  public Double getVolatility(final Double time) {
    ArgumentChecker.notNull(time, "t");
    return _volatility.getYValue(time);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _volatility.hashCode();
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
    final BlackForexTermStructureParameters other = (BlackForexTermStructureParameters) obj;
    if (!ObjectUtils.equals(_volatility, other._volatility)) {
      return false;
    }
    return true;
  }

}
