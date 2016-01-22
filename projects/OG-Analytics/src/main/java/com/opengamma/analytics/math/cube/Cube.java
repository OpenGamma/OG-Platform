/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.tuple.Triple;

/**
 * Defines a general cube <i>(x, y, z, value)</i> class. The data data be of any type. The cubes are named; if a name is not provided then a unique ID will
 * be generated.
 * @param <S> The type of the x data
 * @param <T> The type of the y data
 * @param <U> The type of the z data
 * @param <V> The type of the values
 */
public abstract class Cube<S, T, U, V> implements Serializable {
  private static final AtomicLong ATOMIC = new AtomicLong();
  private final String _name;

  /**
   * Constructs a cube with an automatically-generated name
   */
  public Cube() {
    this(Long.toString(ATOMIC.getAndIncrement()));
  }

  /**
   * Constructs a cube with the supplied name
   * @param name The name of the cube
   */
  public Cube(final String name) {
    _name = name;
  }

  
  /**
   * @return The name of the cube
   */
  public String getName() {
    return _name;
  }

  /**
   * @return The <i>x</i> data for this cube
   */
  public abstract S[] getXData();

  /**
   * @return The <i>y</i> data for this cube
   */
  public abstract T[] getYData();

  /**
   * @return The <i>z</i> data for this cube
   */
  public abstract U[] getZData();

  /**
   * @return The values of this cube
   */
  public abstract V[] getValues();

  /**
   * @return The number of <i>(x, y, z)</i> points used to construct the cube
   */
  public abstract int size();

  /**
   * Given <i>(x, y, z)</i>, returns the value at that point
   * @param x The <i>x</i> value, not null
   * @param y The <i>y</i> value, not null
   * @param z The <i>z</i> value, not null
   * @return The value
   */
  public abstract V getValue(S x, T y, U z);

  /**
   * Given <i>(x, y, z)</i>, returns the value at that point
   * @param xyz An <i>(x, y, z)</i> triple, not null and with no null elements
   * @return The value
   */
  public abstract V getValue(Triple<S, T, U> xyz);

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
    final Cube<?, ?, ?, ?> other = (Cube<?, ?, ?, ?>) obj;
    return ObjectUtils.equals(_name, other._name);
  }

}
