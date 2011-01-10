/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.number;

import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class ComplexNumber extends Number {
  private final double _real;
  private final double _imaginary;

  public ComplexNumber(final double real, final double imaginary) {
    _real = real;
    _imaginary = imaginary;
  }

  public double getReal() {
    return _real;
  }

  public double getImaginary() {
    return _imaginary;
  }

  @Override
  public String toString() {
    if (CompareUtils.closeEquals(0, _real)) {
      if (CompareUtils.closeEquals(0, _imaginary)) {
        return Double.toString(0);
      }
      return Double.toString(_imaginary) + "i";
    }
    if (CompareUtils.closeEquals(0, _imaginary)) {
      return Double.toString(_real);
    }
    return Double.toString(_real) + (_imaginary < 0 ? " " : " + ") + Double.toString(_imaginary) + "i";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_imaginary);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_real);
    result = prime * result + (int) (temp ^ temp >>> 32);
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
    final ComplexNumber other = (ComplexNumber) obj;
    if (Double.doubleToLongBits(_imaginary) != Double.doubleToLongBits(other._imaginary)) {
      return false;
    }
    if (Double.doubleToLongBits(_real) != Double.doubleToLongBits(other._real)) {
      return false;
    }
    return true;
  }

  @Override
  public double doubleValue() {
    throw new UnsupportedOperationException("Cannot get the doubleValue of a ComplexNumber");
  }

  @Override
  public float floatValue() {
    throw new UnsupportedOperationException("Cannot get the floatValue of a ComplexNumber");
  }

  @Override
  public int intValue() {
    throw new UnsupportedOperationException("Cannot get the intValue of a ComplexNumber");
  }

  @Override
  public long longValue() {
    throw new UnsupportedOperationException("Cannot get the longValue of a ComplexNumber");
  }
}
