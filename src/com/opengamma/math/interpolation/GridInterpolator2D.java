/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.opengamma.util.FirstThenSecondPairComparator;
import com.opengamma.util.Pair;

/**
 * 
 * @author emcleod
 */
public class GridInterpolator2D extends Interpolator2D {
  private final Interpolator1D _xInterpolator;
  private final Interpolator1D _yInterpolator;
  private final FirstThenSecondPairComparator<Double, Double> _comparator;

  public GridInterpolator2D(final Interpolator1D xInterpolator, final Interpolator1D yInterpolator) {
    if (xInterpolator == null)
      throw new IllegalArgumentException("X interpolator was null");
    if (yInterpolator == null)
      throw new IllegalArgumentException("Y interpolator was null");
    _xInterpolator = xInterpolator;
    _yInterpolator = yInterpolator;
    _comparator = new FirstThenSecondPairComparator<Double, Double>();
  }

  @Override
  public InterpolationResult<Double> interpolate(final Map<Pair<Double, Double>, Double> data, final Pair<Double, Double> value) {
    if (value == null)
      throw new IllegalArgumentException("Value was null");
    final Map<Double, TreeMap<Double, Double>> sorted = testData(data);
    final Map<Double, Double> xData = new HashMap<Double, Double>();
    for (final Map.Entry<Double, TreeMap<Double, Double>> entry : sorted.entrySet()) {
      xData.put(entry.getKey(), _yInterpolator.interpolate(entry.getValue(), value.getSecond()).getResult());
    }
    return _xInterpolator.interpolate(xData, value.getKey());
  }

  private Map<Double, TreeMap<Double, Double>> testData(final Map<Pair<Double, Double>, Double> data) {
    if (data == null)
      throw new IllegalArgumentException("Data map was null");
    if (data.size() < 4)
      throw new IllegalArgumentException("Need at least four data points to perform 2D grid interpolation");
    final TreeMap<Pair<Double, Double>, Double> sorted = new TreeMap<Pair<Double, Double>, Double>(_comparator);
    sorted.putAll(data);
    final Map<Double, TreeMap<Double, Double>> split = new TreeMap<Double, TreeMap<Double, Double>>();
    Pair<Double, Double> pair;
    Double z;
    for (final Map.Entry<Pair<Double, Double>, Double> entry : sorted.entrySet()) {
      pair = entry.getKey();
      z = entry.getValue();
      if (z == null)
        throw new IllegalArgumentException("The value for " + pair + " was null");
      if (split.containsKey(pair.getKey())) {
        split.get(pair.getKey()).put(pair.getValue(), z);
      } else {
        final TreeMap<Double, Double> m = new TreeMap<Double, Double>();
        m.put(pair.getValue(), z);
        split.put(pair.getKey(), m);
      }
    }
    if (split.size() == 1)
      throw new IllegalArgumentException("Data were on a line - cannot use grid interpolation");
    final Iterator<TreeMap<Double, Double>> iter = split.values().iterator();
    final int size = iter.next().size();
    while (iter.hasNext()) {
      if (iter.next().size() != size)
        throw new InterpolationException("Data were not on a grid");
    }
    return split;
  }

  public Interpolator1D getXInterpolator() {
    return _xInterpolator;
  }

  public Interpolator1D getYInterpolator() {
    return _yInterpolator;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null)
      return false;
    if (o == this)
      return true;
    if (!(o instanceof GridInterpolator2D))
      return false;
    final GridInterpolator2D other = (GridInterpolator2D) o;
    return getXInterpolator().equals(other.getXInterpolator()) && getYInterpolator().equals(other.getYInterpolator());
  }

  @Override
  public int hashCode() {
    int hc = 1;
    hc = (hc * 31) + getXInterpolator().hashCode();
    hc = (hc * 31) + getYInterpolator().hashCode();
    return hc;
  }

}
