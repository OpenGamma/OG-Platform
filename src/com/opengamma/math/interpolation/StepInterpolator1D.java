/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Map;
import java.util.NavigableMap;

import com.opengamma.util.CompareUtils;

/**
 * @author emcleod
 * 
 */
public class StepInterpolator1D extends Interpolator1D {
  private final double _eps;

  public StepInterpolator1D() {
    _eps = 1e-12;
  }

  public StepInterpolator1D(final double eps) {
    _eps = Math.abs(eps);
  }

  @Override
  public InterpolationResult<Double> interpolate(final Map<Double, Double> data, final Double value) {
    if (value == null) {
      throw new IllegalArgumentException("x value to be interpolated was null");
    }
    final NavigableMap<Double, Double> sorted = initData(data);
    if (value < sorted.firstKey() || CompareUtils.closeEquals(sorted.firstKey(), value, _eps)) {
      return new InterpolationResult<Double>(sorted.firstEntry().getValue(), 0.);
    }
    if (value > sorted.lastKey() || CompareUtils.closeEquals(value, sorted.lastKey(), _eps)) {
      return new InterpolationResult<Double>(sorted.lastEntry().getValue(), 0.);
    }
    if (sorted.containsKey(value)) {
      return new InterpolationResult<Double>(sorted.get(value), 0.);
    }
    if (CompareUtils.closeEquals(sorted.higherKey(value), value, _eps)) {
      return new InterpolationResult<Double>(sorted.higherEntry(value).getValue(), 0.);
    }
    return new InterpolationResult<Double>(sorted.lowerEntry(value).getValue(), 0.);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof StepInterpolator1D)) {
      return false;
    }
    final StepInterpolator1D other = (StepInterpolator1D) o;
    return _eps == other._eps;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode() * 17 + ((Double) _eps).hashCode();
  }

}
