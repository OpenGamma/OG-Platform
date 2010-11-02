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

import org.apache.commons.lang.Validate;

import com.opengamma.math.MathException;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.FirstThenSecondPairComparator;

/**
*
 */
public class GridInterpolator2D extends Interpolator2D {
  //TODO this is really inefficient - needs to be changed in a similar way to 1D interpolation
  private final Interpolator1D<Interpolator1DDataBundle> _xInterpolator;
  private final Interpolator1D<Interpolator1DDataBundle> _yInterpolator;
  private final FirstThenSecondPairComparator<Double, Double> _comparator;

  public GridInterpolator2D(final Interpolator1D<Interpolator1DDataBundle> xInterpolator, final Interpolator1D<Interpolator1DDataBundle> yInterpolator) {
    Validate.notNull(xInterpolator);
    Validate.notNull(yInterpolator);
    _xInterpolator = xInterpolator;
    _yInterpolator = yInterpolator;
    _comparator = FirstThenSecondPairComparator.INSTANCE_DOUBLES;
  }

  @Override
  public Double interpolate(final Map<DoublesPair, Double> data, final DoublesPair value) {
    Validate.notNull(value);
    final Map<Double, Interpolator1DDataBundle> sorted = testData(data);
    final Map<Double, Double> xData = new HashMap<Double, Double>();
    for (final Map.Entry<Double, Interpolator1DDataBundle> entry : sorted.entrySet()) {
      xData.put(entry.getKey(), _yInterpolator.interpolate(entry.getValue(), value.getSecond()));
    }
    return _xInterpolator.interpolate(_xInterpolator.getDataBundle(xData), value.getKey());
  }

  private Map<Double, Interpolator1DDataBundle> testData(final Map<DoublesPair, Double> data) {
    Validate.notNull(data);
    if (data.size() < 4) {
      throw new IllegalArgumentException("Need at least four data points to perform 2D grid interpolation");
    }
    final TreeMap<DoublesPair, Double> sorted = new TreeMap<DoublesPair, Double>(_comparator);
    sorted.putAll(data);
    final Map<Double, TreeMap<Double, Double>> split = new TreeMap<Double, TreeMap<Double, Double>>();
    DoublesPair pair;
    Double z;
    for (final Map.Entry<DoublesPair, Double> entry : sorted.entrySet()) {
      pair = entry.getKey();
      z = entry.getValue();
      if (z == null) {
        throw new IllegalArgumentException("The value for " + pair + " was null");
      }
      if (split.containsKey(pair.getKey())) {
        split.get(pair.getKey()).put(pair.getValue(), z);
      } else {
        final TreeMap<Double, Double> m = new TreeMap<Double, Double>();
        m.put(pair.getValue(), z);
        split.put(pair.getKey(), m);
      }
    }
    if (split.size() == 1) {
      throw new IllegalArgumentException("Data were on a line - cannot use grid interpolation");
    }
    final Iterator<Map.Entry<Double, TreeMap<Double, Double>>> iter = split.entrySet().iterator();
    Map.Entry<Double, TreeMap<Double, Double>> entry = iter.next();
    final int size = entry.getValue().size();
    final Map<Double, Interpolator1DDataBundle> result = new HashMap<Double, Interpolator1DDataBundle>();
    result.put(entry.getKey(), _yInterpolator.getDataBundle(entry.getValue()));
    while (iter.hasNext()) {
      entry = iter.next();
      if (entry.getValue().size() != size) {
        throw new MathException("Data were not on a grid");
      }
      result.put(entry.getKey(), _yInterpolator.getDataBundle(entry.getValue()));
    }
    return result;
  }

  public Interpolator1D<Interpolator1DDataBundle> getXInterpolator() {
    return _xInterpolator;
  }

  public Interpolator1D<Interpolator1DDataBundle> getYInterpolator() {
    return _yInterpolator;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof GridInterpolator2D)) {
      return false;
    }
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
