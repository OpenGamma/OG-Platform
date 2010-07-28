/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * @param <T>
 * @param <U>
 */
public abstract class InterpolatedVolatilitySurfaceModel<T, U> implements VolatilitySurfaceModel<Map<T, Double>, U> {
  private final Interpolator2D _interpolator;

  public InterpolatedVolatilitySurfaceModel(final Interpolator2D interpolator) {
    _interpolator = getInterpolator(interpolator);
  }

  @Override
  public InterpolatedVolatilitySurface getSurface(final Map<T, Double> volatilityData, final U dataBundle) {
    final Map<DoublesPair, Double> xyData = new HashMap<DoublesPair, Double>();
    for (final Map.Entry<T, Double> entry : volatilityData.entrySet()) {
      final T key = entry.getKey();
      xyData.put(DoublesPair.of(getXAxisFunctionValue(key, dataBundle), getYAxisFunctionValue(key, dataBundle)), entry.getValue());
    }
    return new InterpolatedVolatilitySurface(xyData, _interpolator);
  }

  protected Interpolator2D getInterpolator(final Interpolator2D interpolator) {
    return interpolator;
  }

  protected abstract double getXAxisFunctionValue(T t, U u);

  protected abstract double getYAxisFunctionValue(T t, U u);

}
