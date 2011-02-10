/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.cube;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public abstract class DoublesCube extends Cube<Double, Double, Double, Double> {
  private final int _n;
  private final double[] _xData;
  private final double[] _yData;
  private final double[] _zData;
  private final double[] _data;
  private Double[] _xDataObject;
  private Double[] _yDataObject;
  private Double[] _zDataObject;
  private Double[] _dataObject;

  public DoublesCube(final double[] xData, final double[] yData, final double[] zData, double[] data) {
    super();
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    Validate.notNull(data, "data");
    _n = xData.length;
    Validate.isTrue(_n == yData.length);
    Validate.isTrue(_n == zData.length);
    Validate.isTrue(_n == data.length);
    _xData = Arrays.copyOf(xData, _n);
    _yData = Arrays.copyOf(yData, _n);
    _zData = Arrays.copyOf(zData, _n);
    _data = Arrays.copyOf(data, _n);
  }

  public DoublesCube(final Double[] xData, final Double[] yData, final Double[] zData, Double[] data) {
    super();
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    Validate.notNull(data, "data");
    _n = xData.length;
    Validate.isTrue(_n == yData.length);
    Validate.isTrue(_n == zData.length);
    Validate.isTrue(_n == data.length);
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    _data = new double[_n];
    for (int i = 0; i < _n; i++) {
      Validate.notNull(xData[i], "element " + i + " of x data");
      Validate.notNull(yData[i], "element " + i + " of y data");
      Validate.notNull(zData[i], "element " + i + " of z data");
      Validate.notNull(data[i], "element " + i + " of data");
      _xData[i] = xData[i];
      _yData[i] = yData[i];
      _zData[i] = zData[i];
      _data[i] = data[i];
    }
  }

  public DoublesCube(final List<Double> xData, final List<Double> yData, final List<Double> zData, List<Double> data) {
    super();
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    Validate.notNull(data, "data");
    _n = xData.size();
    Validate.isTrue(_n == yData.size());
    Validate.isTrue(_n == zData.size());
    Validate.isTrue(_n == data.size());
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    _data = new double[_n];
    for (int i = 0; i < _n; i++) {
      final Double x = xData.get(i);
      final Double y = yData.get(i);
      final Double z = zData.get(i);
      final Double value = data.get(i);
      Validate.notNull(x, "element " + i + " of x data");
      Validate.notNull(y, "element " + i + " of y data");
      Validate.notNull(z, "element " + i + " of z data");
      Validate.notNull(value, "element " + i + " of data");
      _xData[i] = x;
      _yData[i] = y;
      _zData[i] = z;
      _data[i] = value;
    }
  }

  public DoublesCube(final double[] xData, final double[] yData, final double[] zData, double[] data, String name) {
    super(name);
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    Validate.notNull(data, "data");
    _n = xData.length;
    Validate.isTrue(_n == yData.length);
    Validate.isTrue(_n == zData.length);
    Validate.isTrue(_n == data.length);
    _xData = Arrays.copyOf(xData, _n);
    _yData = Arrays.copyOf(yData, _n);
    _zData = Arrays.copyOf(zData, _n);
    _data = Arrays.copyOf(data, _n);
  }

  public DoublesCube(final Double[] xData, final Double[] yData, final Double[] zData, Double[] data, String name) {
    super(name);
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    Validate.notNull(data, "data");
    _n = xData.length;
    Validate.isTrue(_n == yData.length);
    Validate.isTrue(_n == zData.length);
    Validate.isTrue(_n == data.length);
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    _data = new double[_n];
    for (int i = 0; i < _n; i++) {
      Validate.notNull(xData[i], "element " + i + " of x data");
      Validate.notNull(yData[i], "element " + i + " of y data");
      Validate.notNull(zData[i], "element " + i + " of z data");
      Validate.notNull(data[i], "element " + i + " of data");
      _xData[i] = xData[i];
      _yData[i] = yData[i];
      _zData[i] = zData[i];
      _data[i] = data[i];
    }
  }

  public DoublesCube(final List<Double> xData, final List<Double> yData, final List<Double> zData, List<Double> data, String name) {
    super(name);
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.notNull(zData, "z data");
    Validate.notNull(data, "data");
    _n = xData.size();
    Validate.isTrue(_n == yData.size());
    Validate.isTrue(_n == zData.size());
    Validate.isTrue(_n == data.size());
    _xData = new double[_n];
    _yData = new double[_n];
    _zData = new double[_n];
    _data = new double[_n];
    for (int i = 0; i < _n; i++) {
      final Double x = xData.get(i);
      final Double y = yData.get(i);
      final Double z = zData.get(i);
      final Double value = data.get(i);
      Validate.notNull(x, "element " + i + " of x data");
      Validate.notNull(y, "element " + i + " of y data");
      Validate.notNull(z, "element " + i + " of z data");
      Validate.notNull(value, "element " + i + " of data");
      _xData[i] = x;
      _yData[i] = y;
      _zData[i] = z;
      _data[i] = value;
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
  public Double[] getData() {
    if (_dataObject != null) {
      return _dataObject;
    }
    _dataObject = new Double[_n];
    for (int i = 0; i < _n; i++) {
      _dataObject[i] = _data[i];
    }
    return _dataObject;
  }

  public double[] getXDataAsPrimitive() {
    return _xData;
  }

  public double[] getYDataAsPrimitive() {
    return _yData;
  }

  public double[] getZDataAsPrimitive() {
    return _zData;
  }

  public double[] getDataAsPrimitive() {
    return _data;
  }

  @Override
  public int size() {
    return _n;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_data);
    result = prime * result + Arrays.hashCode(_xData);
    result = prime * result + Arrays.hashCode(_yData);
    result = prime * result + Arrays.hashCode(_zData);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DoublesCube other = (DoublesCube) obj;
    if (!Arrays.equals(_data, other._data)) {
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
