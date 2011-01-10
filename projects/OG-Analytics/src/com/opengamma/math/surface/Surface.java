/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.tuple.Pair;

/**
 * 
 * @param <T> The type of the x data
 * @param <U> The type of the y data
 * @param <V> The type of the z data
 */
public abstract class Surface<T, U, V> {
  private static final AtomicLong ATOMIC = new AtomicLong();
  private final String _name;

  public Surface() {
    this(Long.toString(ATOMIC.getAndIncrement()));
  }

  public Surface(final String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  public abstract T[] getXData();

  public abstract U[] getYData();

  public abstract V[] getZData();

  public abstract int size();

  public abstract V getZValue(T x, U y);

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
