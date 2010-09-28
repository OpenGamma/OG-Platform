/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class FixedAmountPayment implements FixedPayment {

  private final double _time;
  private final double _amount;

  public FixedAmountPayment(double paymentTime, double paymentAmount) {
    Validate.isTrue(paymentTime >= 0.0, "Payment time < 0");
    _time = paymentTime;
    _amount = paymentAmount;
  }

  @Override
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
    temp = Double.doubleToLongBits(_time);
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
    FixedAmountPayment other = (FixedAmountPayment) obj;
    if (Double.doubleToLongBits(_amount) != Double.doubleToLongBits(other._amount)) {
      return false;
    }
    if (Double.doubleToLongBits(_time) != Double.doubleToLongBits(other._time)) {
      return false;
    }
    return true;
  }

  @Override
  public <S, T> T accept(PaymentVisitor<S, T> visitor, S data) {
    return visitor.visitFixedAmountPayment(this, data);
  }

}
