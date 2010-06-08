/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Map;

import com.opengamma.util.CompareUtils;

/**
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
    final Interpolator1DModel model = initData(data);
    if (value < model.firstKey() || CompareUtils.closeEquals(model.firstKey(), value, _eps)) {
      return new InterpolationResult<Double>(model.firstValue(), 0.);
    }
    if (value > model.lastKey() || CompareUtils.closeEquals(value, model.lastKey(), _eps)) {
      return new InterpolationResult<Double>(model.lastValue(), 0.);
    }
    if (model.containsKey(value)) {
      return new InterpolationResult<Double>(model.get(value), 0.);
    }
    if (CompareUtils.closeEquals(model.higherKey(value), value, _eps)) {
      return new InterpolationResult<Double>(model.higherValue(value), 0.);
    }
    return new InterpolationResult<Double>(model.get(model.getLowerBoundKey(value)), 0.);
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
