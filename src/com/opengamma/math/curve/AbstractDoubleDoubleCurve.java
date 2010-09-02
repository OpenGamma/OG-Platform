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
 */
public abstract class AbstractDoubleDoubleCurve implements Curve<Double, Double> {
  private static final AtomicLong ATOMIC = new AtomicLong();
  private final String _name;

  public AbstractDoubleDoubleCurve() {
    this(Long.toString(ATOMIC.getAndIncrement()));
  }

  public AbstractDoubleDoubleCurve(final String name) {
    _name = name;
  }

  public String getName() {
    return _name;
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
    final AbstractDoubleDoubleCurve other = (AbstractDoubleDoubleCurve) obj;
    return ObjectUtils.equals(_name, other._name);
  }

}
