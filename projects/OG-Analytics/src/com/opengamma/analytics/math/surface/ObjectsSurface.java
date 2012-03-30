/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * Parent class for a family of surfaces that have <i>x</i>, <i>y</i> and <i>z</i> values of any type.
 * @param <T> Type of the x-axis values
 * @param <U> Type of the y-axis values
 * @param <V> Type of the z-axis values 
 */
public abstract class ObjectsSurface<T, U, V> extends Surface<T, U, V> {
  private final int _n;
  private final T[] _xData;
  private final U[] _yData;
  private final V[] _zData;

  /**
   * @param xData An array of <i>x</i> data, not null, no null elements.
   * @param yData An array of <i>y</i> data, not null, no null elements. Must be the same length as the <i>x</i> data.
   * @param zData An array of <i>z</i> data, not null, no null elements. Must be the same length as the <i>x</i> data.
   */
  public ObjectsSurface(final T[] xData, final U[] yData, final V[] zData) {
    super();
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    Validate.isTrue(xData.length == yData.length);
    Validate.isTrue(xData.length == zData.length);
    _n = xData.length;
    _xData = Arrays.copyOf(xData, _n);
    _yData = Arrays.copyOf(yData, _n);
    _zData = Arrays.copyOf(zData, _n);
  }

  /**
   * @param xData An array of <i>x</i> data, not null
   * @param yData An array of <i>y</i> data, not null
   * @param zData An array of <i>z</i> data, not null
   * @param name The surface name
   */
  public ObjectsSurface(final T[] xData, final U[] yData, final V[] zData, final String name) {
    super(name);
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    Validate.isTrue(xData.length == yData.length);
    Validate.isTrue(xData.length == zData.length);
    _n = xData.length;
    _xData = Arrays.copyOf(xData, _n);
    _yData = Arrays.copyOf(yData, _n);
    _zData = Arrays.copyOf(zData, _n);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T[] getXData() {
    return _xData;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public U[] getYData() {
    return _yData;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public V[] getZData() {
    return _zData;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size() {
    return _n;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_xData);
    result = prime * result + Arrays.hashCode(_yData);
    result = prime * result + Arrays.hashCode(_zData);
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
    final ObjectsSurface<?, ?, ?> other = (ObjectsSurface<?, ?, ?>) obj;
    if (!Arrays.equals(_xData, other._xData)) {
      return false;
    }
    if (!Arrays.equals(_yData, other._yData)) {
      return false;
    }
    if (!Arrays.equals(_zData, other._zData)) {
      return false;
    }
    return true;
  }

}
