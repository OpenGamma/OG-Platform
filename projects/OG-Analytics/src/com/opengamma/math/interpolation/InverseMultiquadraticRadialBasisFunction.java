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
public class InverseMultiquadraticRadialBasisFunction extends Function1D<Double, Double> {
  private final double _scaleFactor;

  public InverseMultiquadraticRadialBasisFunction() {
    _scaleFactor = 1;
  }

  public InverseMultiquadraticRadialBasisFunction(final double scaleFactor) {
    _scaleFactor = scaleFactor * scaleFactor;
  }

  @Override
  public Double evaluate(final Double x) {
    return 1. / Math.sqrt(x * x + _scaleFactor);
  }
}
