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
public class CEVFunctionData extends BlackFunctionData {
  private final double _beta;

  public CEVFunctionData(final double f, final double df, final double sigma, final double beta) {
    super(f, df, sigma);
    Validate.isTrue(beta >= 0.0, "beta less than zero not supported");
    _beta = beta;
  }

  public double getBeta() {
    return _beta;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_beta);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CEVFunctionData other = (CEVFunctionData) obj;
    if (Double.doubleToLongBits(_beta) != Double.doubleToLongBits(other._beta)) {
      return false;
    }
    return true;
  }

}
