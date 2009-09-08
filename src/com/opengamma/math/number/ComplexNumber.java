package com.opengamma.math.number;

import com.opengamma.util.CompareUtils;

/**
 * @author emcleod
 */

// TODO should this extend Number?
public class ComplexNumber {
  private double _real;
  private double _imaginary;

  public ComplexNumber(double real, double imaginary) {
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
    return Double.toString(_real) + " + " + Double.toString(_imaginary) + "i";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_imaginary);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_real);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ComplexNumber other = (ComplexNumber) obj;
    if (Double.doubleToLongBits(_imaginary) != Double.doubleToLongBits(other._imaginary)) return false;
    if (Double.doubleToLongBits(_real) != Double.doubleToLongBits(other._real)) return false;
    return true;
  }
}
