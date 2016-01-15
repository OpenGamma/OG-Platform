/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.tuple.Pair;

/**
 * Defines a general surface <i>(x, y, z)</i> class. The <i>x</i>, <i>y</i> and <i>z</i> data can be any type. The surfaces are named; if a name is not provided then a unique
 * ID will be used. 
 * @param <T> The type of the x data
 * @param <U> The type of the y data
 * @param <V> The type of the z data
 */
public abstract class Surface<T, U, V> implements Serializable {
  private static final AtomicLong ATOMIC = new AtomicLong();
  private final String _name;

  /**
   * Constructs a surface with an automatically-generated name
   */
  public Surface() {
    this(Long.toString(ATOMIC.getAndIncrement()));
  }

  /**
   * Constructs a surface with the given name
   * @param name The name
   */
  public Surface(final String name) {
    _name = name;
  }

  /**
   * @return The name of the curve
   */
  public String getName() {
    return _name;
  }

  /**
   * @return The <i>x</i> data for this surface
   */
  public abstract T[] getXData();

  /**
   * @return The <i>y</i> data for this surface
   */
  public abstract U[] getYData();

  /**
   * @return The <i>z</i> data for this surface
   */
  public abstract V[] getZData();

  /**
   * @return The number of data points used to construct this surface
   */
  public abstract int size();

  /**
   * Given an <i>(x, y)</i> point, return the <i>z</i> value from this surface. 
   * @param x The <i>x</i> value, not null
   * @param y The <i>y</i> value, not null
   * @return The <i>z</i> value
   */
  public abstract V getZValue(T x, U y);

  /**
   * Given an <i>(x, y)</i> point, return the <i>z</i> value from this surface. 
   * @param xy The <i>(x, y)</i> value, not null
   * @return The <i>z</i> value
   */
  public abstract V getZValue(Pair<T, U> xy);

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_name == null) ? 0 : _name.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Surface<?, ?, ?> other = (Surface<?, ?, ?>) obj;
    return ObjectUtils.equals(_name, other._name);
  }

}
