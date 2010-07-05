/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.cash.definition;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class Cash implements InterestRateDerivative {
  private final double _fixedPaymentTime;

  public Cash(final double fixedPaymentTime) {
    ArgumentChecker.notNegative(fixedPaymentTime, "fixed payment time");
    _fixedPaymentTime = fixedPaymentTime;
  }

  public double getFixedPaymentTime() {
    return _fixedPaymentTime;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_fixedPaymentTime);
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
    final Cash other = (Cash) obj;
    if (Double.doubleToLongBits(_fixedPaymentTime) != Double.doubleToLongBits(other._fixedPaymentTime)) {
      return false;
    }
    return true;
  }
}
