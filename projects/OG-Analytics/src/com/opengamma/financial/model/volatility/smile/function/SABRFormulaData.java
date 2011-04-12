/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class SABRFormulaData {
  private final double _nu;
  private final double _rho;
  private final double _beta;
  private final double _alpha;
  private final double _forward;

  /**
   * 
   * @param forward The forward value of the underlying 
   * @param alpha The initial value of the stochastic volatility 
   * @param beta The CEV parameter 
   * @param nu The vol-of-vol
   * @param rho The correlation between the driver of the underlying and the driver of the stochastic volatility 
   */
  public SABRFormulaData(final double forward, final double alpha, final double beta, final double nu, final double rho) {
    Validate.isTrue(forward > 0.0, "f must be > 0.0");
    Validate.isTrue(beta >= 0.0, "beta must be >= 0.0");
    Validate.isTrue(nu >= 0.0, "nu must be >= 0.0");
    Validate.isTrue(rho >= -1 && rho <= 1, "rho must be between -1 and 1");
    _forward = forward;
    _alpha = alpha;
    _beta = beta;
    _rho = rho;
    _nu = nu;
  }

  public double getNu() {
    return _nu;
  }

  public double getRho() {
    return _rho;
  }

  public double getBeta() {
    return _beta;
  }

  public double getAlpha() {
    return _alpha;
  }

  public double getForward() {
    return _forward;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_alpha);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_beta);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_forward);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_nu);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_rho);
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
    final SABRFormulaData other = (SABRFormulaData) obj;
    if (Double.doubleToLongBits(_alpha) != Double.doubleToLongBits(other._alpha)) {
      return false;
    }
    if (Double.doubleToLongBits(_beta) != Double.doubleToLongBits(other._beta)) {
      return false;
    }
    if (Double.doubleToLongBits(_forward) != Double.doubleToLongBits(other._forward)) {
      return false;
    }
    if (Double.doubleToLongBits(_nu) != Double.doubleToLongBits(other._nu)) {
      return false;
    }
    if (Double.doubleToLongBits(_rho) != Double.doubleToLongBits(other._rho)) {
      return false;
    }
    return true;
  }

}
