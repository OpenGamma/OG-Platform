/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.function.Function1D;

/**
 * 
 */
public class ShepardNormalizedRadialBasisFunction extends Function1D<Double, Double> {
  private final double _power;

  public ShepardNormalizedRadialBasisFunction(final double power) {
    _power = power;
  }

  @Override
  public Double evaluate(final Double x) {
    return Math.pow(x, -_power);
  }

}
