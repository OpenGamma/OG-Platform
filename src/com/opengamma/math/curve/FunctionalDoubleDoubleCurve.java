/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class FunctionalDoubleDoubleCurve extends Curve<Double, Double> {

  public static FunctionalDoubleDoubleCurve from(final Function1D<Double, Double> function) {
    return new FunctionalDoubleDoubleCurve(function);
  }

  public static FunctionalDoubleDoubleCurve from(final Function1D<Double, Double> function, final String name) {
    return new FunctionalDoubleDoubleCurve(function, name);
  }

  private final Function1D<Double, Double> _function;

  public FunctionalDoubleDoubleCurve(final Function1D<Double, Double> function) {
    super();
    Validate.notNull(function, "function");
    _function = function;
  }

  public FunctionalDoubleDoubleCurve(final Function1D<Double, Double> function, final String name) {
    super(name);
    Validate.notNull(function, "function");
    _function = function;
  }

  @Override
  public Double[] getXData() {
    throw new UnsupportedOperationException("Cannot get x data - this curve is defined by a function (x -> y)");
  }

  @Override
  public Double[] getYData() {
    throw new UnsupportedOperationException("Cannot get y data - this curve is defined by a function (x -> y)");
  }

  @Override
  public Double getYValue(final Double x) {
    Validate.notNull(x, "x");
    return _function.evaluate(x);
  }

  @Override
  public int size() {
    throw new UnsupportedOperationException("Cannot get size - this curve is defined by a function (x -> y)");
  }

  public InterpolatedDoubleDoubleCurve toInterpolatedDoubleDoubleCurve(final double[] x, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    Validate.notNull(x, "x");
    Validate.notNull(interpolator);
    final int n = x.length;
    final double[] y = new double[n];
    for (int i = 0; i < n; i++) {
      y[i] = _function.evaluate(x[i]);
    }
    return InterpolatedDoubleDoubleCurve.from(x, y, interpolator);
  }

  public InterpolatedDoubleDoubleCurve toInterpolatedDoubleDoubleCurve(final double[] x, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators) {
    Validate.notNull(x, "x");
    Validate.notNull(interpolators);
    final int n = x.length;
    final double[] y = new double[n];
    for (int i = 0; i < n; i++) {
      y[i] = _function.evaluate(x[i]);
    }
    return InterpolatedDoubleDoubleCurve.from(x, y, interpolators);
  }

  public NodalDoubleDoubleCurve toNodalDoubleDoubleCurve(final double[] x) {
    Validate.notNull(x, "x");
    final int n = x.length;
    final double[] y = new double[n];
    for (int i = 0; i < n; i++) {
      y[i] = _function.evaluate(x[i]);
    }
    return NodalDoubleDoubleCurve.from(x, y);
  }

  public Function1D<Double, Double> getFunction() {
    return _function;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_function == null) ? 0 : _function.hashCode());
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
    final FunctionalDoubleDoubleCurve other = (FunctionalDoubleDoubleCurve) obj;
    return ObjectUtils.equals(_function, other._function);
  }

}
