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

  public abstract InterpolationResult<Double> interpolate(T model, Double value);

  protected boolean classEquals(final Object o) {
    if (o == null) {
      return false;
    }
    return getClass().equals(o.getClass());
  }
}
