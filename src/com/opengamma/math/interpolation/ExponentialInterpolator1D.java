/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * @author emcleod
 * 
 */

public class ExponentialInterpolator1D extends Interpolator1D {

  @Override
  public InterpolationResult<Double> interpolate(final Map<Double, Double> data, final Double value) {
    final TreeMap<Double, Double> sorted = initData(data);
    if (value == null)
      throw new IllegalArgumentException("Value was null");
    final Double x1 = getLowerBoundKey(sorted, value);
    if (x1.equals(sorted.lastKey()))
      return new InterpolationResult<Double>(sorted.lastEntry().getValue());
    final Double x2 = sorted.higherKey(x1);
    final Double y1 = sorted.get(x1);
    final Double y2 = sorted.get(x2);
    final double xDiff = x2 - x1;
    final double result = Math.pow(y1, value * (x2 - value) / xDiff / x1) * Math.pow(y2, value * (value - x1) / xDiff / x2);
    return new InterpolationResult<Double>(result);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null)
      return false;
    if (o == this)
      return true;
    if (!(o instanceof ExponentialInterpolator1D))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

}
