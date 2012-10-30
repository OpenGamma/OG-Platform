/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.FirstThenSecondPairComparator;

/**
 * 
 */
public class GridInterpolator2D extends Interpolator2D {
  //TODO this is really inefficient - needs to be changed in a similar way to 1D interpolation
  private final Interpolator1D _xInterpolator;
  private final Interpolator1D _yInterpolator;
  private final FirstThenSecondPairComparator<Double, Double> _comparator;

  public GridInterpolator2D(final Interpolator1D xInterpolator, final Interpolator1D yInterpolator) {
    Validate.notNull(xInterpolator);
    Validate.notNull(yInterpolator);
    _xInterpolator = xInterpolator;
    _yInterpolator = yInterpolator;
    _comparator = FirstThenSecondPairComparator.INSTANCE_DOUBLES;
  }

  public GridInterpolator2D(final Interpolator1D xInterpolator, final Interpolator1D yInterpolator, final Interpolator1D xExtrapolator, final Interpolator1D yExtrapolator) {
    Validate.notNull(xInterpolator);
    Validate.notNull(yInterpolator);
    _xInterpolator = new CombinedInterpolatorExtrapolator(xInterpolator, xExtrapolator);
    _yInterpolator = new CombinedInterpolatorExtrapolator(yInterpolator, yExtrapolator);
    _comparator = FirstThenSecondPairComparator.INSTANCE_DOUBLES;
  }

  public Map<Double, Interpolator1DDataBundle> getDataBundle(final Map<DoublesPair, Double> data) {
    Validate.notNull(data, "data");
    return testData(data);
  }

  @Override
  public Double interpolate(final Map<Double, Interpolator1DDataBundle> dataBundle, final DoublesPair value) {
    Validate.notNull(value);
    Validate.notNull(dataBundle, "data bundle");
    final Map<Double, Double> xData = new HashMap<Double, Double>();
    for (final Map.Entry<Double, Interpolator1DDataBundle> entry : dataBundle.entrySet()) {
      xData.put(entry.getKey(), _yInterpolator.interpolate(entry.getValue(), value.getSecond()));
    }
    return _xInterpolator.interpolate(_xInterpolator.getDataBundle(xData), value.getKey());
  }

  @Override
  public Map<DoublesPair, Double> getNodeSensitivitiesForValue(final Map<Double, Interpolator1DDataBundle> dataBundle, final DoublesPair value) {
    Validate.notNull(value);
    Validate.notNull(dataBundle, "data bundle");
    final Map<Double, Double> xData = new HashMap<Double, Double>();
    double[][] temp = new double[dataBundle.size()][];
    int i = 0;
    for (final Map.Entry<Double, Interpolator1DDataBundle> entry : dataBundle.entrySet()) {
      //this is the sensitivity of the point projected onto a column of y-points to those points 
      temp[i++] = _yInterpolator.getNodeSensitivitiesForValue(entry.getValue(), value.getSecond());
      xData.put(entry.getKey(), _yInterpolator.interpolate(entry.getValue(), value.getSecond()));
    }
    //this is the sensitivity of the point to the points projected onto y columns 
    double[] xSense = _xInterpolator.getNodeSensitivitiesForValue(_xInterpolator.getDataBundle(xData), value.getKey());
    Validate.isTrue(xSense.length == dataBundle.size());
    Map<DoublesPair, Double> res = new HashMap<DoublesPair, Double>();

    double sense;
    i = 0;
    int j = 0;
    for (final Map.Entry<Double, Interpolator1DDataBundle> entry : dataBundle.entrySet()) {
      double[] yValues = entry.getValue().getKeys();
      for (j = 0; j < yValues.length; j++) {
        sense = xSense[i] * temp[i][j];
        res.put(new DoublesPair(entry.getKey(), yValues[j]), sense);
      }
      i++;
    }

    return res;
  }
  private Map<Double, Interpolator1DDataBundle> testData(final Map<DoublesPair, Double> data) {
    final Map<Double, Interpolator1DDataBundle> result = new TreeMap<Double, Interpolator1DDataBundle>();
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
        final Interpolator1DDataBundle interpolatorData = _yInterpolator.getDataBundle(yzValues);
        result.put(x, interpolatorData);
        yzValues = new TreeMap<Double, Double>();
        yzValues.put(nextEntry.getKey().second, nextEntry.getValue());
        x = newX;
      } else {
        yzValues.put(nextEntry.getKey().second, nextEntry.getValue());
      }
      if (!iterator.hasNext()) {
        yzValues.put(nextEntry.getKey().second, nextEntry.getValue());
        final Interpolator1DDataBundle interpolatorData = _yInterpolator.getDataBundle(yzValues);
        result.put(x, interpolatorData);
      }
    }
    return result;
  }

  public Interpolator1D getXInterpolator() {
    return _xInterpolator;
  }

  public Interpolator1D getYInterpolator() {
    return _yInterpolator;
  }

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
