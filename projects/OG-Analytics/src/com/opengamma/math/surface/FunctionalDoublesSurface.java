/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class FunctionalDoublesSurface extends Surface<Double, Double, Double> {

  public static FunctionalDoublesSurface from(final Function<DoublesPair, Double> function) {
    return new FunctionalDoublesSurface(function);
  }

  public static FunctionalDoublesSurface from(final Function<DoublesPair, Double> function, final String name) {
    return new FunctionalDoublesSurface(function, name);
  }

  private final Function<DoublesPair, Double> _function;

  public FunctionalDoublesSurface(final Function<DoublesPair, Double> function) {
    super();
    Validate.notNull(function, "function");
    _function = function;
  }

  public FunctionalDoublesSurface(final Function<DoublesPair, Double> function, final String name) {
    super(name);
    Validate.notNull(function, "function");
    _function = function;
  }

  @Override
  public Double[] getXData() {
    throw new UnsupportedOperationException("Cannot get x data - this surface is defined by a function");
  }

  @Override
  public Double[] getYData() {
    throw new UnsupportedOperationException("Cannot get y data - this surface is defined by a function");
  }

  @Override
  public Double[] getZData() {
    throw new UnsupportedOperationException("Cannot get z data - this surface is defined by a function");
  }

  @Override
  public int size() {
    throw new UnsupportedOperationException("Cannot get size - this surface is defined by a function");
  }

  @Override
  public Double getZValue(final Double x, final Double y) {
    Validate.notNull(x, "x");
    Validate.notNull(y, "y");
    return _function.evaluate(DoublesPair.of(x.doubleValue(), y.doubleValue()));
  }

  @Override
  public Double getZValue(final Pair<Double, Double> xy) {
    Validate.notNull(xy, "x-y pair");
    return _function.evaluate(DoublesPair.of(xy));
  }

  public Function<DoublesPair, Double> getFunction() {
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
    final FunctionalDoublesSurface other = (FunctionalDoublesSurface) obj;
    return ObjectUtils.equals(_function, other._function);
  }

}
