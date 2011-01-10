/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class MultiquadraticRadialBasisFunction extends Function1D<Double, Double> {
  private final double _scaleFactor;

  public MultiquadraticRadialBasisFunction() {
    _scaleFactor = 1;
  }

  public MultiquadraticRadialBasisFunction(final double scaleFactor) {
    _scaleFactor = scaleFactor * scaleFactor;
  }

  @Override
  public Double evaluate(final Double x) {
    return Math.sqrt(x * x + _scaleFactor);
  }
}
