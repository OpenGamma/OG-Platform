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
public class FlatExtrapolatorWithSensitivities<T extends Interpolator1DDataBundle, U extends InterpolationResultWithSensitivities> implements ExtrapolatorMethod<T, U> {

  @SuppressWarnings("unchecked")
  @Override
  public U leftExtrapolate(final T model, final Double value, final Interpolator1D<T, U> interpolator) {
    final int n = model.size();
    final double[] sense = new double[n];
    sense[0] = 1.0;
    return (U) new InterpolationResultWithSensitivities(model.firstValue(), sense);
  }

  @SuppressWarnings("unchecked")
  @Override
  public U rightExtrapolate(final T model, final Double value, final Interpolator1D<T, U> interpolator) {
    final int n = model.size();
    final double[] sense = new double[n];
    sense[n - 1] = 1.0;
    return (U) new InterpolationResultWithSensitivities(model.lastValue(), sense);
  }

}
