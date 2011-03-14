/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;

/**
 * 
 */
public class PaymentFixed extends Payment {
  //  private final double _time;
  //  private final String _fundingCurveName;
  private final double _amount;

  public PaymentFixed(final double paymentTime, final double paymentAmount, final String fundingCurve) {
    super(paymentTime, fundingCurve);
    Validate.notNull(fundingCurve);
    //    _time = paymentTime;
    _amount = paymentAmount;
    //    _fundingCurveName = fundingCurve;
  }

  //  @Override
  //  public String getFundingCurveName() {
  //    return _fundingCurveName;
  //  }

  public double getAmount() {
    return _amount;
  }

  //  @Override
  //  public double getPaymentTime() {
  //    return _time;
  //  }

  //  @Override
  //  public int hashCode() {
  //    final int prime = 31;
  //    int result = 1;
  //    long temp;
  //    temp = Double.doubleToLongBits(_amount);
  //    result = prime * result + (int) (temp ^ (temp >>> 32));
  //    result = prime * result + _fundingCurveName.hashCode();
  //    temp = Double.doubleToLongBits();
  //    result = prime * result + (int) (temp ^ (temp >>> 32));
  //    return result;
  //  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitFixedPayment(this, data);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_amount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PaymentFixed other = (PaymentFixed) obj;
    if (Double.doubleToLongBits(_amount) != Double.doubleToLongBits(other._amount)) {
      return false;
    }
    return true;
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitFixedPayment(this);
  }

}
