/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

/**
 * 
 */
public class NaturalSplineInterpolator1D extends PiecewisePolynomialInterpolator1D {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Default constructor where the interpolation method is fixed
   */
  public NaturalSplineInterpolator1D() {
    super(new NaturalSplineInterpolator());
  }
}
