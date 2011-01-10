/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.ObjectUtils;

/**
 * Defines a general curve (x, y) class. The x and y data can be any type. The curves are named; if a name is not provided then a unique
 * ID will be used. 
 * @param <T> The type of the x data
 * @param <U> The type of the y data
 */
public abstract class Curve<T extends Comparable<T>, U> {
  private static final AtomicLong ATOMIC = new AtomicLong();
  private final String _name;

  /**
   * Constructs a curve with an automatically-generated name
   */
  public Curve() {
    this(Long.toString(ATOMIC.getAndIncrement()));
  }

  /**
   * Constructs a curve with the given name
   * @param name The name of the curve
   */
  public Curve(final String name) {
    _name = name;
  }

  /**
   * 
   * @return The name of the curve
   */
  public String getName() {
    return _name;
  }

  /**
   * 
   * @return The x data for this curve
   */
  public abstract T[] getXData();

  /**
   * 
   * @return The y data for this curve
   */
  public abstract U[] getYData();

  /**
   * 
   * @return The number of data points used to construct this curve
   */
  public abstract int size();

  /**
   * Given an x value, return the y value from this curve
   * @param x The x value
   * @return The y value
   */
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
