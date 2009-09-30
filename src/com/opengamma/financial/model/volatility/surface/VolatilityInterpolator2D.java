/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.math.interpolation.InterpolationResult;
import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.util.Pair;

/**
 * 
 * @author emcleod
 */
public class VolatilityInterpolator2D extends Interpolator2D {
  private final Interpolator2D _interpolator;

  public VolatilityInterpolator2D(final Interpolator2D interpolator) {
    _interpolator = interpolator;
  }

  @Override
  public InterpolationResult<Double> interpolate(final Map<Pair<Double, Double>, Double> data, final Pair<Double, Double> value) {
    final Map<Pair<Double, Double>, Double> variances = new HashMap<Pair<Double, Double>, Double>();
    for (final Map.Entry<Pair<Double, Double>, Double> entry : data.entrySet()) {
      variances.put(entry.getKey(), entry.getValue() * entry.getValue());
    }
    final InterpolationResult<Double> squaredResult = _interpolator.interpolate(variances, value);
    return new InterpolationResult<Double>(Math.sqrt(squaredResult.getResult()), Math.sqrt(squaredResult.getErrorEstimate()));
  }

}
