/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Triple;

/**
 * Parent class for a family of surfaces that have real <i>x</i>, <i>y</i> and <i>z</i> values. 
 */
public abstract class DoublesSurface extends Surface<Double, Double, Double> {
  private final int _n;
  private final double[] _xData;
  private final double[] _yData;
  private final double[] _zData;
  private Double[] _xDataObject;
  private Double[] _yDataObject;
  private Double[] _zDataObject;

  /**
   * @param xData An array of <i>x</i> data, not null
   * @param yData An array of <i>y</i> data, not null
   * @param zData An array of <i>z</i> data, not null
   */
  public DoublesSurface(final double[] xData, final double[] yData, final double[] zData) {
    super();
    ArgumentChecker.notNull(xData, "x data");
    ArgumentChecker.notNull(yData, "y data");
    ArgumentChecker.notNull(zData, "z data");
    ArgumentChecker.isTrue(xData.length == yData.length, "X data size different from Y data size");
    ArgumentChecker.isTrue(xData.length == zData.length, "X data size different from Z data size");
    _n = xData.length;
    _xData = Arrays.copyOf(xData, _n);
    _yData = Arrays.copyOf(yData, _n);
    _zData = Arrays.copyOf(zData, _n);
  }

  /**
   * @param xData An array of <i>x</i> data, not null, no null elements.
   * @param yData An array of <i>y</i> data, not null, no null elements. Must be the same length as the <i>x</i> data.
   * @param zData An array of <i>z</i> data, not null, no null elements. Must be the same length as the <i>x</i> data.
   */
  public DoublesSurface(final Double[] xData, final Double[] yData, final Double[] zData) {
    super();
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    Validate.isTrue(xData.length == yData.length);
    Validate.isTrue(xData.length == zData.length);
    _n = xData.length;
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    for (int i = 0; i < _n; i++) {
      Validate.notNull(xData[i]);
      Validate.notNull(yData[i]);
      Validate.notNull(zData[i]);
      _xData[i] = xData[i];
      _yData[i] = yData[i];
      _zData[i] = zData[i];
    }
  }

  /**
   * @param xData A list of <i>x</i> data, not null, no null elements.
   * @param yData A list of <i>y</i> data, not null, no null elements. Must be the same length as the <i>x</i> data.
   * @param zData A list of <i>z</i> data, not null, no null elements. Must be the same length as the <i>x</i> data.
   */
  public DoublesSurface(final List<Double> xData, final List<Double> yData, final List<Double> zData) {
    super();
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    Validate.isTrue(xData.size() == yData.size());
    Validate.isTrue(xData.size() == zData.size());
    _n = xData.size();
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    for (int i = 0; i < _n; i++) {
      final Double x = xData.get(i);
      final Double y = yData.get(i);
      final Double z = zData.get(i);
      Validate.notNull(x);
      Validate.notNull(y);
      Validate.notNull(z);
      _xData[i] = x;
      _yData[i] = y;
      _zData[i] = z;
    }
  }

  /**
   * @param xyData An array of <i>x-y</i> data, not null, no null elements.
   * @param zData An array of <i>z</i> data, not null. Must be the same length as the <i>x-y</i> data.
   */
  public DoublesSurface(final DoublesPair[] xyData, final double[] zData) {
    super();
    Validate.notNull(xyData, "x-y data");
    Validate.notNull(zData, "z data");
    Validate.isTrue(xyData.length == zData.length);
    _n = xyData.length;
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = Arrays.copyOf(zData, _n);
    for (int i = 0; i < _n; i++) {
      final DoublesPair pair = xyData[i];
      Validate.notNull(pair);
      Validate.notNull(zData[i]);
      _xData[i] = pair.first;
      _yData[i] = pair.second;
    }
  }

  /**
   * @param xyData An array of <i>x-y</i> data, not null, no null elements.
   * @param zData An array of <i>z</i> data, not null. Must be the same length as the <i>x-y</i> data.
   */
  public DoublesSurface(final DoublesPair[] xyData, final Double[] zData) {
    super();
    Validate.notNull(xyData, "x-y data");
    Validate.notNull(zData, "z data");
    Validate.isTrue(xyData.length == zData.length);
    _n = xyData.length;
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    for (int i = 0; i < _n; i++) {
      final DoublesPair pair = xyData[i];
      Validate.notNull(pair);
      Validate.notNull(zData[i]);
      _xData[i] = pair.first;
      _yData[i] = pair.second;
      _zData[i] = zData[i];
    }
  }

