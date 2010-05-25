/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.curve;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.opengamma.math.interpolation.Interpolator1D;

/**
 * 
 */
public class InterpolatedVolatilityCurve extends VolatilityCurve {
  private final SortedMap<Double, Double> _volatilityData;
  private final Interpolator1D _interpolator;

  public InterpolatedVolatilityCurve(final Map<Double, Double> data, final Interpolator1D interpolator) {
    if (data == null)
      throw new IllegalArgumentException("Data map was null");
    if (interpolator == null)
      throw new IllegalArgumentException("Interpolator was null");
    if (data.size() < 2)
      throw new IllegalArgumentException("Need to have at least two data points for an interpolated curve");
    final SortedMap<Double, Double> sortedVolatilities = new TreeMap<Double, Double>();
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      if (entry.getKey() < 0)
        throw new IllegalArgumentException("Cannot have negative time in a discount curve");
      sortedVolatilities.put(entry.getKey(), entry.getValue());
    }
    _volatilityData = Collections.<Double, Double> unmodifiableSortedMap(sortedVolatilities);
    _interpolator = interpolator;
  }

  @Override
  public Double getVolatility(final Double t) {
    return _interpolator.interpolate(getVolatilityData(), t).getResult();
  }

  @Override
  public Set<Double> getXData() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public VolatilityCurve withMultipleShifts(final Map<Double, Double> shifts) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public VolatilityCurve withParallelShift(final Double shift) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public VolatilityCurve withSingleShift(final Double x, final Double shift) {
    // TODO Auto-generated method stub
    return null;
  }

  public Map<Double, Double> getVolatilityData() {
    return _volatilityData;
  }

}
