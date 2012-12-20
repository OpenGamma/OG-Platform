/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class CEVFunctionData {

  /**
   * The forward.
   */
  private final double _forward;
  /**
   * The numeraire.
   */
  private final double _numeraire;
  /**
   * The normal volatility.
   */
  private final double _volatility;
  /**
   * The elasticity parameter.
   */
  private final double _beta;

  public CEVFunctionData(final double f, final double df, final double sigma, final double beta) {
    _forward = f;
    _numeraire = df;
    _volatility = sigma;
    Validate.isTrue(beta >= 0.0, "beta less than zero not supported");
    _beta = beta;
  }

  public double getForward() {
    return _forward;
  }

  public double getNumeraire() {
    return _numeraire;
  }

  public double getVolatility() {
    return _volatility;
  }

  public double getBeta() {
    return _beta;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_beta);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_forward);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_numeraire);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_volatility);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CEVFunctionData other = (CEVFunctionData) obj;
    if (Double.doubleToLongBits(_beta) != Double.doubleToLongBits(other._beta)) {
      return false;
    }
    if (Double.doubleToLongBits(_forward) != Double.doubleToLongBits(other._forward)) {
      return false;
    }
    if (Double.doubleToLongBits(_numeraire) != Double.doubleToLongBits(other._numeraire)) {
      return false;
    }
    if (Double.doubleToLongBits(_volatility) != Double.doubleToLongBits(other._volatility)) {
      return false;
    }
    return true;
  }

}
