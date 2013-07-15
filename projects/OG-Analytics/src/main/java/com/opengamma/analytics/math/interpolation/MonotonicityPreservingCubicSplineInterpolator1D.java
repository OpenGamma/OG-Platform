/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

/**
 * 
 */
public class MonotonicityPreservingCubicSplineInterpolator1D extends PiecewisePolynomialInterpolator1D {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * If the primary interpolation method is not specified, the cubic spline interpolation with natrual endpoint conditions is used
   */
  public MonotonicityPreservingCubicSplineInterpolator1D() {
    super(new MonotonicityPreservingCubicSplineInterpolator(new NaturalSplineInterpolator()));
  }

  /**
   * @param method Primary interpolation method. The first derivative values are modified according to the monotonicity conditions
   */
  public MonotonicityPreservingCubicSplineInterpolator1D(final PiecewisePolynomialInterpolator method) {
    super(new MonotonicityPreservingCubicSplineInterpolator(method));
  }
}
