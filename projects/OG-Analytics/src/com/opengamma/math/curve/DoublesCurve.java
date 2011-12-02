/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.ParallelArrayBinarySort;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Parent class for a family of curves that have real <i>x</i> and <i>y</i> values. It is possible to construct a curve using either unsorted (in <i>x</i>) data
 * or sorted (ascending in <i>x</i>). Note that if the constructor is told that unsorted data are sorted then no sorting will take place, which will give 
 * unpredictable results.
 */
// TODO test for distinctness of nodes?
public abstract class DoublesCurve extends Curve<Double, Double> {
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
  public DoublesCurve(final double[] xData, final double[] yData, final boolean isSorted) {
    super();
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.isTrue(xData.length == yData.length);
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
  public DoublesCurve(final Double[] xData, final Double[] yData, final boolean isSorted) {
    super();
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    _n = xData.length;
    Validate.isTrue(_n == yData.length);
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      Double x = xData[i];
      Double y = yData[i];
      if (x == null || y == null) {
        Validate.notNull(x, "element " + i + " of x data");
        Validate.notNull(y, "element " + i + " of y data");
      }
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
  public DoublesCurve(final Map<Double, Double> data, final boolean isSorted) {
    super();
    Validate.notNull(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    int i = 0;
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      Double x = entry.getKey();
      Double y = entry.getValue();
      if (x == null || y == null) {
        Validate.notNull(x, "element " + i + " of x data");
        Validate.notNull(y, "element " + i + " of y data");
      }
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
  public DoublesCurve(final DoublesPair[] data, final boolean isSorted) {
    super();
    Validate.notNull(data, "data");
    _n = data.length;
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      DoublesPair pair = data[i];
      if (pair == null) {
        Validate.notNull(data[i], "element " + i + " of data");
      }
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
  public DoublesCurve(final Set<DoublesPair> data, final boolean isSorted) {
    super();
    Validate.notNull(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    int i = 0;
    for (final DoublesPair entry : data) {
      if (entry == null) {
        Validate.notNull(entry, "element " + i + " of data");
      }
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
  public DoublesCurve(final List<Double> xData, final List<Double> yData, final boolean isSorted) {
    super();
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.isTrue(xData.size() == yData.size());
    _n = xData.size();
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      Double x = xData.get(i);
      Double y = yData.get(i);
      if (x == null || y == null) {
        Validate.notNull(x, "element " + i + " of x data");
        Validate.notNull(y, "element " + i + " of y data");
      }
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
  public DoublesCurve(final List<DoublesPair> data, final boolean isSorted) {
    super();
    Validate.notNull(data, "data");
    Validate.noNullElements(data, "data");
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
  public DoublesCurve(final double[] xData, final double[] yData, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.isTrue(xData.length == yData.length);
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
  public DoublesCurve(final Double[] xData, final Double[] yData, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(xData, "x data");
    _n = xData.length;
    Validate.notNull(yData, "y data");
    Validate.isTrue(_n == yData.length);
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      if (xData[i] == null || yData[i] == null) {
        Validate.notNull(xData[i], "element " + i + " of x data");
        Validate.notNull(yData[i], "element " + i + " of y data");
      }
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
  public DoublesCurve(final Map<Double, Double> data, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    int i = 0;
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      Validate.notNull(entry.getKey(), "element " + i + " of x data");
      Validate.notNull(entry.getValue(), "element " + i + " of y data");
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
  public DoublesCurve(final DoublesPair[] data, final boolean isSorted, final String name) {
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

  /**
   * 
   * @param data A set of <i>x-y</i> pairs, not null
   * @param isSorted Is the <i>x</i>-data sorted
   * @param name The name of the curve
   */
  public DoublesCurve(final Set<DoublesPair> data, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    int i = 0;
    for (final DoublesPair entry : data) {
      Validate.notNull(entry, "element " + i + " of data");
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
  public DoublesCurve(final List<Double> xData, final List<Double> yData, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    Validate.isTrue(xData.size() == yData.size());
    _n = xData.size();
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      Validate.notNull(xData.get(i), "element " + i + " of data");
      Validate.notNull(yData.get(i), "element " + i + " of data");
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
  public DoublesCurve(final List<DoublesPair> data, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(data, "data");
    Validate.noNullElements(data, "data");
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
    final DoublesCurve other = (DoublesCurve) obj;
    return ArrayUtils.isEquals(_xData, other._xData) && ArrayUtils.isEquals(_yData, other._yData);
  }

}
