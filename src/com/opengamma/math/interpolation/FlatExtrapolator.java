/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

/**
 * 
 */
public class FlatExtrapolator implements ExtrapolatorMethod {

  @Override
  public InterpolationResult interpolate(Interpolator1DDataBundle model, Double value, Interpolator1D interpolator) {
    final InterpolationBoundedValues boundedValues = model.getBoundedValues(value);
    if (boundedValues.getHigherBoundKey() == null || boundedValues.getHigherBoundKey() < 0) {
      return new InterpolationResult(model.lastValue());
    }
    if (boundedValues.getLowerBoundKey() == null || boundedValues.getLowerBoundKey() < 0) {
      return new InterpolationResult(model.firstValue());
    }
    throw new IllegalArgumentException("value was not not of range");
  }

}
