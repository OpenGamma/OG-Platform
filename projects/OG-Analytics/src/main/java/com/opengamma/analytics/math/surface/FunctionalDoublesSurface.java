/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.util.tuple.Pair;

/**
 * A surface that is defined by a function <i>z = f(x, y)</i>.
 */
public class FunctionalDoublesSurface extends Surface<Double, Double, Double> {

  /**
   * @param function The function that defines the surface, not null
   * @return A functional surface with automatically-generated name
   */
  public static FunctionalDoublesSurface from(final Function<Double, Double> function) {
    return new FunctionalDoublesSurface(function);
  }

  /**
   * @param function The function that defines the surface, not null
   * @param name The name of the surface
   * @return A functional surface
   */
  public static FunctionalDoublesSurface from(final Function<Double, Double> function, final String name) {
    return new FunctionalDoublesSurface(function, name);
  }

  private final Function<Double, Double> _function;

  /**
   * @param function The function that defines the surface, not null
   */
  public FunctionalDoublesSurface(final Function<Double, Double> function) {
    super();
    Validate.notNull(function, "function");
    _function = function;
  }

  /**
   * @param function The function that defines the surface, not null
   * @param name The name of the surface
   */
  public FunctionalDoublesSurface(final Function<Double, Double> function, final String name) {
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
    throw new UnsupportedOperationException("Cannot get x data - this surface is defined by a function");
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public Double[] getYData() {
    throw new UnsupportedOperationException("Cannot get y data - this surface is defined by a function");
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public Double[] getZData() {
    throw new UnsupportedOperationException("Cannot get z data - this surface is defined by a function");
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public int size() {
    throw new UnsupportedOperationException("Cannot get size - this surface is defined by a function");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double getZValue(final Double x, final Double y) {
    Validate.notNull(x, "x");
    Validate.notNull(y, "y");
    return _function.evaluate(x, y);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double getZValue(final Pair<Double, Double> xy) {
    Validate.notNull(xy, "x-y pair");
    return _function.evaluate(xy.getFirst(), xy.getSecond());
  }

  /**
   * @return The function that defines the surface
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
    final FunctionalDoublesSurface other = (FunctionalDoublesSurface) obj;
    return ObjectUtils.equals(_function, other._function);
  }

}