  /**
   * @param xyData A list of <i>x-y</i> data, not null, no null elements.
   * @param zData A list of <i>z</i> data, not null. Must be the same length as the <i>x-y</i> data.
   */
  public DoublesSurface(final List<DoublesPair> xyData, final List<Double> zData) {
    super();
    Validate.notNull(xyData, "x-y data");
    Validate.notNull(zData, "z data");
    Validate.isTrue(xyData.size() == zData.size());
    _n = xyData.size();
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    for (int i = 0; i < _n; i++) {
      final DoublesPair pair = xyData.get(i);
      final Double z = zData.get(i);
      Validate.notNull(pair);
      Validate.notNull(z);
      _xData[i] = pair.first;
      _yData[i] = pair.second;
      _zData[i] = z;
    }
  }

  /**
   * @param xyzData A map of <i>x-y</i> data to <i>z</i> data, not null, no null elements.
   */
  public DoublesSurface(final Map<DoublesPair, Double> xyzData) {
    super();
    Validate.notNull(xyzData, "x-y-z data");
    _n = xyzData.size();
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    int i = 0;
    for (final Map.Entry<DoublesPair, Double> entry : xyzData.entrySet()) {
      Validate.notNull(entry.getKey());
      Validate.notNull(entry.getValue());
      _xData[i] = entry.getKey().first;
      _yData[i] = entry.getKey().second;
      _zData[i++] = entry.getValue();
    }
  }

  /**
   * @param xyzData A list of <i>x-y-z</i> data, not null, no null elements.
   */
  public DoublesSurface(final List<Triple<Double, Double, Double>> xyzData) {
    super();
    Validate.notNull(xyzData, "x-y-z data");
    _n = xyzData.size();
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    int i = 0;
    for (final Triple<Double, Double, Double> entry : xyzData) {
      Validate.notNull(entry);
      final double x = entry.getFirst();
      final double y = entry.getSecond();
      final double z = entry.getThird();
      _xData[i] = x;
      _yData[i] = y;
      _zData[i] = z;
      i++;
    }
  }

  /**
   * @param xData An array of <i>x</i> data, not null
   * @param yData An array of <i>y</i> data, not null
   * @param zData An array of <i>z</i> data, not null
   * @param name The surface name
   */
  public DoublesSurface(final double[] xData, final double[] yData, final double[] zData, final String name) {
    super(name);
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    Validate.isTrue(xData.length == yData.length);
    Validate.isTrue(xData.length == zData.length);
    _n = xData.length;
    _xData = Arrays.copyOf(xData, _n);
    _yData = Arrays.copyOf(yData, _n);
    _zData = Arrays.copyOf(zData, _n);
  }

  /**
   * @param xData An array of <i>x</i> data, not null, no null elements.
   * @param yData An array of <i>y</i> data, not null, no null elements. Must be the same length as the <i>x</i> data.
   * @param zData An array of <i>z</i> data, not null, no null elements. Must be the same length as the <i>x</i> data.
   * @param name The surface name
   */
  public DoublesSurface(final Double[] xData, final Double[] yData, final Double[] zData, final String name) {
    super(name);
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    Validate.isTrue(xData.length == yData.length);
    Validate.isTrue(xData.length == zData.length);
    _n = xData.length;
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    for (int i = 0; i < _n; i++) {
      Validate.notNull(xData[i]);
      Validate.notNull(yData[i]);
      Validate.notNull(zData[i]);
      _xData[i] = xData[i];
      _yData[i] = yData[i];
      _zData[i] = zData[i];
    }
  }

  /**
   * @param xData A list of <i>x</i> data, not null, no null elements.
   * @param yData A list of <i>y</i> data, not null, no null elements. Must be the same length as the <i>x</i> data.
   * @param zData A list of <i>z</i> data, not null, no null elements. Must be the same length as the <i>x</i> data.
   * @param name The surface name
   */
  public DoublesSurface(final List<Double> xData, final List<Double> yData, final List<Double> zData, final String name) {
    super(name);
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    Validate.isTrue(xData.size() == yData.size());
    Validate.isTrue(xData.size() == zData.size());
    _n = xData.size();
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    for (int i = 0; i < _n; i++) {
      final Double x = xData.get(i);
      final Double y = yData.get(i);
      final Double z = zData.get(i);
      Validate.notNull(x);
      Validate.notNull(y);
      Validate.notNull(z);
      _xData[i] = x;
      _yData[i] = y;
      _zData[i] = z;
    }
  }

  /**
   * @param xyData An array of <i>x-y</i> data, not null, no null elements.
   * @param zData An array of <i>z</i> data, not null. Must be the same length as the <i>x-y</i> data.
   * @param name The surface name
   */
  public DoublesSurface(final DoublesPair[] xyData, final double[] zData, final String name) {
    super(name);
    Validate.notNull(xyData, "x-y data");
    Validate.notNull(zData, "z data");
    Validate.isTrue(xyData.length == zData.length);
    _n = xyData.length;
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = Arrays.copyOf(zData, _n);
    for (int i = 0; i < _n; i++) {
      final DoublesPair pair = xyData[i];
      Validate.notNull(pair);
      Validate.notNull(zData[i]);
      _xData[i] = pair.first;
      _yData[i] = pair.second;
    }
  }

