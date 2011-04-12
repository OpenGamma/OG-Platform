/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;


/**
 * 
 */
public class SVIFormulaData {
  private final double _a;
  private final double _b;
  private final double _rho;
  private final double _sigma;
  private final double _m;

  public SVIFormulaData(final double a, final double b, final double rho, final double sigma, final double m) {
    _a = a;
    _b = b;
    _rho = rho;
    _sigma = sigma;
    _m = m;
  }

  public double getA() {
    return _a;
  }

  public double getB() {
    return _b;
  }

  public double getRho() {
    return _rho;
  }

  public double getSigma() {
    return _sigma;
  }

  public double getM() {
    return _m;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_a);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_b);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_m);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_rho);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_sigma);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final SVIFormulaData other = (SVIFormulaData) obj;
    if (Double.doubleToLongBits(_a) != Double.doubleToLongBits(other._a)) {
      return false;
    }
    if (Double.doubleToLongBits(_b) != Double.doubleToLongBits(other._b)) {
      return false;
    }
    if (Double.doubleToLongBits(_m) != Double.doubleToLongBits(other._m)) {
      return false;
    }
    if (Double.doubleToLongBits(_rho) != Double.doubleToLongBits(other._rho)) {
      return false;
    }
    if (Double.doubleToLongBits(_sigma) != Double.doubleToLongBits(other._sigma)) {
      return false;
    }
    return true;
  }

}
