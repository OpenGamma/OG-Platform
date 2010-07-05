/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.libor;

import com.opengamma.financial.interestrate.InterestRateDerivative;

/**
 * 
 */
public class Libor implements InterestRateDerivative {
  private final double _paymentTime;

  public Libor(final double paymentTime) {
    _paymentTime = paymentTime;
  }

  public double getPaymentTime() {
    return _paymentTime;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_paymentTime);
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
    final Libor other = (Libor) obj;
    if (Double.doubleToLongBits(_paymentTime) != Double.doubleToLongBits(other._paymentTime)) {
      return false;
    }
    return true;
  }

}
