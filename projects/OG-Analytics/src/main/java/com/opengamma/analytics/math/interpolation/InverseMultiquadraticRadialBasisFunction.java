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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_scaleFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final InverseMultiquadraticRadialBasisFunction other = (InverseMultiquadraticRadialBasisFunction) obj;
    return Double.doubleToLongBits(_scaleFactor) == Double.doubleToLongBits(other._scaleFactor);
  }

}
