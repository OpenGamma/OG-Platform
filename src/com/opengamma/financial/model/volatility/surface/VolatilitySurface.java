/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import com.opengamma.financial.model.volatility.VolatilityModel;

/**
 * 
 * @author emcleod
 */
public abstract class VolatilitySurface implements VolatilityModel<Double, Double> {

  @Override
  public abstract Double getVolatility(Double x, Double y);

}
