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
 * 
 */
public class ConstantDoubleDoubleCurve extends Curve<Double, Double> {

  public static ConstantDoubleDoubleCurve of(final double y) {
    return new ConstantDoubleDoubleCurve(y);
  }

  public static ConstantDoubleDoubleCurve of(final double y, final String name) {
    return new ConstantDoubleDoubleCurve(y, name);
  }

  private final double _y;

  public ConstantDoubleDoubleCurve(final double y) {
    super();
    _y = y;
  }

  public ConstantDoubleDoubleCurve(final double y, final String name) {
    super(name);
    _y = y;
  }

  @Override
  public Double[] getXData() {
    throw new UnsupportedOperationException("Cannot get x data for constant curve");
  }

  @Override
  public Double[] getYData() {
    return new Double[] {_y};
  }

  @Override
  public Double getYValue(final Double x) {
    return _y;
  }

  public InterpolatedDoubleDoubleCurve toInterpolatedDoubleDoubleCurve(double[] x, Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    double[] y = new double[x.length];
    Arrays.fill(y, _y);
    return InterpolatedDoubleDoubleCurve.of(x, y, interpolator);
  }

  public InterpolatedDoubleDoubleCurve toInterpolatedDoubleDoubleCurve(double[] x, Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators) {
    double[] y = new double[x.length];
    Arrays.fill(y, _y);
    return InterpolatedDoubleDoubleCurve.of(x, y, interpolators);
  }

  public NodalDoubleDoubleCurve toNodalDoubleDoubleCurve(double[] x) {
    double[] y = new double[x.length];
    Arrays.fill(y, _y);
    return NodalDoubleDoubleCurve.of(x, y);
  }

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
