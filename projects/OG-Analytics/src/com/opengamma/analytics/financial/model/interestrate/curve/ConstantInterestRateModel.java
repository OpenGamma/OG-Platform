/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import com.opengamma.analytics.financial.model.interestrate.InterestRateModel;

/**
 * 
 */
public class ConstantInterestRateModel implements InterestRateModel<Double> {
  private final double _r;

  public ConstantInterestRateModel(final double r) {
    _r = r;
  }

  @Override
  public double getInterestRate(final Double t) {
    return _r;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_r);
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
    final ConstantInterestRateModel other = (ConstantInterestRateModel) obj;
    if (Double.doubleToLongBits(_r) != Double.doubleToLongBits(other._r)) {
      return false;
    }
    return true;
  }

}
