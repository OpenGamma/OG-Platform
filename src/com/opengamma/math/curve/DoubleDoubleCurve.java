/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.math.ParallelArrayBinarySort;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
//TODO test for distinctness of nodes?
public abstract class DoubleDoubleCurve extends AbstractDoubleDoubleCurve {
  private int _n;
  private final double[] _xData;
  private final double[] _yData;

  public DoubleDoubleCurve(final double[] xData, final double[] yData) {
    this(xData, yData, false);
  }

  public DoubleDoubleCurve(final Double[] xData, final Double[] yData) {
    this(xData, yData, false);
  }

  public DoubleDoubleCurve(final Map<Double, Double> data) {
    this(data, false);
  }

  public DoubleDoubleCurve(final DoublesPair[] data) {
    this(data, false);
  }

  public DoubleDoubleCurve(final Set<DoublesPair> data) {
    this(data, false);
  }

  public DoubleDoubleCurve(final double[] xData, final double[] yData, final String name) {
    this(xData, yData, false, name);
  }

  public DoubleDoubleCurve(final Double[] xData, final Double[] yData, final String name) {
    this(xData, yData, false, name);
  }

  public DoubleDoubleCurve(final Map<Double, Double> data, final String name) {
    this(data, false, name);
  }

  public DoubleDoubleCurve(final DoublesPair[] data, final String name) {
    this(data, false, name);
  }

  public DoubleDoubleCurve(final Set<DoublesPair> data, final String name) {
    this(data, false, name);
  }

  public DoubleDoubleCurve(final double[] xData, final double[] yData, final boolean isSorted) {
    super();
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.isTrue(xData.length == yData.length);
    _n = xData.length;
    _xData = Arrays.copyOf(xData, _n);
    _yData = Arrays.copyOf(yData, _n);
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xData, yData);
    }
  }

  public DoubleDoubleCurve(final Double[] xData, final Double[] yData, final boolean isSorted) {
    super();
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.isTrue(xData.length == yData.length);
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      Validate.notNull(xData[i], "element " + i + " of x data");
      Validate.notNull(yData[i], "element " + i + " of y data");
      _xData[i] = xData[i];
      _yData[i] = yData[i];
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  public DoubleDoubleCurve(final Map<Double, Double> data, final boolean isSorted) {
    super();
    Validate.notNull(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    final int i = 0;
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      Validate.notNull(entry.getKey(), "element " + i + " of x data");
      Validate.notNull(entry.getValue(), "element " + i + " of y data");
      _xData[i] = entry.getKey();
      _yData[i] = entry.getValue();
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  public DoubleDoubleCurve(final DoublesPair[] data, final boolean isSorted) {
    super();
    Validate.notNull(data, "data");
    _n = data.length;
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      Validate.notNull(data[i], "element " + i + " of data");
      _xData[i] = data[i].first;
      _yData[i] = data[i].second;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  public DoubleDoubleCurve(final Set<DoublesPair> data, final boolean isSorted) {
    super();
    Validate.notNull(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    final int i = 0;
    for (final DoublesPair entry : data) {
      Validate.notNull(entry, "element " + i + " of data");
      _xData[i] = entry.first;
      _yData[i] = entry.second;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  public DoubleDoubleCurve(final double[] xData, final double[] yData, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.isTrue(xData.length == yData.length);
    _n = xData.length;
    _xData = Arrays.copyOf(xData, _n);
    _yData = Arrays.copyOf(yData, _n);
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(xData, yData);
    }
  }

  public DoubleDoubleCurve(final Double[] xData, final Double[] yData, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.isTrue(xData.length == yData.length);
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      Validate.notNull(xData[i], "element " + i + " of x data");
      Validate.notNull(yData[i], "element " + i + " of y data");
      _xData[i] = xData[i];
      _yData[i] = yData[i];
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  public DoubleDoubleCurve(final Map<Double, Double> data, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    final int i = 0;
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      Validate.notNull(entry.getKey(), "element " + i + " of x data");
      Validate.notNull(entry.getValue(), "element " + i + " of y data");
      _xData[i] = entry.getKey();
      _yData[i] = entry.getValue();
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  public DoubleDoubleCurve(final DoublesPair[] data, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(data, "data");
    _n = data.length;
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      Validate.notNull(data[i], "element " + i + " of data");
      _xData[i] = data[i].first;
      _yData[i] = data[i].second;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  public DoubleDoubleCurve(final Set<DoublesPair> data, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    final int i = 0;
    for (final DoublesPair entry : data) {
      Validate.notNull(entry, "element " + i + " of data");
      _xData[i] = entry.first;
      _yData[i] = entry.second;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  //TODO not ideal
  @Override
  public Double[] getXData() {
    final Double[] result = new Double[_n];
    for (int i = 0; i < _n; i++) {
      result[i] = _xData[i];
    }
    return result;
  }

  //TODO not ideal
  @Override
  public Double[] getYData() {
    final Double[] result = new Double[_n];
    for (int i = 0; i < _n; i++) {
      result[i] = _yData[i];
    }
    return result;
  }

  public double[] getXDataAsPrimitive() {
    return _xData;
  }

  public double[] getYDataAsPrimitive() {
    return _yData;
  }

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
    final DoubleDoubleCurve other = (DoubleDoubleCurve) obj;
    return Arrays.equals(_xData, other._xData) && Arrays.equals(_yData, other._yData);
  }

}
