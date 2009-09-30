/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.util.Pair;

/**
 * 
 * @author emcleod
 */
public abstract class InterpolatedVolatilitySurfaceModel<T, U> implements VolatilitySurfaceModel<T, U> {
  private final Interpolator2D _interpolator;

  public InterpolatedVolatilitySurfaceModel(final Interpolator2D interpolator) {
    _interpolator = getInterpolator(interpolator);
  }

  @Override
  public VolatilitySurface getSurface(final Map<T, Double> volatilityData, final U dataBundle) {
    final Map<Pair<Double, Double>, Double> xyData = new HashMap<Pair<Double, Double>, Double>();
    for (final Map.Entry<T, Double> entry : volatilityData.entrySet()) {
      final T key = entry.getKey();
      xyData.put(new Pair<Double, Double>(getXAxisFunctionValue(key, dataBundle), getYAxisFunctionValue(key, dataBundle)), entry.getValue());
    }
    return new VolatilitySurface(xyData, _interpolator);
  }

  protected Interpolator2D getInterpolator(final Interpolator2D interpolator) {
    return interpolator;
  }

  protected abstract Double getXAxisFunctionValue(T t, U u);

  protected abstract Double getYAxisFunctionValue(T t, U u);

}
