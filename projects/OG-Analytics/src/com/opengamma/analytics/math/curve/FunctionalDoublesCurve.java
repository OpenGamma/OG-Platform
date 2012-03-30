/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.interpolation.Interpolator1D;

/**
 * A curve that is defined by a function (i.e. <i>y = f(x)</i>, where <i>f(x)</i> is supplied)
 */
public class FunctionalDoublesCurve extends Curve<Double, Double> {

  /**
   * 
   * @param function The function that defines the curve, not null
   * @return A functional curve with an automatically-generated name
   */
  public static FunctionalDoublesCurve from(final Function<Double, Double> function) {
    return new FunctionalDoublesCurve(function);
  }

  /**
   * 
   * @param function The function that defines the curve, not null
   * @param name Name of the curve 
   * @return A functional curve
   */
  public static FunctionalDoublesCurve from(final Function<Double, Double> function, final String name) {
    return new FunctionalDoublesCurve(function, name);
  }

  private final Function<Double, Double> _function;

  /**
   * 
   * @param function The function that defines the curve, not null
   */
  public FunctionalDoublesCurve(final Function<Double, Double> function) {
    super();
    Validate.notNull(function, "function");
    _function = function;
  }

  /**
   * 
   * @param function The function that defines the curve, not null
   * @param name The name of the curve
   */
  public FunctionalDoublesCurve(final Function<Double, Double> function, final String name) {
    super(name);
    Validate.notNull(function, "function");
    _function = function;
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public Double[] getXData() {
    throw new UnsupportedOperationException("Cannot get x data - this curve is defined by a function (x -> y)");
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public Double[] getYData() {
    throw new UnsupportedOperationException("Cannot get y data - this curve is defined by a function (x -> y)");
  }

  @Override
  public Double getYValue(final Double x) {
    Validate.notNull(x, "x");
    return _function.evaluate(x);
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public int size() {
    throw new UnsupportedOperationException("Cannot get size - this curve is defined by a function (x -> y)");
  }

  /**
   * 
   * @param x An array of <i>x</i> values
   * @param interpolator An interpolator
   * @return An interpolated curve with values <i>(x, f(x))</i>
   */
  public InterpolatedDoublesCurve toInterpolatedDoubleDoubleCurve(final double[] x, final Interpolator1D interpolator) {
    Validate.notNull(x, "x");
    Validate.notNull(interpolator);
    final int n = x.length;
    final double[] y = new double[n];
    for (int i = 0; i < n; i++) {
      y[i] = _function.evaluate(x[i]);
    }
    return InterpolatedDoublesCurve.from(x, y, interpolator);
  }

  /**
   * 
   * @param x An array of x values
   * @return A nodal curve with values <i>(x, f(x))</i>
   */
  public NodalDoublesCurve toNodalDoubleDoubleCurve(final double[] x) {
    Validate.notNull(x, "x");
    final int n = x.length;
    final double[] y = new double[n];
    for (int i = 0; i < n; i++) {
      y[i] = _function.evaluate(x[i]);
    }
    return NodalDoublesCurve.from(x, y);
  }

  /**
   * 
   * @return The function
   */
  public Function<Double, Double> getFunction() {
    return _function;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _function.hashCode();
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
    final FunctionalDoublesCurve other = (FunctionalDoublesCurve) obj;
    return ObjectUtils.equals(_function, other._function);
  }

}
