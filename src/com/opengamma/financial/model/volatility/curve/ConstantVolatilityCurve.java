/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.curve;

import java.util.Map;
import java.util.Set;

/**
 * 
 * @author emcleod
 */
public class ConstantVolatilityCurve extends VolatilityCurve {
  private final double _sigma;

  public ConstantVolatilityCurve(final double sigma) {
    _sigma = sigma;
  }

  @Override
  public Double getVolatility(final Double t) {
    return _sigma;
  }

  @Override
  public Set<Double> getXData() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public VolatilityCurve withMultipleShifts(final Map<Double, Double> shifts) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public VolatilityCurve withParallelShift(final Double shift) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public VolatilityCurve withSingleShift(final Double x, final Double shift) {
    // TODO Auto-generated method stub
    return null;
  }

}
