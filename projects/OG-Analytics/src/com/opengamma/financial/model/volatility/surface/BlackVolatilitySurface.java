/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import com.opengamma.math.surface.Surface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * A surface with gives the Black (implied) volatility  as a function of time to maturity and strike
 */
public class BlackVolatilitySurface extends VolatilitySurface {

  /**
   * 
   * @param surface The time to maturity should be the first coordinate and the strike the second 
   */
  public BlackVolatilitySurface(Surface<Double, Double, Double> surface) {
    super(surface);
  }

  /**
   * 
   * @param t time to maturity
   * @param k strike
   * @return The Black (implied) volatility 
   */
  public double getVolatility(final double t, final double k) {
    DoublesPair temp = new DoublesPair(t, k);
    return getVolatility(temp);
  }

}
