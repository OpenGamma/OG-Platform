/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

/**
 * 
 */
public class FlatExtrapolatorWithSensitivities<T extends Interpolator1DDataBundle, U extends InterpolationResultWithSensitivities> implements ExtrapolatorMethod<T, U> {

  @SuppressWarnings("unchecked")
  @Override
  public U leftExtrapolate(T model, Double value, Interpolator1D<T, U> interpolator) {
    int n = model.size();
    double[] sense = new double[n];
    sense[0] = 1.0;
    return (U) new InterpolationResultWithSensitivities(model.firstValue(), sense);
  }

  @SuppressWarnings("unchecked")
  @Override
  public U rightExtrapolate(T model, Double value, Interpolator1D<T, U> interpolator) {
    int n = model.size();
    double[] sense = new double[n];
    sense[n - 1] = 1.0;
    return (U) new InterpolationResultWithSensitivities(model.lastValue(), sense);
  }

}
