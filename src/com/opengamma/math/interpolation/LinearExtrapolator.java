/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

/**
 * 
 */
public class LinearExtrapolator<T extends Interpolator1DDataBundle, U extends InterpolationResult> implements ExtrapolatorMethod<T, U> {
  private static final double EPS = 1e-8;

  @SuppressWarnings("unchecked")
  @Override
  public U leftExtrapolate(T model, Double value, Interpolator1D<T, U> interpolator) {
    double eps = EPS * (model.lastKey() - model.firstKey());
    double x = model.firstKey();
    double y = model.firstValue();
    double m = (interpolator.interpolate(model, x + eps).getResult() - y) / eps;
    return (U) new InterpolationResult(y + (value - x) * m);
  }

  @SuppressWarnings("unchecked")
  @Override
  public U rightExtrapolate(T model, Double value, Interpolator1D<T, U> interpolator) {
    double eps = EPS * (model.lastKey() - model.firstKey());
    double x = model.lastKey();
    double y = model.lastValue();
    double m = (y - interpolator.interpolate(model, x - eps).getResult()) / eps;
    return (U) new InterpolationResult(y + (value - x) * m);
  }

}
