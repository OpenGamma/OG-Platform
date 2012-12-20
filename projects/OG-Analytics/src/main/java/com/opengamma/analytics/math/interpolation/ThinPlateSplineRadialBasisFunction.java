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
public class ThinPlateSplineRadialBasisFunction extends Function1D<Double, Double> {
  private final double _scaleFactor;

  public ThinPlateSplineRadialBasisFunction() {
    _scaleFactor = 1;
  }

  public ThinPlateSplineRadialBasisFunction(final double scaleFactor) {
    if (scaleFactor <= 0) {
      throw new IllegalArgumentException("Scale factor must be greater than zero");
    }
    _scaleFactor = scaleFactor;
  }

  @Override
  public Double evaluate(final Double x) {
    if (x == 0.0) {
      return 0.0;
    }
    return x * x * Math.log(x / _scaleFactor);
  }

}
