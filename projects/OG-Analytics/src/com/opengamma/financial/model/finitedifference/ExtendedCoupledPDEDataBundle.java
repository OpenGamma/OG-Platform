/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.surface.Surface;

/**
 * 
 */
public class ExtendedCoupledPDEDataBundle extends ExtendedConvectionDiffusionPDEDataBundle {

  private final double _lambda;

  /**
   * @param a
   * @param b
   * @param c
   * @param initialCondition
   */
  public ExtendedCoupledPDEDataBundle(Surface<Double, Double, Double> a, Surface<Double, Double, Double> b,
      Surface<Double, Double, Double> c, Surface<Double, Double, Double> alpha, Surface<Double, Double, Double> beta, double lambda, Function1D<Double, Double> initialCondition) {
    super(a, b, c, alpha, beta, initialCondition);
    _lambda = lambda;
  }

  public double getCoupling() {
    return _lambda;
  }

}
