/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import com.opengamma.analytics.math.surface.Surface;

/**
 * 
 */
public class PureImpliedVolatilitySurface extends VolatilitySurface {

  /**
   * @param surface The volatility surface
   */
  public PureImpliedVolatilitySurface(final Surface<Double, Double, Double> surface) {
    super(surface);
  }

}
