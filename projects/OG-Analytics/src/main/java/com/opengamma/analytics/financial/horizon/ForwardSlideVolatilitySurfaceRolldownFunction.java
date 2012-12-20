/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;

/**
 * 
 */
public class ForwardSlideVolatilitySurfaceRolldownFunction implements RolldownFunction<VolatilitySurface> {

  @Override
  public VolatilitySurface rollDown(final VolatilitySurface volatilitySurface, final double time) {
    return volatilitySurface;
  }

}
