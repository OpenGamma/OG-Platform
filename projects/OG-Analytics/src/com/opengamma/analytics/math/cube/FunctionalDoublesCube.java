/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.util.tuple.Triple;

/**
 * A cube that is defined by a function <i>value = f(x, y, z)</i>, where <i>f(x, y, z)</i> is supplied.
 */
public class FunctionalDoublesCube extends Cube<Double, Double, Double, Double> {
  
  /**
   * @param function The function that defines the cube, not null
   * @return A functional cube with an automatically-generated name
   */
  public static FunctionalDoublesCube from(final Function<Double, Double> function) {
    return new FunctionalDoublesCube(function);
  }

  /**
   * @param function The function that defines the cube, not null
   * @param name The name of the cube
   * @return A functional cube
   */
  public static FunctionalDoublesCube from(final Function<Double, Double> function, final String name) {
    return new FunctionalDoublesCube(function, name);
  }

  private final Function<Double, Double> _function;

  /**
   * @param function The function that defines the cube, not null
   */
  public FunctionalDoublesCube(final Function<Double, Double> function) {
    super();
    Validate.notNull(function, "function");
    _function = function;
  }

  /**
   * @param function The function that defines the cube, not null
   * @param name The name of the cube
   */
  public FunctionalDoublesCube(final Function<Double, Double> function, final String name) {
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
    throw new UnsupportedOperationException("Cannot get x data - this cube is defined by a function");
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public Double[] getYData() {
    throw new UnsupportedOperationException("Cannot get y data - this cube is defined by a function");
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public Double[] getZData() {
    throw new UnsupportedOperationException("Cannot get z data - this cube is defined by a function");
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public Double[] getValues() {
    throw new UnsupportedOperationException("Cannot get data - this cube is defined by a function");
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public int size() {
    throw new UnsupportedOperationException("Cannot get size - this cube is defined by a function");
  }

  @Override
  public Double getValue(final Double x, final Double y, Double z) {
    Validate.notNull(x, "x");
    Validate.notNull(y, "y");
    Validate.notNull(z, "z");
    return _function.evaluate(x, y, z);
  }

  @Override
  public Double getValue(final Triple<Double, Double, Double> xyz) {
    Validate.notNull(xyz, "x-y-z data");
    return _function.evaluate(xyz.getFirst(), xyz.getSecond(), xyz.getThird());
  }

  /**
   * @return The function that defines the cube
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
    final FunctionalDoublesCube other = (FunctionalDoublesCube) obj;
    return ObjectUtils.equals(_function, other._function);
  }

}
