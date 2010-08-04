/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.temp;

import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * @param <T> Type of data bundle
 * @param <U> Type of interpolation result
 */
public class LinearExtrapolator<T extends Interpolator1DDataBundle, U extends InterpolationResult> implements ExtrapolatorMethod<T, U> {
  private static final double EPS = 1e-8;

  @SuppressWarnings("unchecked")
  @Override
  public U leftExtrapolate(final T model, final Double value, final Interpolator1D<T, U> interpolator) {
    final double eps = EPS * (model.lastKey() - model.firstKey());
    final double x = model.firstKey();
    final double y = model.firstValue();
    final double m = (interpolator.interpolate(model, x + eps).getResult() - y) / eps;
    return (U) new InterpolationResult(y + (value - x) * m);
  }

  @SuppressWarnings("unchecked")
  @Override
  public U rightExtrapolate(final T model, final Double value, final Interpolator1D<T, U> interpolator) {
    final double eps = EPS * (model.lastKey() - model.firstKey());
    final double x = model.lastKey();
    final double y = model.lastValue();
    final double m = (y - interpolator.interpolate(model, x - eps).getResult()) / eps;
    return (U) new InterpolationResult(y + (value - x) * m);
  }

}
