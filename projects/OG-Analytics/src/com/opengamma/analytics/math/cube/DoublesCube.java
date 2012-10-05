/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * Parent class for a family of curves that have real <i>(x, y, z, value)</i> points.  
 */
public abstract class DoublesCube extends Cube<Double, Double, Double, Double> {
  private final int _n;
  private final double[] _xData;
  private final double[] _yData;
  private final double[] _zData;
  private final double[] _values;
  private Double[] _xDataObject;
  private Double[] _yDataObject;
  private Double[] _zDataObject;
  private Double[] _valuesObject;

  /**
   * @param xData An array of <i>x</i> data, not null
   * @param yData An array of <i>y</i> data, not null, must be the same length as the <i>x</i> data array
   * @param zData An array of <i>z</i> data, not null, must be the same length as the <i>x</i> data array
   * @param values An array of <i>values</i> , not null, must be the same length as the <i>x</i> data array
   */
  public DoublesCube(final double[] xData, final double[] yData, final double[] zData, final double[] values) {
    super();
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    Validate.notNull(values, "values");
    _n = xData.length;
    Validate.isTrue(_n == yData.length);
    Validate.isTrue(_n == zData.length);
    Validate.isTrue(_n == values.length);
    _xData = Arrays.copyOf(xData, _n);
    _yData = Arrays.copyOf(yData, _n);
    _zData = Arrays.copyOf(zData, _n);
    _values = Arrays.copyOf(values, _n);
  }

  /**
   * @param xData An array of <i>x</i> data, not null
   * @param yData An array of <i>y</i> data, not null, must be the same length as the <i>x</i> data array
   * @param zData An array of <i>z</i> data, not null, must be the same length as the <i>x</i> data array
   * @param values An array of <i>values</i> , not null, must be the same length as the <i>x</i> data array
   */
  public DoublesCube(final Double[] xData, final Double[] yData, final Double[] zData, final Double[] values) {
    super();
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    Validate.notNull(values, "data");
    _n = xData.length;
    Validate.isTrue(_n == yData.length);
    Validate.isTrue(_n == zData.length);
    Validate.isTrue(_n == values.length);
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    _values = new double[_n];
    for (int i = 0; i < _n; i++) {
      Validate.notNull(xData[i], "element " + i + " of x data");
      Validate.notNull(yData[i], "element " + i + " of y data");
      Validate.notNull(zData[i], "element " + i + " of z data");
      Validate.notNull(values[i], "element " + i + " of data");
      _xData[i] = xData[i];
      _yData[i] = yData[i];
      _zData[i] = zData[i];
      _values[i] = values[i];
    }
  }

  /**
   * @param xData A list of <i>x</i> data, not null
   * @param yData A list of <i>y</i> data, not null, must be the same length as the <i>x</i> data list
   * @param zData A list of <i>z</i> data, not null, must be the same length as the <i>x</i> data list
   * @param values A list of <i>values</i> , not null, must be the same length as the <i>x</i> data list
   */
  public DoublesCube(final List<Double> xData, final List<Double> yData, final List<Double> zData, final List<Double> values) {
    super();
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    Validate.notNull(values, "data");
    _n = xData.size();
    Validate.isTrue(_n == yData.size());
    Validate.isTrue(_n == zData.size());
    Validate.isTrue(_n == values.size());
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    _values = new double[_n];
    for (int i = 0; i < _n; i++) {
      final Double x = xData.get(i);
      final Double y = yData.get(i);
      final Double z = zData.get(i);
      final Double value = values.get(i);
      Validate.notNull(x, "element " + i + " of x data");
      Validate.notNull(y, "element " + i + " of y data");
      Validate.notNull(z, "element " + i + " of z data");
      Validate.notNull(value, "element " + i + " of data");
      _xData[i] = x;
      _yData[i] = y;
      _zData[i] = z;
      _values[i] = value;
    }
  }

  /**
   * @param xData An array of <i>x</i> data, not null
   * @param yData An array of <i>y</i> data, not null, must be the same length as the <i>x</i> data array
   * @param zData An array of <i>z</i> data, not null, must be the same length as the <i>x</i> data array
   * @param values An array of <i>values</i> , not null, must be the same length as the <i>x</i> data array
   * @param name The name of the cube
   */
  public DoublesCube(final double[] xData, final double[] yData, final double[] zData, final double[] values, final String name) {
    super(name);
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    Validate.notNull(values, "data");
    _n = xData.length;
    Validate.isTrue(_n == yData.length);
    Validate.isTrue(_n == zData.length);
    Validate.isTrue(_n == values.length);
    _xData = Arrays.copyOf(xData, _n);
    _yData = Arrays.copyOf(yData, _n);
    _zData = Arrays.copyOf(zData, _n);
    _values = Arrays.copyOf(values, _n);
  }

