/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

/**
 * 
 */
public class LinearExtrapolator implements ExtrapolatorMethod {
  private static final double EPS = 1e-8;

  @Override
  public InterpolationResult interpolate(Interpolator1DDataBundle model, Double value, Interpolator1D interpolator) {
    final InterpolationBoundedValues boundedValues = model.getBoundedValues(value);
    if (boundedValues.getHigherBoundKey() == null || boundedValues.getHigherBoundKey() < 0) {
      double x = model.lastKey();
      double y = model.lastValue();
      double m = (y - interpolator.interpolate(model, x - EPS).getResult()) / EPS;
      return new InterpolationResult(y + (value - x) * m);
    }
    if (boundedValues.getLowerBoundKey() == null || boundedValues.getLowerBoundKey() < 0) {
      double x = model.firstKey();
      double y = model.firstValue();
      double m = (interpolator.interpolate(model, x + EPS).getResult() - y) / EPS;
      return new InterpolationResult(y + (value - x) * m);
    }
    throw new IllegalArgumentException("value was not not of range");
  }

}
