/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

/**
 * @param <T> Type of the Interpolator1DModel
 */
public abstract class Interpolator1DWithSensitivities<T extends Interpolator1DModel> extends
    Interpolator1D<T, InterpolationResultWithSensitivities> {
  private final Interpolator1D<T, InterpolationResult> _interpolator;

  public Interpolator1DWithSensitivities(final Interpolator1D<T, InterpolationResult> interpolator) {
    System.out.println("inside Interpolator1DWithSensitivities constructor with " + interpolator.toString());
    _interpolator = interpolator;
  }

  @Override
  public abstract InterpolationResultWithSensitivities interpolate(T model, Double value);

  protected Interpolator1D<T, InterpolationResult> getUnderlyingInterpolator() {
    return _interpolator;
  }
}
