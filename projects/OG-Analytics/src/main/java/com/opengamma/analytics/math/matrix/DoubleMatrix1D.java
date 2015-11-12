/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.matrix;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * A minimal implementation of a vector (in the mathematical sense) that contains doubles.
 */
public class DoubleMatrix1D implements Matrix<Double>, Serializable {
  private static final long serialVersionUID = 1L;
  
  private final double[] _data;
  private final int _elements;
  /** Empty vector */
  public static final DoubleMatrix1D EMPTY_MATRIX = new DoubleMatrix1D(new double[0]);

  /**
   * @param data The data, not null
   */
  public DoubleMatrix1D(final Double[] data) {
    Validate.notNull(data);
    _elements = data.length;
    _data = new double[_elements];
    for (int i = 0; i < _elements; i++) {
      _data[i] = data[i];
    }
  }

  /**
   * @param data The data, not null
   */
  public DoubleMatrix1D(final double... data) {
    Validate.notNull(data);
    _elements = data.length;
    _data = Arrays.copyOf(data, _elements);
  }

  /**
   * Create an vector of length n with all entries equal to value
   * @param n number of elements
   * @param value value of elements
   */
  public DoubleMatrix1D(final int n, final double value) {
    _elements = n;
    _data = new double[_elements];
    Arrays.fill(_data, value);
  }

  /**
   * Create an vector of length n with all entries zero
   * @param n number of elements
   */
  public DoubleMatrix1D(final int n) {
    _elements = n;
    _data = new double[_elements];
  }
  
  /**
   * Create a vector based on the data provided.
   * @param data the data, not null
   * @param copy true if the array should be copied.
   */
  public DoubleMatrix1D(final double[] data, final boolean copy) {
    Validate.notNull(data);
    _elements = data.length;
    if (copy) {
      _data = Arrays.copyOf(data, _elements);
    } else {
      _data = data;
    }
  }

  /**
   * Returns the underlying vector data. If this is changed so is the vector.
   * @see #toArray to get a copy of data
   * @return An array containing the vector elements
   */
  public double[] getData() {
    return _data;
  }

  /**
   * Convert the vector to a double array.
   * As its elements are copied, the array is independent from the vector data.
   * @return An array containing a copy of vector elements
   */
  public double[] toArray() {
    return Arrays.copyOf(_data, _elements);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfElements() {
    return _elements;
  }

  /**
   * {@inheritDoc}
   * This method expects one index - any subsequent indices will be ignored.
   */
  @Override
  public Double getEntry(final int... index) {
    return _data[index[0]];
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_data);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DoubleMatrix1D other = (DoubleMatrix1D) obj;
    if (!Arrays.equals(_data, other._data)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    final int n = _data.length;
    sb.append(" (");
    for (int i = 0; i < (n - 1); i++) {
      sb.append(_data[i] + ", ");
    }
    sb.append(_data[n - 1] + ") ");
    return sb.toString();
  }
}
