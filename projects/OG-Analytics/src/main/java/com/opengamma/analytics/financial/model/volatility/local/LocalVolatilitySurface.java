/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.volatility.surface.StrikeType;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  A surface with gives the Dupire local volatility  as a function of time to maturity and some abstraction of strike
 *  @param <T> Parameter that describes the abstraction of strike - this could be the actual strike, the delta (most commonly used in FX), moneyness (defined as the strike/forward),
 *  the logarithm of moneyness or some other parameterisation
 */
public abstract class LocalVolatilitySurface<T extends StrikeType> extends VolatilitySurface {

  /**
   * @param surface The time to maturity should be the first coordinate and the abstraction of strike the second
   */
  public LocalVolatilitySurface(final Surface<Double, Double, Double> surface) {
    super(surface);
  }

  /**
   * Depending on the application the same local volatility surface can be seem either as either a function of calendar
   * time and value of some abstraction of the  underlying, or as a function of expiry and some abstraction of strike
   * @param t time
   * @param s value of abstraction of strike
   * @return The Dupire local volatility
   */
  public double getVolatility(final double t, final T s) {
    final DoublesPair temp = DoublesPair.of(t, s.value());
    return getVolatility(temp);
  }


  @Override
  public abstract double getVolatility(final double t, final double k);

}
