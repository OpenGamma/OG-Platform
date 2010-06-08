/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * A base class for interpolation in one dimension.
 * 
 * @author emcleod
 */

public abstract class Interpolator1D implements Interpolator<Map<Double, Double>, Double, Double>, Serializable {
  protected static final double EPS = 1e-12;

  /**
   * @param data
   *          A map containing the (x, y) data set.
   * @param value
   *          The value of x for which an interpolated value for y is to be
   *          found.
   * @return An InterpolationResult containing the interpolated value of y and
   *         (if appropriate) the estimated error of this value.
   */
  @Override
  public InterpolationResult<Double> interpolate(Map<Double, Double> data, Double value) {
    return interpolate(Interpolator1DModelFactory.fromMap(data), value);
  }

  public abstract InterpolationResult<Double> interpolate(Interpolator1DModel model, Double value);
  
  /**
   * 
   * @param data
   *          A map containing the (x, y) data set.
   * @return A map containing the data set sorted by x values.
   * @throws IllegalArgumentException
   *           Thrown if the data set is null or if its size is less than two.
   */
  protected Interpolator1DModel initData(final Map<Double, Double> data) {
    if (data == null) {
      throw new IllegalArgumentException("Data map was null");
    }
    if (data.size() < 2) {
      throw new IllegalArgumentException("Need at least two points to perform interpolation");
    }
    return new NavigableMapInterpolator1DModel(new TreeMap<Double, Double>(data));
  }

  protected boolean classEquals(final Object o) {
    if (o == null) {
      return false;
    }
    return getClass().equals(o.getClass());
  }
}
