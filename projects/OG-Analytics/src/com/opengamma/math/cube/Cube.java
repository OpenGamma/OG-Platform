/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.cube;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.tuple.Triple;

/**
 *
 * @param <S> The type of the x data
 * @param <T> The type of the y data
 * @param <U> The type of the z data
 * @param <V> The type of the values
 */
public abstract class Cube<S, T, U, V> {
  private static final AtomicLong ATOMIC = new AtomicLong();
  private final String _name;

  public Cube() {
    this(Long.toString(ATOMIC.getAndIncrement()));
  }

  public Cube(final String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  public abstract S[] getXData();

  public abstract T[] getYData();

  public abstract U[] getZData();

  public abstract V[] getData();

  public abstract int size();

  public abstract V getValue(S x, T y, U z);

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
