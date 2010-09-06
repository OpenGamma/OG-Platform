/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.ObjectUtils;

/**
 * 
 * @param <T> The type of the x data
 * @param <U> The type of the y data
 */
public abstract class Curve<T extends Comparable<T>, U> {
  private static final AtomicLong ATOMIC = new AtomicLong();
  private final String _name;

  public Curve() {
    this(Long.toString(ATOMIC.getAndIncrement()));
  }

  public Curve(final String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  public abstract T[] getXData();

  public abstract U[] getYData();

  public abstract int size();

  public abstract U getYValue(T x);

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
    final Curve<?, ?> other = (Curve<?, ?>) obj;
    return ObjectUtils.equals(_name, other._name);
  }

}
