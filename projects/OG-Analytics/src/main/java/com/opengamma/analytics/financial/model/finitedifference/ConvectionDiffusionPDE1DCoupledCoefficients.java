/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import com.opengamma.analytics.math.surface.Surface;

/**
 * 
 */
public class ConvectionDiffusionPDE1DCoupledCoefficients extends ConvectionDiffusionPDE1DStandardCoefficients {

  private final double _lambda;

  /**
   * @param a a
   * @param b b
   * @param c c
   * @param lambda the coupling to other system
   */
  public ConvectionDiffusionPDE1DCoupledCoefficients(final Surface<Double, Double, Double> a, final Surface<Double, Double, Double> b, final Surface<Double, Double, Double> c,
      final double lambda) {
    super(a, b, c);
    _lambda = lambda;
  }

  public double getLambda() {
    return _lambda;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_lambda);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof ConvectionDiffusionPDE1DCoupledCoefficients)) {
      return false;
    }
    final ConvectionDiffusionPDE1DCoupledCoefficients other = (ConvectionDiffusionPDE1DCoupledCoefficients) obj;
    if (Double.compare(_lambda, other._lambda) != 0) {
      return false;
    }
    return true;
  }


}
