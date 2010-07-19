/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

/**
 * @param <T> Type of data bundle
 * @param <U> Type of interpolation result
 */
public class LinearExtrapolatorWithSensitivity<T extends Interpolator1DDataBundle, U extends InterpolationResultWithSensitivities> implements ExtrapolatorMethod<T, U> {
  private static final double EPS = 1e-6;

  @SuppressWarnings("unchecked")
  @Override
  public U leftExtrapolate(final T model, final Double value, final Interpolator1D<T, U> interpolator) {
    final double eps = EPS * (model.lastKey() - model.firstKey());
    final double x = model.firstKey();
    final double y = model.firstValue();
    final U deltaResult = interpolator.interpolate(model, x + eps);
    final double m = (deltaResult.getResult() - y) / eps;
    final double[] sense = deltaResult.getSensitivities();
    final int n = sense.length;
    for (int i = 1; i < n; i++) {
      sense[i] = sense[i] * (value - x) / eps;
    }
    sense[0] = 1 + (sense[0] - 1) * (value - x) / eps;
    return (U) new InterpolationResultWithSensitivities(y + (value - x) * m, sense);
  }

  @SuppressWarnings("unchecked")
  @Override
  public U rightExtrapolate(final T model, final Double value, final Interpolator1D<T, U> interpolator) {
    final double eps = EPS * (model.lastKey() - model.firstKey());
    final double x = model.lastKey();
    final double y = model.lastValue();
    final U deltaResult = interpolator.interpolate(model, x - eps);
    final double m = (y - deltaResult.getResult()) / eps;
    final double[] sense = deltaResult.getSensitivities();
    final int n = sense.length;
    for (int i = 0; i < n - 1; i++) {
      sense[i] = -sense[i] * (value - x) / eps;
    }
    sense[n - 1] = 1 + (1 - sense[n - 1]) * (value - x) / eps;
    return (U) new InterpolationResultWithSensitivities(y + (value - x) * m, sense);
  }
}
