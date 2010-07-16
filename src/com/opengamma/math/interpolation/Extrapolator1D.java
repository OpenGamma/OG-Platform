/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

/**
 * 
 */
public class Extrapolator1D<T extends Interpolator1DDataBundle, U extends InterpolationResult> extends Interpolator1D<T, U> implements WrappedInterpolator {

  private final Interpolator1D<T, U> _interpolator;
  private final ExtrapolatorMethod<T, U> _leftExtrapolator;
  private final ExtrapolatorMethod<T, U> _rightExtrapolator;

  public Extrapolator1D(ExtrapolatorMethod<T, U> extrapolatorMethod, final Interpolator1D<T, U> interpolator) {
    _interpolator = interpolator;
    _leftExtrapolator = extrapolatorMethod;
    _rightExtrapolator = extrapolatorMethod;
  }

  public Extrapolator1D(ExtrapolatorMethod<T, U> leftExtrapolatorMethod, ExtrapolatorMethod<T, U> rightExtrapolatorMethod, final Interpolator1D<T, U> interpolator) {
    _interpolator = interpolator;
    _leftExtrapolator = leftExtrapolatorMethod;
    _rightExtrapolator = rightExtrapolatorMethod;
  }

  @Override
  public U interpolate(T model, Double value) {
    final InterpolationBoundedValues boundedValues = model.getBoundedValues(value);
    if (boundedValues.getHigherBoundKey() == null || boundedValues.getHigherBoundKey() < 0) {
      return _rightExtrapolator.rightExtrapolate(model, value, _interpolator);
    }
    if (boundedValues.getLowerBoundKey() == null || boundedValues.getLowerBoundKey() < 0) {
      return _leftExtrapolator.leftExtrapolate(model, value, _interpolator);
    }
    return _interpolator.interpolate(model, value);
  }

  public Interpolator1D<T, U> getUnderlyingInterpolator() {
    return _interpolator;
  }

}
