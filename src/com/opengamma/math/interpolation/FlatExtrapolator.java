/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

/**
 * 
 */
public class FlatExtrapolator<T extends Interpolator1DDataBundle, U extends InterpolationResult> implements ExtrapolatorMethod<T, U> {

  @SuppressWarnings("unchecked")
  @Override
  public U leftExtrapolate(T model, Double value, Interpolator1D<T, U> interpolator) {
    return (U) new InterpolationResult(model.firstValue());
  }

  @SuppressWarnings("unchecked")
  @Override
  public U rightExtrapolate(T model, Double value, Interpolator1D<T, U> interpolator) {
    return (U) new InterpolationResult(model.lastValue());
  }

}
