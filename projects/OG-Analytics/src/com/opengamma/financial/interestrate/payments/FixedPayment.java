/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;

/**
 * 
 */
public class FixedPayment implements Payment {
  private final double _time;
  private final double _amount;
  private final String _fundingCurveName;

  public FixedPayment(final double paymentTime, final double paymentAmount, final String fundingCurve) {
    Validate.isTrue(paymentTime >= 0.0, "Payment time < 0");
    Validate.notNull(fundingCurve);
    _time = paymentTime;
    _amount = paymentAmount;
    _fundingCurveName = fundingCurve;
  }

  @Override
  public String getFundingCurveName() {
    return _fundingCurveName;
  }

  public double getAmount() {
    return _amount;
  }

  @Override
  public double getPaymentTime() {
    return _time;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_amount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _fundingCurveName.hashCode();
    temp = Double.doubleToLongBits(_time);
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
    final FixedPayment other = (FixedPayment) obj;
    if (Double.doubleToLongBits(_amount) != Double.doubleToLongBits(other._amount)) {
      return false;
    }
    if (!ObjectUtils.equals(_fundingCurveName, other._fundingCurveName)) {
      return false;
    }
    return Double.doubleToLongBits(_time) == Double.doubleToLongBits(other._time);
  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitFixedPayment(this, data);
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitFixedPayment(this);
  }

}
