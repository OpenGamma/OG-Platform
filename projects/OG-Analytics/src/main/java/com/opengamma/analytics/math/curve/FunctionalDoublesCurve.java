/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.differentiation.ScalarFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * A curve that is defined by a function (i.e. <i>y = f(x)</i>, where <i>f(x)</i> is supplied)
 */
public class FunctionalDoublesCurve extends DoublesCurve {

  private static final ScalarFirstOrderDifferentiator DIFF = new ScalarFirstOrderDifferentiator();

  /**
   * 
   * @param function The function that defines the curve, not null
   * @return A functional curve with an automatically-generated name
   */
  public static FunctionalDoublesCurve from(final Function1D<Double, Double> function) {
    return new FunctionalDoublesCurve(function);
  }

  /**
   * 
   * @param function The function that defines the curve, not null
   * @param derivative The first derivative for the function, not null
   * @return A functional curve with an automatically-generated name
   */
  public static FunctionalDoublesCurve from(final Function1D<Double, Double> function, final Function1D<Double, Double> derivative) {
    return new FunctionalDoublesCurve(function, derivative);
  }

  /**
   * 
   * @param function The function that defines the curve, not null
   * @param name Name of the curve 
   * @return A functional curve
   */
  public static FunctionalDoublesCurve from(final Function1D<Double, Double> function, final String name) {
    return new FunctionalDoublesCurve(function, name);
  }

  /**
   * 
   * @param function The function that defines the curve, not null
   * @param derivative The first derivative for the function, not null
   * @param name Name of the curve 
   * @return A functional curve
   */
  public static FunctionalDoublesCurve from(final Function1D<Double, Double> function, final Function1D<Double, Double> derivative, final String name) {
    return new FunctionalDoublesCurve(function, derivative, name);
  }

  private final Function1D<Double, Double> _function;
  private final Function1D<Double, Double> _derivative;

  /**
   * 
   * @param function The function that defines the curve, not null
   */
  public FunctionalDoublesCurve(final Function1D<Double, Double> function) {
    super();
    Validate.notNull(function, "function");
    _function = function;
    _derivative = DIFF.differentiate(_function);
  }

  /**
   * 
   * @param function The function that defines the curve, not null
   * @param derivative The first derivative for the function, not null
   */
  private FunctionalDoublesCurve(final Function1D<Double, Double> function, final Function1D<Double, Double> derivative) {
    super();
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.notNull(derivative, "derivative");
    _function = function;
    _derivative = derivative;
  }

  /**
   * 
   * @param function The function that defines the curve, not null
   * @param name The name of the curve
   */
  public FunctionalDoublesCurve(final Function1D<Double, Double> function, final String name) {
    super(name);
    Validate.notNull(function, "function");
    _function = function;
    _derivative = DIFF.differentiate(_function);
  }

  /**
   * 
   * @param function The function that defines the curve, not null
   *   * @param derivative The first derivative for the function, not null
   * @param name The name of the curve
   */
  private FunctionalDoublesCurve(final Function1D<Double, Double> function, final Function1D<Double, Double> derivative, final String name) {
    super(name);
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.notNull(derivative, "derivative");
    _function = function;
    _derivative = derivative;
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

  @Override
  public double getDyDx(final double x) {
    return _derivative.evaluate(x);
  }

  @Override
  public Double[] getYValueParameterSensitivity(final Double x) {
    throw new UnsupportedOperationException("Parameter sensitivity not supported yet for FunctionalDoublesCurve");
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
  public InterpolatedDoublesCurve toInterpolatedDoublesCurve(final double[] x, final Interpolator1D interpolator) {
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
   * @return The function
   */
  public Function1D<Double, Double> getFunction() {
    return _function;
  }

  /**
   * 
   * @return The function
   */
  public Function1D<Double, Double> getFirstDerivativeFunction() {
    return _derivative;
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
