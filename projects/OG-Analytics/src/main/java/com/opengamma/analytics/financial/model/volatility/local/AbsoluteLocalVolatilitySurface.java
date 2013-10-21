/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  A surface with gives the absolute local volatility (i.e. for a SDE dx = sigma(t,x)dw) as a function of time to maturity and value of the
 *  underlying
 */
public class AbsoluteLocalVolatilitySurface extends VolatilitySurface {

  /**
   * @param surface The time to maturity should be the first coordinate and the strike the second
   */
  public AbsoluteLocalVolatilitySurface(final Surface<Double, Double, Double> surface) {
    super(surface);
  }

  /**
   * 
   * @param t time to maturity
   * @param s value of the underlying
   * @return The Dupire local volatility
   */
  @Override
  public double getVolatility(final double t, final double s) {
    final DoublesPair temp = DoublesPair.of(t, s);
    return getVolatility(temp);
  }

}
