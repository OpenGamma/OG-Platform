/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.volatility.surface.Strike;
import com.opengamma.analytics.math.surface.Surface;

/**
 * 
 */
public class LocalVolatilitySurfaceStrike extends LocalVolatilitySurface<Strike> {

  /**
   * @param surface  The time to maturity should be the first coordinate and the strike the second
   */
  public LocalVolatilitySurfaceStrike(final Surface<Double, Double, Double> surface) {
    super(surface);
  }

  @Override
  public double getVolatility(final double t, final double k) {
    final Strike s = new Strike(k);
    return getVolatility(t, s);
  }

}
