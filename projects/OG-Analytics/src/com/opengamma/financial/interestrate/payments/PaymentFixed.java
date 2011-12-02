/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 * Class describing a fixed payment.
 */
public class PaymentFixed extends Payment {

  /**
   * The paid amount.
   */
  private final double _amount;

  /**
   * Fixed payment constructor.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentAmount The amount paid.
   * @param fundingCurve Name of the funding curve.
   */
  public PaymentFixed(final Currency currency, final double paymentTime, final double paymentAmount, final String fundingCurve) {
    super(currency, paymentTime, fundingCurve);
    Validate.notNull(fundingCurve);
    _amount = paymentAmount;
  }

  /**
   * Gets the amount paid.
   * @return The amount.
   */
  public double getAmount() {
    return _amount;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitFixedPayment(this, data);
  }

  @Override
  public String toString() {
    return super.toString() + ", amount = " + _amount;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_amount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitFixedPayment(this);
  }

  @Override
  public double getReferenceAmount() {
    return _amount;
  }

}
