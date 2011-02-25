/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

/**
 * 
 */
public class SVIFormulaData {
  private double _a;
  private double _b;
  private double _rho;
  private double _sigma;
  private double _m;

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

  public void setA(final double a) {
    _a = a;
  }

  public void setB(final double b) {
    _b = b;
  }

  public void setRho(final double rho) {
    _rho = rho;
  }

  public void setSigma(final double sigma) {
    _sigma = sigma;
  }

  public void setM(final double m) {
    _m = m;
  }

}
