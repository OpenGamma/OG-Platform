/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * A minimal implementation of a vector (in the mathematical sense) that contains doubles
 */
public class DoubleMatrix1D implements Matrix<Double> {
  private final double[] _data;
  private final int _elements;
  /**
   * Empty matrix
   */
  public static final DoubleMatrix1D EMPTY_MATRIX = new DoubleMatrix1D(new double[0]);

  public DoubleMatrix1D(final Double[] data) {
    Validate.notNull(data);
    _elements = data.length;
    _data = new double[_elements];
    for (int i = 0; i < _elements; i++) {
      _data[i] = data[i];

    }
  }

  public DoubleMatrix1D(final double[] data) {
    Validate.notNull(data);
    _elements = data.length;
    _data = new double[_elements];
    for (int i = 0; i < _elements; i++) {
      _data[i] = data[i];
    }
  }

  /**
   * Returns the underlying vector data. If this is changed so is the vector
   * @see #toArray to get clone of data
   * @return array containing the vector elements 
   */
  public double[] getData() {
    return _data;
  }

  /**
   * Convert the vector to a double array. 
   * The array is independent from vector data, its elements are copied.
   * @return array containing a copy of vector elements
   */
  public double[] toArray() {
    return Arrays.copyOf(_data, _elements);
  }

  @Override
  public int getNumberOfElements() {
    return _elements;
  }

  /**
   * @param index 
   * @return the element at the index
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
    for (final Double d : _data) {
      sb.append("(" + d + ")\n");
    }
    return sb.toString();
  }
}
