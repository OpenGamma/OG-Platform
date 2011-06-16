/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.FirstThenSecondPairComparator;

/**
*
 */
public class GridInterpolator2D<S extends Interpolator1DDataBundle, T extends Interpolator1DDataBundle> extends Interpolator2D<T> {
  //TODO this is really inefficient - needs to be changed in a similar way to 1D interpolation
  private final Interpolator1D<S> _xInterpolator;
  private final Interpolator1D<T> _yInterpolator;
  private final FirstThenSecondPairComparator<Double, Double> _comparator;
  //  private Map<Double, T> _cached = null;
  private static final FlatExtrapolator1D EXTRAPOLATOR = new FlatExtrapolator1D();

  public GridInterpolator2D(final Interpolator1D<S> xInterpolator, final Interpolator1D<T> yInterpolator) {
    Validate.notNull(xInterpolator);
    Validate.notNull(yInterpolator);
    _xInterpolator = new CombinedInterpolatorExtrapolator(xInterpolator, EXTRAPOLATOR);
    _yInterpolator = new CombinedInterpolatorExtrapolator(yInterpolator, EXTRAPOLATOR);
    _comparator = FirstThenSecondPairComparator.INSTANCE_DOUBLES;
  }

  public Map<Double, T> getDataBundle(final Map<DoublesPair, Double> data) {
    Map<Double, T> temp = null;
    try {
      temp = testData(data);
    } catch (Exception e) {
      System.out.println(e.toString());
    }
    return temp;
  }

  @Override
  public Double interpolate(/*final Map<DoublesPair, Double> data,*/final Map<Double, T> dataBundle, final DoublesPair value) {
    Validate.notNull(value);
    //   final Map<Double, T> sorted = _cached == null ? testData(data) : _cached;
    final Map<Double, Double> xData = new HashMap<Double, Double>();
    for (final Map.Entry<Double, T> entry : dataBundle.entrySet()) {
      xData.put(entry.getKey(), _yInterpolator.interpolate(entry.getValue(), value.getSecond()));
    }
    return _xInterpolator.interpolate(_xInterpolator.getDataBundle(xData), value.getKey());
  }

  private Map<Double, T> testData(final Map<DoublesPair, Double> data) {
    Map<Double, T> result = new TreeMap<Double, T>();
    TreeMap<DoublesPair, Double> sorted = new TreeMap<DoublesPair, Double>(_comparator);
    sorted.putAll(data);
    Iterator<Map.Entry<DoublesPair, Double>> iterator = sorted.entrySet().iterator();
    Map.Entry<DoublesPair, Double> firstEntry = iterator.next();
    double x = firstEntry.getKey().first;
    Map<Double, Double> yzValues = new TreeMap<Double, Double>();
    yzValues.put(firstEntry.getKey().second, firstEntry.getValue());
    while (iterator.hasNext()) {
      Map.Entry<DoublesPair, Double> nextEntry = iterator.next();
      double newX = nextEntry.getKey().first;
      if (Double.doubleToLongBits(newX) != Double.doubleToLongBits(x)) {
        T interpolatorData = _yInterpolator.getDataBundle(yzValues);
        result.put(x, interpolatorData);
        yzValues = new TreeMap<Double, Double>();
        yzValues.put(nextEntry.getKey().second, nextEntry.getValue());
        x = newX;
      } else {
        yzValues.put(nextEntry.getKey().second, nextEntry.getValue());
      }
      if (!iterator.hasNext()) {
        yzValues.put(nextEntry.getKey().second, nextEntry.getValue());
        T interpolatorData = _yInterpolator.getDataBundle(yzValues);
        result.put(x, interpolatorData);
      }
    }
    //    if (_cached != null) {
    //      throw new OpenGammaRuntimeException("ARSE");
    //    }
    //    _cached = result;
    return result;
    //    Validate.notNull(data);
    //    if (data.size() < 4) {
    //      throw new IllegalArgumentException("Need at least four data points to perform 2D grid interpolation");
    //    }
    //    final TreeMap<DoublesPair, Double> sorted = new TreeMap<DoublesPair, Double>(_comparator);
    //    sorted.putAll(data);
    //    final Map<Double, TreeMap<Double, Double>> split = new TreeMap<Double, TreeMap<Double, Double>>();
    //    DoublesPair pair;
    //    Double z;
    //    for (final Map.Entry<DoublesPair, Double> entry : sorted.entrySet()) {
    //      pair = entry.getKey();
    //      z = entry.getValue();
    //      if (z == null) {
    //        throw new IllegalArgumentException("The value for " + pair + " was null");
    //      }
    //      if (split.containsKey(pair.getKey())) {
    //        split.get(pair.getKey()).put(pair.getValue(), z);
    //      } else {
    //        final TreeMap<Double, Double> m = new TreeMap<Double, Double>();
    //        m.put(pair.getValue(), z);
    //        split.put(pair.getKey(), m);
    //      }
    //    }
    //    if (split.size() == 1) {
    //      throw new IllegalArgumentException("Data were on a line - cannot use grid interpolation");
    //    }
    //    System.err.println(split);
    //    final Iterator<Map.Entry<Double, TreeMap<Double, Double>>> iter = split.entrySet().iterator();
    //    Map.Entry<Double, TreeMap<Double, Double>> entry = iter.next();
    //    final int size = entry.getValue().size();
    //    final Map<Double, T> result = new HashMap<Double, T>();
    //    result.put(entry.getKey(), _yInterpolator.getDataBundle(entry.getValue()));
    //    while (iter.hasNext()) {
    //      entry = iter.next();
    //      if (entry.getValue().size() != size) {
    //        throw new MathException("Data were not on a grid: " + size + " " + entry.getValue().size());
    //      }
    //      result.put(entry.getKey(), _yInterpolator.getDataBundle(entry.getValue()));
    //    }
    //    return result;
  }

  public Interpolator1D<S> getXInterpolator() {
    return _xInterpolator;
  }

  public Interpolator1D<T> getYInterpolator() {
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
