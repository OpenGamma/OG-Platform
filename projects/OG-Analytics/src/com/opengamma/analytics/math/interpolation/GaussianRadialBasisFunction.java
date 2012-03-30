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
public class GaussianRadialBasisFunction extends Function1D<Double, Double> {
  private final double _scaleFactor;

  public GaussianRadialBasisFunction() {
    _scaleFactor = 1;
  }

  public GaussianRadialBasisFunction(final double scaleFactor) {
    _scaleFactor = scaleFactor * scaleFactor;
  }

  @Override
  public Double evaluate(final Double x) {
    return Math.exp(-0.5 * x * x / _scaleFactor);
  }

}