  /**
   * @param xData An array of <i>x</i> data, not null
   * @param yData An array of <i>y</i> data, not null, must be the same length as the <i>x</i> data array
   * @param zData An array of <i>z</i> data, not null, must be the same length as the <i>x</i> data array
   * @param values An array of <i>values</i> , not null, must be the same length as the <i>x</i> data array
   * @param name The name of the cube
   */
  public DoublesCube(final Double[] xData, final Double[] yData, final Double[] zData, final Double[] values, final String name) {
    super(name);
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    Validate.notNull(values, "data");
    _n = xData.length;
    Validate.isTrue(_n == yData.length);
    Validate.isTrue(_n == zData.length);
    Validate.isTrue(_n == values.length);
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    _values = new double[_n];
    for (int i = 0; i < _n; i++) {
      Validate.notNull(xData[i], "element " + i + " of x data");
      Validate.notNull(yData[i], "element " + i + " of y data");
      Validate.notNull(zData[i], "element " + i + " of z data");
      Validate.notNull(values[i], "element " + i + " of data");
      _xData[i] = xData[i];
      _yData[i] = yData[i];
      _zData[i] = zData[i];
      _values[i] = values[i];
    }
  }

  /**
   * @param xData A list of <i>x</i> data, not null
   * @param yData A list of <i>y</i> data, not null, must be the same length as the <i>x</i> data list
   * @param zData A list of <i>z</i> data, not null, must be the same length as the <i>x</i> data list
   * @param values A list of <i>values</i> , not null, must be the same length as the <i>x</i> data list
   * @param name The name of the cube
   */
  public DoublesCube(final List<Double> xData, final List<Double> yData, final List<Double> zData, final List<Double> values, final String name) {
    super(name);
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    Validate.notNull(values, "data");
    _n = xData.size();
    Validate.isTrue(_n == yData.size());
    Validate.isTrue(_n == zData.size());
    Validate.isTrue(_n == values.size());
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    _values = new double[_n];
    for (int i = 0; i < _n; i++) {
      final Double x = xData.get(i);
      final Double y = yData.get(i);
      final Double z = zData.get(i);
      final Double value = values.get(i);
      Validate.notNull(x, "element " + i + " of x data");
      Validate.notNull(y, "element " + i + " of y data");
      Validate.notNull(z, "element " + i + " of z data");
      Validate.notNull(value, "element " + i + " of data");
      _xData[i] = x;
      _yData[i] = y;
      _zData[i] = z;
      _values[i] = value;
    }
  }

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

  @Override
  public Double[] getValues() {
    if (_valuesObject != null) {
      return _valuesObject;
    }
    _valuesObject = new Double[_n];
    for (int i = 0; i < _n; i++) {
      _valuesObject[i] = _values[i];
    }
    return _valuesObject;
  }

  /**
   * Returns the <i>x</i> data points as a primitive array
   * @return The <i>x</i> data 
   */
  public double[] getXDataAsPrimitive() {
    return _xData;
  }

  /**
   * Returns the <i>y</i> data points as a primitive array
   * @return The <i>y</i> data 
   */
  public double[] getYDataAsPrimitive() {
    return _yData;
  }

  /**
   * Returns the <i>z</i> data points as a primitive array
   * @return The <i>z</i> data 
   */
  public double[] getZDataAsPrimitive() {
    return _zData;
  }

  /**
   * Returns the values as a primitive array
   * @return The values
   */
  public double[] getValuesAsPrimitive() {
    return _values;
  }

  @Override
  public int size() {
    return _n;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_values);
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
    final DoublesCube other = (DoublesCube) obj;
    if (!Arrays.equals(_values, other._values)) {
      return false;
    }
    if (!Arrays.equals(_xData, other._xData)) {
      return false;
    }
    if (!Arrays.equals(_yData, other._yData)) {
      return false;
    }
    return Arrays.equals(_zData, other._zData);
  }

}
