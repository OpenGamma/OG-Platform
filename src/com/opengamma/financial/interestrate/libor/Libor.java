/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.libor;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class Libor implements InterestRateDerivative {
  private final double _paymentTime;
  private final String _curveName;

  public Libor(final double paymentTime, final String liborCurveName) {
    ArgumentChecker.notNegative(paymentTime, "payment time");
    Validate.notNull(liborCurveName);
    _paymentTime = paymentTime;
    _curveName = liborCurveName;
  }

  public double getPaymentTime() {
    return _paymentTime;
  }

  public String getLiborCurveName() {
    return _curveName;

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_curveName == null) ? 0 : _curveName.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_paymentTime);
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
    Libor other = (Libor) obj;
    if (_curveName == null) {
      if (other._curveName != null) {
        return false;
      }
    } else if (!_curveName.equals(other._curveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentTime) != Double.doubleToLongBits(other._paymentTime)) {
      return false;
    }
    return true;
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<T> visitor, YieldCurveBundle curves) {
    return visitor.visitLibor(this, curves);
  }

}
