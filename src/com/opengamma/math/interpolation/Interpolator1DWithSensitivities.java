/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

/**
 * @param <T> Type of the Interpolator1DDataBundle
 */
public abstract class Interpolator1DWithSensitivities<T extends Interpolator1DDataBundle> extends Interpolator1D<T, InterpolationResultWithSensitivities> {
  private final Interpolator1D<T, InterpolationResult> _interpolator;

  public Interpolator1DWithSensitivities(final Interpolator1D<T, InterpolationResult> interpolator) {
    _interpolator = interpolator;
  }

  @Override
  public abstract InterpolationResultWithSensitivities interpolate(T data, Double value);

  protected Interpolator1D<T, InterpolationResult> getUnderlyingInterpolator() {
    return _interpolator;
  }
}
