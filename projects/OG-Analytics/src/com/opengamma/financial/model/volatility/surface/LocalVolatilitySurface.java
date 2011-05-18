/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import com.opengamma.math.surface.Surface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class LocalVolatilitySurface extends VolatilitySurface {

  /**
   * @param surface
   */
  public LocalVolatilitySurface(Surface<Double, Double, Double> surface) {
    super(surface);
  }

  public double getVolatility(final double t, final double s) {
    DoublesPair temp = new DoublesPair(t, s);
    return getVolatility(temp);
  }

}
