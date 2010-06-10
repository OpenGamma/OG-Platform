/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.io.Serializable;

/**
 * A base class for interpolation in one dimension.
 * @param <T> Type of Interpolator1DModel
 */

public abstract class Interpolator1D<T extends Interpolator1DModel> implements Interpolator<T, Double, Double>, Serializable {
  /**
   * Default accuracy
   */
  protected static final double EPS = 1e-12;

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

  public abstract InterpolationResult<Double> interpolate(T model, Double value);

  protected boolean classEquals(final Object o) {
    if (o == null) {
      return false;
    }
    return getClass().equals(o.getClass());
  }
}
