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

}
