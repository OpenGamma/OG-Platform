/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * A surface with gives the price of a European call as a function of time to maturity and strike
 */
public class PriceSurface {

  private final Surface<Double, Double, Double> _surface;

  /**
   * 
   * @param surface  The time to maturity should be the first coordinate and the strike the second 
   */
  public PriceSurface(final Surface<Double, Double, Double> surface) {
    Validate.notNull(surface, "surface");
    _surface = surface;
  }

  /**
   * 
   * @param t time to maturity
   * @param k strike
   * @return The price of a European call
   */
  public Double getPrice(final double t, final double k) {
    DoublesPair pair = DoublesPair.of(t, k);
    return _surface.getZValue(pair);
  }

  public Surface<Double, Double, Double> getSurface() {
    return _surface;
  }

}
