/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import com.opengamma.util.tuple.Pair;

/**
 * Defines a constant surface (i.e. a surface with <i>z = constant</i>).
 */
public class ConstantDoublesSurface extends Surface<Double, Double, Double> {

  /**
   * @param z Level of the surface
   * @return A constant surface with automatically-generated name
   */
  public static ConstantDoublesSurface from(final double z) {
    return new ConstantDoublesSurface(z);
  }

  /**
   * @param z Level of the surface
   * @param name Name of the surface
   * @return A constant surface
   */
  public static ConstantDoublesSurface from(final double z, final String name) {
    return new ConstantDoublesSurface(z, name);
  }

  private final double _z;
  private Double[] _zArray;

  /**
   * @param z The level of the surface
   */
  public ConstantDoublesSurface(final double z) {
    super();
    _z = z;
  }

  /**
   * @param z The level of the surface
   * @param name The name of the surface
   */
  public ConstantDoublesSurface(final double z, final String name) {
    super(name);
    _z = z;
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public Double[] getXData() {
    throw new UnsupportedOperationException("Cannot get x data for constant surface");
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public Double[] getYData() {
    throw new UnsupportedOperationException("Cannot get y data for constant surface");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double[] getZData() {
    if (_zArray != null) {
      return _zArray;
    }
    _zArray = new Double[] {_z};
    return _zArray;
  }

  /**
   * @return The size of the surface (= 1)
   */
  @Override
  public int size() {
    return 1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double getZValue(final Double x, final Double y) {
    return _z;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double getZValue(final Pair<Double, Double> xy) {
    return _z;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_z);
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
    final ConstantDoublesSurface other = (ConstantDoublesSurface) obj;
    return Double.doubleToLongBits(_z) == Double.doubleToLongBits(other._z);
  }
}
