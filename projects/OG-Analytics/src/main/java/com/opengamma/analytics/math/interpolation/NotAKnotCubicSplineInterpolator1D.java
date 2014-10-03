/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * Cubic spline interpolator with not-a-knot endpoint conditions, i.e., second derivative value is set to be the same as the next knot
 */
public class NotAKnotCubicSplineInterpolator1D extends PiecewisePolynomialInterpolator1D {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Default constructor where the interpolation method is fixed
   */
  public NotAKnotCubicSplineInterpolator1D() {
    super(new CubicSplineInterpolator());
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y, final double leftCond, final double rightCond) {
    throw new IllegalArgumentException("No degrees of freedom at endpoints for this interpolation method");
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y, final double leftCond, final double rightCond) {
    throw new IllegalArgumentException("No degrees of freedom at endpoints for this interpolation method");
  }
}