  /**
   * @param xyData An array of <i>x-y</i> data, not null, no null elements.
   * @param zData An array of <i>z</i> data, not null. Must be the same length as the <i>x-y</i> data.
   * @param name The surface name
   */
  public DoublesSurface(final DoublesPair[] xyData, final Double[] zData, final String name) {
    super(name);
    Validate.notNull(xyData, "x-y data");
    Validate.notNull(zData, "z data");
    Validate.isTrue(xyData.length == zData.length);
    _n = xyData.length;
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    for (int i = 0; i < _n; i++) {
      final DoublesPair pair = xyData[i];
      Validate.notNull(pair);
      Validate.notNull(zData[i]);
      _xData[i] = pair.first;
      _yData[i] = pair.second;
      _zData[i] = zData[i];
    }
  }

  /**
   * @param xyData A list of <i>x-y</i> data, not null, no null elements.
   * @param zData A list of <i>z</i> data, not null. Must be the same length as the <i>x-y</i> data.
   * @param name The surface name
   */
  public DoublesSurface(final List<DoublesPair> xyData, final List<Double> zData, final String name) {
    super(name);
    Validate.notNull(xyData, "x-y data");
    Validate.notNull(zData, "z data");
    Validate.isTrue(xyData.size() == zData.size());
    _n = xyData.size();
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    for (int i = 0; i < _n; i++) {
      final DoublesPair pair = xyData.get(i);
      final Double z = zData.get(i);
      Validate.notNull(pair);
      Validate.notNull(z);
      _xData[i] = pair.first;
      _yData[i] = pair.second;
      _zData[i] = z;
    }
  }

  /**
   * @param xyzData A map of <i>x-y</i> data to <i>z</i> data, not null, no null elements.
   * @param name The surface name
   */
  public DoublesSurface(final Map<DoublesPair, Double> xyzData, final String name) {
    super(name);
    Validate.notNull(xyzData, "x-y-z data");
    _n = xyzData.size();
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    int i = 0;
    for (final Map.Entry<DoublesPair, Double> entry : xyzData.entrySet()) {
      Validate.notNull(entry.getKey());
      Validate.notNull(entry.getValue());
      _xData[i] = entry.getKey().first;
      _yData[i] = entry.getKey().second;
      _zData[i++] = entry.getValue();
    }
  }

  /**
   * @param xyzData A list of <i>x-y-z</i> data, not null, no null elements.
   * @param name The surface name
   */
  public DoublesSurface(final List<Triple<Double, Double, Double>> xyzData, final String name) {
    super(name);
    Validate.notNull(xyzData, "x-y-z data");
    _n = xyzData.size();
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    int i = 0;
    for (final Triple<Double, Double, Double> entry : xyzData) {
      Validate.notNull(entry);
      final double x = entry.getFirst();
      final double y = entry.getSecond();
      final double z = entry.getThird();
      _xData[i] = x;
      _yData[i] = y;
      _zData[i] = z;
      i++;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double[] getXData() {
    if (_xDataObject != null) {
      return _xDataObject;
    }
    _xDataObject = new Double[_n];
    for (int i = 0; i < _n; i++) {
      _xDataObject[i] = _xData[i];
    }
    return _xDataObject;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double[] getYData() {
    if (_yDataObject != null) {
      return _yDataObject;
    }
    _yDataObject = new Double[_n];
    for (int i = 0; i < _n; i++) {
      _yDataObject[i] = _yData[i];
    }
    return _yDataObject;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double[] getZData() {
    if (_zDataObject != null) {
      return _zDataObject;
    }
    _zDataObject = new Double[_n];
    for (int i = 0; i < _n; i++) {
      _zDataObject[i] = _zData[i];
    }
    return _zDataObject;
  }

  /**
   * @return The <i>x</i> data as an array of primitives
   */
  public double[] getXDataAsPrimitive() {
    return _xData;
  }

  /**
   * @return The <i>y</i> data as an array of primitives
   */
  public double[] getYDataAsPrimitive() {
    return _yData;
  }

  /**
   * @return The <i>z</i> data as an array of primitives
   */
  public double[] getZDataAsPrimitive() {
    return _zData;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size() {
    return _n;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_xData);
    result = prime * result + Arrays.hashCode(_yData);
    result = prime * result + Arrays.hashCode(_zData);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DoublesSurface other = (DoublesSurface) obj;
    if (!Arrays.equals(_xData, other._xData)) {
      return false;
    }
    if (!Arrays.equals(_yData, other._yData)) {
      return false;
    }
    if (!Arrays.equals(_zData, other._zData)) {
      return false;
    }
    return true;
  }

}
