/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import com.opengamma.math.surface.Surface;

/**
 * 
 */
public class LocalVolatilitySurfaceStrike extends LocalVolatilitySurface<Strike> {

  /**
   * @param surface  The time to maturity should be the first coordinate and the strike the second
   */
  public LocalVolatilitySurfaceStrike(Surface<Double, Double, Double> surface) {
    super(surface);
  }

  @Override
  public double getVolatility(double t, double k) {
    Strike s = new Strike(k);
    return getVolatility(t, s);
  }

}
