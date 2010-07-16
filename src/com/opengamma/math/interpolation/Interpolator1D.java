/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.io.Serializable;

import com.opengamma.util.CompareUtils;

/**
 * A base class for interpolation in one dimension.
 * @param <T> Type of Interpolator1DDataBundle
 * @param <U> Type of InterpolationResult
 */

public abstract class Interpolator1D<T extends Interpolator1DDataBundle, U extends InterpolationResult> implements Interpolator<T, Double, U>, Serializable {
  /**
   * Default accuracy
   */
  private final double _eps;

  public Interpolator1D() {
    _eps = 1e-12;
  }

  public Interpolator1D(final double eps) {
    _eps = eps;
  }

  public double getEPS() {
    return _eps;
  }

  protected void checkValue(final T data, final Double value) {
    final InterpolationBoundedValues boundedValues = data.getBoundedValues(value);
    if ((boundedValues.getHigherBoundKey() == null || boundedValues.getHigherBoundKey() < 0) && !CompareUtils.closeEquals(value, data.lastKey(), _eps)) {
      throw new InterpolationException(value + " was greater than maximum value of the data " + data.lastKey());
    }
    if ((boundedValues.getLowerBoundKey() == null || boundedValues.getLowerBoundKey() < 0) && !CompareUtils.closeEquals(value, data.firstKey(), _eps)) {
      throw new InterpolationException(value + " was less than minimum value of the data " + data.firstKey());
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result;
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
    return true;
  }

  public abstract U interpolate(T model, Double value);

  protected boolean classEquals(final Object o) {
    if (o == null) {
      return false;
    }
    return getClass().equals(o.getClass());
  }
}
