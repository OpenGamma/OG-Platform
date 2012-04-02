/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.interpolation.Interpolator1D;

/**
 * Defines a constant curve (i.e. a curve with <i>y = constant</i>)
 */
public class ConstantDoublesCurve extends Curve<Double, Double> {

  /**
   * @param y Level of the curve
   * @return A constant curve with automatically-generated name
   */
  public static ConstantDoublesCurve from(final double y) {
    return new ConstantDoublesCurve(y);
  }

  /**
   * 
   * @param y Level of the curve
   * @param name Name of the curve
   * @return A constant curve
   */
  public static ConstantDoublesCurve from(final double y, final String name) {
    return new ConstantDoublesCurve(y, name);
  }

  private final double _y;

  /**
   * 
   * @param y The level of the curve
   */
  public ConstantDoublesCurve(final double y) {
    super();
    _y = y;
  }

  /**
   * 
   * @param y The level of the curve
   * @param name The name of the curve
   */
  public ConstantDoublesCurve(final double y, final String name) {
    super(name);
    _y = y;
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public Double[] getXData() {
    throw new UnsupportedOperationException("Cannot get x data for constant curve");
  }

  /**
   * @return An array containing one element (the level)
   */
  @Override
  public Double[] getYData() {
    return new Double[] {_y};
  }

  /**
   * @param x The value
   * @return The level
   */
  @Override
  public Double getYValue(final Double x) {
    return _y;
  }

  /**
   * 
   * @param x An array of <i>x</i> values, not null
   * @param interpolator An interpolator, not null
   * @return An interpolated curve with constant value 
   */
  public InterpolatedDoublesCurve toInterpolatedDoublesCurve(final double[] x, final Interpolator1D interpolator) {
    Validate.notNull(x, "x");
    Validate.notNull(interpolator, "interpolator");
    final double[] y = new double[x.length];
    Arrays.fill(y, _y);
    return InterpolatedDoublesCurve.from(x, y, interpolator);
  }

  /**
   * 
   * @param x An array of <i>x</i> values, not null
   * @return A nodal curve with constant value
   */
  public NodalDoublesCurve toNodalDoublesCurve(final double[] x) {
    Validate.notNull(x, "x");
    final double[] y = new double[x.length];
    Arrays.fill(y, _y);
    return NodalDoublesCurve.from(x, y);
  }

  /**
   * @return The size of the curve is one
   */
  @Override
  public int size() {
    return 1;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_y);
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
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ConstantDoublesCurve other = (ConstantDoublesCurve) obj;
    return Double.doubleToLongBits(_y) == Double.doubleToLongBits(other._y);
  }

  @Override
  public String toString() {
    return "ConstantDoublesCurve[name=" + getName() + ", y=" + _y + "]";
  }
}
