/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class FunctionalDoubleDoubleCurve extends Curve<Double, Double> {

  public static FunctionalDoubleDoubleCurve of(final Function1D<Double, Double> function) {
    return new FunctionalDoubleDoubleCurve(function);
  }

  public static FunctionalDoubleDoubleCurve of(final Function1D<Double, Double> function, final String name) {
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
