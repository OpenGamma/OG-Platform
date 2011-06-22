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
 * @param <S> The type of the data needed for interpolation in the x-direction
 * @param <T> The type of the data needed for interpolation in the y-direction
 */
public class GridInterpolator2D<S extends Interpolator1DDataBundle, T extends Interpolator1DDataBundle> extends Interpolator2D<T> {
  //TODO this is really inefficient - needs to be changed in a similar way to 1D interpolation
  private final Interpolator1D<S> _xInterpolator;
  private final Interpolator1D<T> _yInterpolator;
  private final FirstThenSecondPairComparator<Double, Double> _comparator;

  public GridInterpolator2D(final Interpolator1D<S> xInterpolator, final Interpolator1D<T> yInterpolator) {
    Validate.notNull(xInterpolator);
    Validate.notNull(yInterpolator);
    _xInterpolator = xInterpolator;
    _yInterpolator = yInterpolator;
    _comparator = FirstThenSecondPairComparator.INSTANCE_DOUBLES;
  }

  public GridInterpolator2D(final Interpolator1D<S> xInterpolator, final Interpolator1D<T> yInterpolator, final Interpolator1D<S> xExtrapolator, final Interpolator1D<T> yExtrapolator) {
    Validate.notNull(xInterpolator);
    Validate.notNull(yInterpolator);
    _xInterpolator = new CombinedInterpolatorExtrapolator<S>(xInterpolator, xExtrapolator);
    _yInterpolator = new CombinedInterpolatorExtrapolator<T>(yInterpolator, yExtrapolator);
    _comparator = FirstThenSecondPairComparator.INSTANCE_DOUBLES;
  }

  public Map<Double, T> getDataBundle(final Map<DoublesPair, Double> data) {
    Validate.notNull(data, "data");
    return testData(data);
  }

  @Override
  public Double interpolate(final Map<Double, T> dataBundle, final DoublesPair value) {
    Validate.notNull(value);
    Validate.notNull(dataBundle, "data bundle");
    final Map<Double, Double> xData = new HashMap<Double, Double>();
    for (final Map.Entry<Double, T> entry : dataBundle.entrySet()) {
      xData.put(entry.getKey(), _yInterpolator.interpolate(entry.getValue(), value.getSecond()));
    }
    return _xInterpolator.interpolate(_xInterpolator.getDataBundle(xData), value.getKey());
  }

  private Map<Double, T> testData(final Map<DoublesPair, Double> data) {
    final Map<Double, T> result = new TreeMap<Double, T>();
    final TreeMap<DoublesPair, Double> sorted = new TreeMap<DoublesPair, Double>(_comparator);
    sorted.putAll(data);
    final Iterator<Map.Entry<DoublesPair, Double>> iterator = sorted.entrySet().iterator();
    final Map.Entry<DoublesPair, Double> firstEntry = iterator.next();
    double x = firstEntry.getKey().first;
    Map<Double, Double> yzValues = new TreeMap<Double, Double>();
    yzValues.put(firstEntry.getKey().second, firstEntry.getValue());
    while (iterator.hasNext()) {
      final Map.Entry<DoublesPair, Double> nextEntry = iterator.next();
      final double newX = nextEntry.getKey().first;
      if (Double.doubleToLongBits(newX) != Double.doubleToLongBits(x)) {
        final T interpolatorData = _yInterpolator.getDataBundle(yzValues);
        result.put(x, interpolatorData);
        yzValues = new TreeMap<Double, Double>();
        yzValues.put(nextEntry.getKey().second, nextEntry.getValue());
        x = newX;
      } else {
        yzValues.put(nextEntry.getKey().second, nextEntry.getValue());
      }
      if (!iterator.hasNext()) {
        yzValues.put(nextEntry.getKey().second, nextEntry.getValue());
        final T interpolatorData = _yInterpolator.getDataBundle(yzValues);
        result.put(x, interpolatorData);
      }
    }
    return result;
  }

  public Interpolator1D<S> getXInterpolator() {
    return _xInterpolator;
  }

  public Interpolator1D<T> getYInterpolator() {
    return _yInterpolator;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
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
