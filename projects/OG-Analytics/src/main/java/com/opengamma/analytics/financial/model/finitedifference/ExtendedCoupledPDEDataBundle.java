/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.Surface;

/**
 * Not part of the new hierarchy
 */
@Deprecated
public class ExtendedCoupledPDEDataBundle {

  private final Surface<Double, Double, Double> _a;
  private final Surface<Double, Double, Double> _b;
  private final Surface<Double, Double, Double> _c;
  private final Surface<Double, Double, Double> _alpha;
  private final Surface<Double, Double, Double> _beta;
  private final Function1D<Double, Double> _initialCondition;
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

    _a = a;
    _b = b;
    _c = c;
    _alpha = alpha;
    _beta = beta;
    _initialCondition = initialCondition;
    _lambda = lambda;
  }

  public double getCoupling() {
    return _lambda;
  }

  /**
   * Gets the a.
   * @return the a
   */
  public double getA(final double t, final double x) {
    return _a.getZValue(t, x);
  }

  /**
   * Gets the b.
   * @return the b
   */
  public double getB(final double t, final double x) {
    return _b.getZValue(t, x);
  }

  /**
   * Gets the c.
   * @return the c
   */
  public double getC(final double t, final double x) {
    return _c.getZValue(t, x);
  }

  /**
   * Gets the alpha.
   * @return the alpha
   */
  public double getAlpha(final double t, final double x) {
    return _alpha.getZValue(t, x);
  }

  /**
   * Gets the beta.
   * @return the beta
   */
  public double getBeta(final double t, final double x) {
    return _beta.getZValue(t, x);
  }

  /**
   * Gets the initialCondition.
   * @return the initialCondition
   */
  public double getInitialCondition(final double x) {
    return _initialCondition.evaluate(x);
  }

}
