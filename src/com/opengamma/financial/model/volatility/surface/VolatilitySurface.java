/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import com.opengamma.financial.model.volatility.VolatilityModel;
import com.opengamma.plot.RenderVisitor;
import com.opengamma.plot.Renderable;

/**
 * 
 * @author emcleod
 */
public abstract class VolatilitySurface implements VolatilityModel<Double, Double>, Renderable {

  @Override
  public abstract Double getVolatility(Double x, Double y);

  @Override
  public <T> T accept(final RenderVisitor<T> visitor) {
    return visitor.visitVolatilitySurface(this);
  }
}
