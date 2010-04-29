/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

public class DoubleMatrix1D implements Matrix<Double[]> {
  private final Double[] _data;
  private final double[] _primitives;
  private final int _elements;

  public DoubleMatrix1D(final Double[] data) {
    if (data == null)
      throw new IllegalArgumentException("Cannot initialize matrix with null data");
    _elements = data.length;
    _primitives = new double[_elements];
    _data = new Double[_elements];
    for (int i = 0; i < _elements; i++) {
      _primitives[i] = data[i];
      _data[i] = data[i];
    }
  }

  public DoubleMatrix1D(final double[] primitives) {
    if (primitives == null)
      throw new IllegalArgumentException("Cannot initialize matrix with null data");
    _elements = primitives.length;
    _data = new Double[_elements];
    _primitives = new double[_elements];
    for (int i = 0; i < _elements; i++) {
      _data[i] = primitives[i];
      _primitives[i] = primitives[i];
    }
  }

  public Double[] getDataAsObjectArray() {
    return _data;
  }

  public double[] getDataAsPrimitiveArray() {
    return _primitives;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.math.matrix.Matrix#getNumberOfElements()
   */
  @Override
  public int getNumberOfElements() {
    return _elements;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _data.hashCode();
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final DoubleMatrix1D other = (DoubleMatrix1D) obj;
    if (_data == null) {
      if (other._data != null)
        return false;
    } else if (!_data.equals(other._data))
      return false;
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
