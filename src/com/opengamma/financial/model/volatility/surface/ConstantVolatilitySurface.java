/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

/**
 * 
 * A VolatilitySurface that has a constant volatility for all values of x and y
 * 
 * @author emcleod
 */
public class ConstantVolatilitySurface extends VolatilitySurface {
  private final double _sigma;

  public ConstantVolatilitySurface(final Double sigma) {
    _sigma = sigma;
  }

  @Override
  public Double getVolatility(final Double x, final Double y) {
    return _sigma;
  }
}
