/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * @param <T> Type of the data bundle
 */
public class Interpolator1DWithSensitivities<T extends Interpolator1DDataBundle> extends Interpolator1D<T, InterpolationResultWithSensitivities> {
  private final Interpolator1D<T, InterpolationResult> _interpolator;
  private static final double EPS = 1e-8;

  public Interpolator1DWithSensitivities(final Interpolator1D<T, InterpolationResult> interpolator) {
    _interpolator = interpolator;
  }

  @SuppressWarnings("unchecked")
  @Override
  public InterpolationResultWithSensitivities interpolate(final T data, final Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(data, "Model must not be null");
    checkValue(data, value);

    final double[] x = data.getKeys();
    final double[] y = data.getValues();
    final int n = y.length;
    double[] yUp = new double[n];
    double[] yDown = new double[n];
    final double[] sensitivity = new double[n];
    for (int node = 0; node < n; node++) {
      yUp = Arrays.copyOf(y, n);
      yDown = Arrays.copyOf(y, n);
      yUp[node] += EPS;
      yDown[node] -= EPS;
      final T modelUp = (T) Interpolator1DDataBundleFactory.fromSortedArrays(x, yUp, getUnderlyingInterpolator());
      final T modelDown = (T) Interpolator1DDataBundleFactory.fromSortedArrays(x, yDown, getUnderlyingInterpolator());
      final double up = getUnderlyingInterpolator().interpolate(modelUp, value).getResult();
      final double down = getUnderlyingInterpolator().interpolate(modelDown, value).getResult();
      sensitivity[node] = (up - down) / 2.0 / EPS;
    }
    return new InterpolationResultWithSensitivities(getUnderlyingInterpolator().interpolate(data, value).getResult(), sensitivity);
  }

  protected Interpolator1D<T, InterpolationResult> getUnderlyingInterpolator() {
    return _interpolator;
  }
}
