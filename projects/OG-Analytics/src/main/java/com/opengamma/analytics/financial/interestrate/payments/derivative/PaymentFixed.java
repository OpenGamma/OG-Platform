/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
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
   * @deprecated Use the version that does not take a funding curve name
   */
  @Deprecated
  public PaymentFixed(final Currency currency, final double paymentTime, final double paymentAmount, final String fundingCurve) {
    super(currency, paymentTime, fundingCurve);
    _amount = paymentAmount;
  }

  /**
   * Fixed payment constructor.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentAmount The amount paid.
   */
  public PaymentFixed(final Currency currency, final double paymentTime, final double paymentAmount) {
    super(currency, paymentTime);
    _amount = paymentAmount;
  }

  /**
   * Create a new fixed payment with the same characteristic except the amount which is the given amount.
   * @param paymentAmount The amount.
   * @return The fixed payment.
   */
  @SuppressWarnings("deprecation")
  public PaymentFixed withAmount(final double paymentAmount) {
    try {
      return new PaymentFixed(getCurrency(), getPaymentTime(), paymentAmount, getFundingCurveName());
    } catch (final IllegalStateException e) {
      return new PaymentFixed(getCurrency(), getPaymentTime(), paymentAmount);
    }
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
    ArgumentChecker.notNull(visitor, "visitor");
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
    final PaymentFixed other = (PaymentFixed) obj;
    if (Double.doubleToLongBits(_amount) != Double.doubleToLongBits(other._amount)) {
      return false;
    }
    return true;
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitFixedPayment(this);
  }

  @Override
  public double getReferenceAmount() {
    return _amount;
  }

}
