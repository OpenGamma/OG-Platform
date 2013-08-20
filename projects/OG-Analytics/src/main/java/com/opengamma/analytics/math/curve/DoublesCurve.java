/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

/**
 * Parent class for a family of curves that have real <i>x</i> and <i>y</i> values.
 */
public abstract class DoublesCurve extends Curve<Double, Double> {

  /**
   * Constructor
   */
  public DoublesCurve() {
    super();
  }

  /**
   * Constructor with a name.
   * @param name The curve name.
   */
  public DoublesCurve(final String name) {
    super(name);
  }

  /**
   * Computes the sensitivity of the Y value with respect to the curve parameters.
   * @param x The value at which the parameter sensitivity is computed.
   * @return The sensitivity.
   */
  public abstract Double[] getYValueParameterSensitivity(Double x);

  /**
   * Compute the first derivative of the curve, $\frac{dy}{dx}$ 
   * @param x The value at which the derivative is taken 
   * @return The first derivative 
   */
  public abstract double getDyDx(final double x);

}
