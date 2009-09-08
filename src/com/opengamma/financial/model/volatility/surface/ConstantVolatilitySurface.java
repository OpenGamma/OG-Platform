package com.opengamma.financial.model.volatility.surface;

/**
 * 
 * @author emcleod
 * 
 */
public class ConstantVolatilitySurface extends VolatilitySurface {
  private final double _sigma;

  public ConstantVolatilitySurface(double sigma) {
    _sigma = sigma;
  }

  @Override
  public double getVolatility(Double x, Double y) {
    return _sigma;
  }
}
