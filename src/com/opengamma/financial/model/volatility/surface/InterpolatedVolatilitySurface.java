/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.FirstThenSecondPairComparator;
import com.opengamma.util.tuple.Pair;

/**
 * 
 * A volatility surface contains volatilities for pairs of values (x, y) (e.g.
 * time and strike).
 * 
 */

public class InterpolatedVolatilitySurface extends VolatilitySurface {
  private static final Logger s_logger = LoggerFactory.getLogger(InterpolatedVolatilitySurface.class);
  private final SortedMap<Pair<Double, Double>, Double> _volatilityData;
  private final SortedMap<Pair<Double, Double>, Double> _varianceData;
  private final Interpolator2D _interpolator;

  /**
   * 
   * @param data
   *          A map containing pairs of (x, y) values and volatilities as
   *          decimals (i.e. 20% = 0.2).
   * @param interpolator
   *          An interpolator to get volatilities for an (x, y) pair that falls
   *          in between nodes.
   * @throws IllegalArgumentException
   *           Thrown if the data map is null or empty.
   */
  public InterpolatedVolatilitySurface(final Map<Pair<Double, Double>, Double> data, final Interpolator2D interpolator) {
    Validate.notNull(data);
    Validate.notNull(interpolator);
    ArgumentChecker.notEmpty(data, "data");
    for (final Double sigma : data.values()) {
      if (sigma < 0) {
        throw new IllegalArgumentException("Cannot have negative volatility");
      }
    }
    final SortedMap<Pair<Double, Double>, Double> sortedVolatility = new TreeMap<Pair<Double, Double>, Double>(FirstThenSecondPairComparator.INSTANCE_DOUBLES);
    sortedVolatility.putAll(data);
    final SortedMap<Pair<Double, Double>, Double> sortedVariance = new TreeMap<Pair<Double, Double>, Double>(FirstThenSecondPairComparator.INSTANCE_DOUBLES);
    for (final Map.Entry<Pair<Double, Double>, Double> entry : data.entrySet()) {
      sortedVariance.put(entry.getKey(), entry.getValue() * entry.getValue());
    }
    _volatilityData = Collections.<Pair<Double, Double>, Double>unmodifiableSortedMap(sortedVolatility);
    _varianceData = Collections.<Pair<Double, Double>, Double>unmodifiableSortedMap(sortedVariance);
    _interpolator = interpolator;
  }

  /**
   * 
   * @return The data sorted by (x, y) pair. The ordering is first x, then y
   * 
   * @see com.opengamma.util.tuple.FirstThenSecondPairComparator
   */
  public SortedMap<Pair<Double, Double>, Double> getData() {
    return _volatilityData;
  }

  /**
   * 
   * @return The interpolator for this surface.
   */
  public Interpolator2D getInterpolator() {
    return _interpolator;
  }

  /**
   * @param xy The (x, y) coordinate
   * @return The volatility for (x, y).
   */
  @Override
  public Double getVolatility(final Pair<Double, Double> xy) {
    Validate.notNull(xy, "xy");
    Validate.notNull(xy.getFirst(), "x value");
    Validate.notNull(xy.getSecond(), "y value");
    return Math.sqrt(_interpolator.interpolate(_varianceData, xy));
  }

  public Set<Pair<Double, Double>> getXYData() {
    return _volatilityData.keySet();
  }

  @Override
  public VolatilitySurface withMultipleShifts(final Map<Pair<Double, Double>, Double> shifts) {
    Validate.notNull(shifts, "shifts map");
    if (shifts.isEmpty()) {
      s_logger.info("Shift map was empty; returning identical surface");
      return new InterpolatedVolatilitySurface(getData(), getInterpolator());
    }
    final Map<Pair<Double, Double>, Double> data = getData();
    final Map<Pair<Double, Double>, Double> map = new HashMap<Pair<Double, Double>, Double>(data);
    for (final Map.Entry<Pair<Double, Double>, Double> entry : shifts.entrySet()) {
      if (entry.getKey() == null) {
        throw new IllegalArgumentException("Null xy pair in shift map");
      }
      if (entry.getValue() == null) {
        throw new IllegalArgumentException("Null shift in shift map");
      }
      if (map.containsKey(entry.getKey())) {
        map.put(entry.getKey(), map.get(entry.getKey()) + entry.getValue());
      } else {
        map.put(entry.getKey(), getVolatility(entry.getKey()) + entry.getValue());
      }
    }
    return new InterpolatedVolatilitySurface(map, getInterpolator());
  }

  @Override
  public VolatilitySurface withParallelShift(final double shift) {
    final Map<Pair<Double, Double>, Double> data = getData();
    final Map<Pair<Double, Double>, Double> shifted = new HashMap<Pair<Double, Double>, Double>();
    for (final Map.Entry<Pair<Double, Double>, Double> entry : data.entrySet()) {
      shifted.put(entry.getKey(), entry.getValue() + shift);
    }
    return new InterpolatedVolatilitySurface(shifted, getInterpolator());
  }

  @Override
  public VolatilitySurface withSingleShift(final Pair<Double, Double> xy, final double shift) {
    Validate.notNull(xy, "xy");
    Validate.notNull(xy.getFirst(), "x value");
    Validate.notNull(xy.getSecond(), "y value");
    final Map<Pair<Double, Double>, Double> data = getData();
    final Map<Pair<Double, Double>, Double> map = new HashMap<Pair<Double, Double>, Double>(data);
    if (map.containsKey(xy)) {
      map.put(xy, map.get(xy) + shift);
      return new InterpolatedVolatilitySurface(map, getInterpolator());
    }
    map.put(xy, getVolatility(xy) + shift);
    return new InterpolatedVolatilitySurface(map, getInterpolator());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_volatilityData == null ? 0 : _volatilityData.hashCode());
    result = prime * result + (_interpolator == null ? 0 : _interpolator.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final InterpolatedVolatilitySurface other = (InterpolatedVolatilitySurface) obj;
    return ObjectUtils.equals(_volatilityData, other._volatilityData) && ObjectUtils.equals(_interpolator, other._interpolator);
  }
}
