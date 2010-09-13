/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.Arrays;
import java.util.Map;

import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * Defines a constant curve 
 */
public class ConstantDoubleDoubleCurve extends Curve<Double, Double> {

  /**
   * @param y Level of the curve
   * @return A constant curve with automatically-generated name
   */
  public static ConstantDoubleDoubleCurve from(final double y) {
    return new ConstantDoubleDoubleCurve(y);
  }

  /**
   * 
   * @param y Level of the curve
   * @param name Name of the curve
   * @return A constant curve
   */
  public static ConstantDoubleDoubleCurve from(final double y, final String name) {
    return new ConstantDoubleDoubleCurve(y, name);
  }

  private final double _y;

  /**
   * 
   * @param y The level of the curve
   */
  public ConstantDoubleDoubleCurve(final double y) {
    super();
    _y = y;
  }

  /**
   * 
   * @param y The level of the curve
   * @param name The name of the curve
   */
  public ConstantDoubleDoubleCurve(final double y, final String name) {
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
   * @param x An array of x values
   * @param interpolator An interpolator
   * @return An interpolated curve with constant value 
   */
  public InterpolatedDoubleDoubleCurve toInterpolatedDoubleDoubleCurve(final double[] x, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    final double[] y = new double[x.length];
    Arrays.fill(y, _y);
    return InterpolatedDoubleDoubleCurve.from(x, y, interpolator);
  }

  /**
   * 
   * @param x An array of x values
   * @param interpolators A map of (time valid -> interpolator)
   * @return An interpolated curve with constant value
   */
  public InterpolatedDoubleDoubleCurve toInterpolatedDoubleDoubleCurve(final double[] x, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators) {
    final double[] y = new double[x.length];
    Arrays.fill(y, _y);
    return InterpolatedDoubleDoubleCurve.from(x, y, interpolators);
  }

  /**
   * 
   * @param x An array of x values
   * @return A nodal curve with constant value
   */
  public NodalDoubleDoubleCurve toNodalDoubleDoubleCurve(final double[] x) {
    final double[] y = new double[x.length];
    Arrays.fill(y, _y);
    return NodalDoubleDoubleCurve.from(x, y);
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
    final ConstantDoubleDoubleCurve other = (ConstantDoubleDoubleCurve) obj;
    if (Double.doubleToLongBits(_y) != Double.doubleToLongBits(other._y)) {
      return false;
    }
    return true;
  }

}
