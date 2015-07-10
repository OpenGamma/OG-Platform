/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import com.opengamma.util.tuple.Triple;

/**
 * Defines a constant cube (i.e. a cube with <i>z = constant</i>).
 */
public class ConstantDoublesCube extends Cube<Double, Double, Double, Double> {

  /**
   * @param v Level (in fourth dimention) of the cube
   * @return A constant cube with automatically-generated name
   */
  public static ConstantDoublesCube from(final double v) {
    return new ConstantDoublesCube(v);
  }

  /**
   * @param v Level (in fourth dimention) of the cube
   * @param name Name of the cube
   * @return A constant cube
   */
  public static ConstantDoublesCube from(final double v, final String name) {
    return new ConstantDoublesCube(v, name);
  }

  private final double _v;
  private Double[] _vArray;

  /**
   * @param v The level of the cube
   */
  public ConstantDoublesCube(final double v) {
    super();
    _v = v;
  }

  /**
   * @param v The level of the cube
   * @param name The name of the cube
   */
  public ConstantDoublesCube(final double v, final String name) {
    super(name);
    _v = v;
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public Double[] getXData() {
    throw new UnsupportedOperationException("Cannot get x data for constant cube");
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public Double[] getYData() {
    throw new UnsupportedOperationException("Cannot get y data for constant cube");
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public Double[] getZData() {
    throw new UnsupportedOperationException("Cannot get z data for constant cube");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double[] getValues() {
    if (_vArray != null) {
      return _vArray;
    }
    _vArray = new Double[] {_v };
    return _vArray;
  }

  /**
   * @return The size of the cube (= 1)
   */
  @Override
  public int size() {
    return 1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double getValue(final Double x, final Double y, final Double z) {
    return _v;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double getValue(final Triple<Double, Double, Double> xyz) {
    return _v;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_v);
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
    final ConstantDoublesCube other = (ConstantDoublesCube) obj;
    return Double.doubleToLongBits(_v) == Double.doubleToLongBits(other._v);
  }
}
