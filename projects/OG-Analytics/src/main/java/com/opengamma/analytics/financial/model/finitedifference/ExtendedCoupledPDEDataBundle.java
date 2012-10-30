/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.Surface;

/**
 * 
 */
public class ExtendedCoupledPDEDataBundle extends ExtendedConvectionDiffusionPDEDataBundle {

  private final double _lambda;

  /**
   * @param a a
   * @param b b
   * @param c c
   * @param alpha alpha
   * @param beta beta
   * @param lambda lambda
   * @param initialCondition initial condition
   */
  public ExtendedCoupledPDEDataBundle(final Surface<Double, Double, Double> a, final Surface<Double, Double, Double> b, final Surface<Double, Double, Double> c,
      final Surface<Double, Double, Double> alpha, final Surface<Double, Double, Double> beta, final double lambda, final Function1D<Double, Double> initialCondition) {
    super(a, b, c, alpha, beta, initialCondition);
    _lambda = lambda;
  }

  public double getCoupling() {
    return _lambda;
  }

}
