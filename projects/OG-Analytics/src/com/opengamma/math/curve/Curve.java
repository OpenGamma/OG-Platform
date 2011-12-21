/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.math.function.Function1D;

/**
 * Defines a general curve <i>(x, y)</i> class. The <i>x</i> and <i>y</i> data can be any type. The curves are named; if a name is not provided then a unique
 * ID will be used.
 * @param <T> The type of the <i>x</i> data
 * @param <U> The type of the <i>y</i> data
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
   * @return The <i>x</i> data for this curve
   */
  public abstract T[] getXData();

  /**
   * 
   * @return The <i>y</i> data for this curve
   */
  public abstract U[] getYData();

  /**
   * 
   * @return The number of data points used to construct this curve
   */
  public abstract int size();

  /**
   * Given an <i>x</i> value, return the <i>y</i> value from this curve
   * @param x The <i>x</i> value, not null
   * @return The <i>y</i> value
   */
  public abstract U getYValue(T x);

  /**
   * converts a curve to a Function1D
   * @return
   */
  public Function1D<T, U> toFunction1D() {
    return new Function1D<T, U>() {
      @Override
      public U evaluate(T x) {
        return Curve.this.getYValue(x);
      }
    };
  }

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
