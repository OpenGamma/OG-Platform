/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class SABRFormulaData {
  private double _nu;
  private double _rho;
  private double _beta;
  private double _alpha;
  private double _f;

  public SABRFormulaData(final double f, final double alpha, final double beta, final double nu, final double rho) {
    Validate.isTrue(f > 0.0, "f must be > 0.0");
    Validate.isTrue(beta >= 0.0, "beta must be >= 0.0");
    Validate.isTrue(nu >= 0.0, "nu must be >= 0.0");
    Validate.isTrue(rho >= -1 && rho <= 1, "rho must be between -1 and 1");
    _f = f;
    _alpha = alpha;
    _beta = beta;
    _rho = rho;
    _nu = nu;
  }

  public double getNu() {
    return _nu;
  }

  public void setNu(final double nu) {
    _nu = nu;
  }

  public double getRho() {
    return _rho;
  }

  public void setRho(final double rho) {
    _rho = rho;
  }

  public double getBeta() {
    return _beta;
  }

  public void setBeta(final double beta) {
    _beta = beta;
  }

  public double getAlpha() {
    return _alpha;
  }

  public void setAlpha(final double alpha) {
    _alpha = alpha;
  }

  public double getF() {
    return _f;
  }

  public void setF(final double f) {
    _f = f;
  }

}
