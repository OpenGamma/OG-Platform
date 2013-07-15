/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;
import com.opengamma.util.tuple.DoublesPair;

/** 
 * Parent class for a family of curves where the data is stored as arrays.
 * It is possible to construct a curve using either unsorted (in <i>x</i>) data or sorted (ascending in <i>x</i>). 
 * Note that if the constructor is told that unsorted data are sorted then no sorting will take place, which will give unpredictable results.
 * 
 */
public abstract class ArraysDoublesCurve extends DoublesCurve {

  private final int _n;
  private final double[] _xData;
  private final double[] _yData;
  private Double[] _xDataObject;
  private Double[] _yDataObject;

  /**
    * @param xData An array of <i>x</i> data, not null
    * @param yData An array of <i>y</i> data, not null, contains same number of entries as <i>x</i>
    * @param isSorted Is the <i>x</i>-data sorted
    */

  public ArraysDoublesCurve(final double[] xData, final double[] yData, final boolean isSorted) {
    super();
    ArgumentChecker.notNull(xData, "x data");
    ArgumentChecker.notNull(yData, "y data");
    ArgumentChecker.isTrue(xData.length == yData.length, "x data size {} must be equal to y data size {}", xData.length, yData.length);
    _n = xData.length;
    _xData = Arrays.copyOf(xData, _n);
    _yData = Arrays.copyOf(yData, _n);
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param xData An array of <i>x</i> data, not null
    * @param yData An array of <i>y</i> data, not null, contains same number of entries as <i>x</i>
    * @param isSorted Is the <i>x</i>-data sorted
    */

  public ArraysDoublesCurve(final Double[] xData, final Double[] yData, final boolean isSorted) {
    super();
    ArgumentChecker.notNull(xData, "x data");
    ArgumentChecker.notNull(yData, "y data");
    _n = xData.length;
    ArgumentChecker.isTrue(xData.length == yData.length, "x data size {} must be equal to y data size {}", xData.length, yData.length);
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      Double x = xData[i];
      Double y = yData[i];
      ArgumentChecker.notNull(x, "x");
      ArgumentChecker.notNull(y, "y");
      _xData[i] = x;
      _yData[i] = y;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    *
    * @param data A map of <i>x-y</i> data, not null
    * @param isSorted Is the <i>x</i>-data sorted
    */
  public ArraysDoublesCurve(final Map<Double, Double> data, final boolean isSorted) {
    super();
    ArgumentChecker.notNull(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    int i = 0;
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      Double x = entry.getKey();
      Double y = entry.getValue();
      ArgumentChecker.notNull(x, "x");
      ArgumentChecker.notNull(y, "y");
      _xData[i] = x;
      _yData[i++] = y;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param data An array of <i>x-y</i> pairs, not null
    * @param isSorted Is the <i>x</i>-data sorted
    */
  public ArraysDoublesCurve(final DoublesPair[] data, final boolean isSorted) {
    super();
    ArgumentChecker.notNull(data, "data");
    _n = data.length;
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      DoublesPair pair = data[i];
      ArgumentChecker.notNull(pair, "pair");
      _xData[i] = pair.first;
      _yData[i] = pair.second;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param data A set of <i>x-y</i> pairs, not null
    * @param isSorted Is the <i>x</i>-data sorted
    */
  public ArraysDoublesCurve(final Set<DoublesPair> data, final boolean isSorted) {
    super();
    ArgumentChecker.notNull(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    int i = 0;
    for (final DoublesPair entry : data) {
      ArgumentChecker.notNull(entry, "entry");
      _xData[i] = entry.first;
      _yData[i++] = entry.second;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param xData A list of <i>x</i> data points, assumed to be sorted ascending, not null
    * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
    * @param isSorted Is the <i>x</i>-data sorted
    */
  public ArraysDoublesCurve(final List<Double> xData, final List<Double> yData, final boolean isSorted) {
    super();
    ArgumentChecker.notNull(xData, "x data");
    ArgumentChecker.notNull(yData, "y data");
    ArgumentChecker.isTrue(xData.size() == yData.size(), "x data size {} must be equal to y data size {}", xData.size(), yData.size());
    _n = xData.size();
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      Double x = xData.get(i);
      Double y = yData.get(i);
      ArgumentChecker.notNull(x, "x");
      ArgumentChecker.notNull(y, "y");
      _xData[i] = x;
      _yData[i] = y;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param data A list of <i>x-y</i> data points, assumed to be sorted ascending, not null
    * @param isSorted Is the <i>x</i>-data sorted
    */
  public ArraysDoublesCurve(final List<DoublesPair> data, final boolean isSorted) {
    super();
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.noNulls(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    int i = 0;
    for (final DoublesPair pair : data) {
      _xData[i] = pair.first;
      _yData[i++] = pair.second;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param xData An array of <i>x</i> data, not null
    * @param yData An array of <i>y</i> data, not null, contains same number of entries as <i>x</i>
    * @param isSorted Is the <i>x</i>-data sorted
    * @param name The name of the curve
    */
  public ArraysDoublesCurve(final double[] xData, final double[] yData, final boolean isSorted, final String name) {
    super(name);
    ArgumentChecker.notNull(xData, "x data");
    ArgumentChecker.notNull(yData, "y data");
    ArgumentChecker.isTrue(xData.length == yData.length, "x data size {} must be equal to y data size {}", xData.length, yData.length);
    _n = xData.length;
    _xData = Arrays.copyOf(xData, _n);
    _yData = Arrays.copyOf(yData, _n);
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param xData An array of <i>x</i> data, not null
    * @param yData An array of <i>y</i> data, not null, contains same number of entries as <i>x</i>
    * @param isSorted Is the <i>x</i>-data sorted
    * @param name The name of the curve
    */
  public ArraysDoublesCurve(final Double[] xData, final Double[] yData, final boolean isSorted, final String name) {
    super(name);
    ArgumentChecker.notNull(xData, "x data");
    _n = xData.length;
    ArgumentChecker.notNull(yData, "y data");
    ArgumentChecker.isTrue(xData.length == yData.length, "x data size {} must be equal to y data size {}", xData.length, yData.length);
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      ArgumentChecker.notNull(xData[i], "x");
      ArgumentChecker.notNull(yData[i], "y");
      _xData[i] = xData[i];
      _yData[i] = yData[i];
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param data A map of <i>x-y</i> data, not null
    * @param isSorted Is the <i>x</i>-data sorted
    * @param name The name of the curve
    */
  public ArraysDoublesCurve(final Map<Double, Double> data, final boolean isSorted, final String name) {
    super(name);
    ArgumentChecker.notNull(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    int i = 0;
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      ArgumentChecker.notNull(entry.getKey(), "x");
      ArgumentChecker.notNull(entry.getValue(), "y");
      _xData[i] = entry.getKey();
      _yData[i++] = entry.getValue();
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param data An array of <i>x-y</i> pairs, not null
    * @param isSorted Is the <i>x</i>-data sorted
    * @param name The name of the curve
    */
  public ArraysDoublesCurve(final DoublesPair[] data, final boolean isSorted, final String name) {
    super(name);
    ArgumentChecker.notNull(data, "data");
    _n = data.length;
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      ArgumentChecker.notNull(data[i], "entry");
      _xData[i] = data[i].first;
      _yData[i] = data[i].second;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param data A set of <i>x-y</i> pairs, not null
    * @param isSorted Is the <i>x</i>-data sorted
    * @param name The name of the curve
    */
  public ArraysDoublesCurve(final Set<DoublesPair> data, final boolean isSorted, final String name) {
    super(name);
    ArgumentChecker.notNull(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    int i = 0;
    for (final DoublesPair entry : data) {
      ArgumentChecker.notNull(entry, "entry");
      _xData[i] = entry.first;
      _yData[i++] = entry.second;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param xData A list of <i>x</i> data, not null
    * @param yData A list of <i>y</i> data, not null, contains same number of entries as <i>x</i>
    * @param isSorted Is the <i>x</i>-data sorted
    * @param name The name of the curve
    */
  public ArraysDoublesCurve(final List<Double> xData, final List<Double> yData, final boolean isSorted, final String name) {
    super(name);
    ArgumentChecker.notNull(xData, "x data");
    ArgumentChecker.notNull(yData, "y data");
    ArgumentChecker.isTrue(xData.size() == yData.size(), "x data size {} must be equal to y data size {}", xData.size(), yData.size());
    _n = xData.size();
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      ArgumentChecker.notNull(xData.get(i), "x");
      ArgumentChecker.notNull(yData.get(i), "y");
      _xData[i] = xData.get(i);
      _yData[i] = yData.get(i);
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param data A list of <i>x-y</i> pairs, not null
    * @param isSorted Is the <i>x</i>-data sorted
    * @param name The name of the curve
    */
  public ArraysDoublesCurve(final List<DoublesPair> data, final boolean isSorted, final String name) {
    super(name);
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.noNulls(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    int i = 0;
    for (final DoublesPair pair : data) {
      _xData[i] = pair.first;
      _yData[i++] = pair.second;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
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
    final ArraysDoublesCurve other = (ArraysDoublesCurve) obj;
    return ArrayUtils.isEquals(_xData, other._xData) && ArrayUtils.isEquals(_yData, other._yData);
  }

}
