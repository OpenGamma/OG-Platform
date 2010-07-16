/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

/**
 * 
 */
public class LinearExtrapolatorWithSensitivity<T extends Interpolator1DDataBundle, U extends InterpolationResultWithSensitivities> implements ExtrapolatorMethod<T, U> {
  private static final double EPS = 1e-6;

  @SuppressWarnings("unchecked")
  @Override
  public U leftExtrapolate(T model, Double value, Interpolator1D<T, U> interpolator) {
    double eps = EPS * (model.lastKey() - model.firstKey());
    double x = model.firstKey();
    double y = model.firstValue();
    U deltaResult = interpolator.interpolate(model, x + eps);
    double m = (deltaResult.getResult() - y) / eps;
    double[] sense = deltaResult.getSensitivities();
    int n = sense.length;
    for (int i = 1; i < n; i++) {
      sense[i] = sense[i] * (value - x) / eps;
    }
    sense[0] = 1 + (sense[0] - 1) * (value - x) / eps;
    return (U) new InterpolationResultWithSensitivities(y + (value - x) * m, sense);
  }

  @SuppressWarnings("unchecked")
  @Override
  public U rightExtrapolate(T model, Double value, Interpolator1D<T, U> interpolator) {
    double eps = EPS * (model.lastKey() - model.firstKey());
    double x = model.lastKey();
    double y = model.lastValue();
    U deltaResult = interpolator.interpolate(model, x - eps);
    double m = (y - deltaResult.getResult()) / eps;
    double[] sense = deltaResult.getSensitivities();
    int n = sense.length;
    for (int i = 0; i < n - 1; i++) {
      sense[i] = -sense[i] * (value - x) / eps;
    }
    sense[n - 1] = 1 + (1 - sense[n - 1]) * (value - x) / eps;
    return (U) new InterpolationResultWithSensitivities(y + (value - x) * m, sense);
  }
}
