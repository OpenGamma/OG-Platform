/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.math.surface.Surface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class PriceSurface {

  private final Surface<Double, Double, Double> _surface;

  public PriceSurface(final Surface<Double, Double, Double> surface) {
    Validate.notNull(surface, "surface");
    _surface = surface;
  }

  public Double getPrice(final double t, final double k) {
    DoublesPair pair = new DoublesPair(t, k);
    return _surface.getZValue(pair);
  }

  public Surface<Double, Double, Double> getSurface() {
    return _surface;
  }

}
