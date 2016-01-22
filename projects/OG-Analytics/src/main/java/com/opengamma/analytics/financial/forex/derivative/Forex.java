/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.derivative;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a foreign exchange transaction (spot or forward).
 */
public class Forex implements InstrumentDerivative, Serializable {

  /**
   * The payment in the first currency.
   */
  private final PaymentFixed _paymentCurrency1;
  /**
   * The payment in the second currency.
   */
  private final PaymentFixed _paymentCurrency2;

  /**
   * Constructor from two fixed payments. The payments should take place on the same date. The sign of the amounts should be opposite.
   * @param paymentCurrency1 The first currency payment.
   * @param paymentCurrency2 The second currency payment.
   */
  public Forex(final PaymentFixed paymentCurrency1, final PaymentFixed paymentCurrency2) {
    Validate.notNull(paymentCurrency1, "Payment 1");
    Validate.notNull(paymentCurrency2, "Payment 2");
    Validate.isTrue(Double.doubleToLongBits(paymentCurrency1.getPaymentTime()) == Double.doubleToLongBits(paymentCurrency2.getPaymentTime()), "Payments on different time");
    Validate.isTrue((paymentCurrency1.getAmount() * paymentCurrency2.getAmount()) <= 0, "Payments with same sign");
    Validate.isTrue(!paymentCurrency1.getCurrency().equals(paymentCurrency2.getCurrency()), "same currency");
    this._paymentCurrency1 = paymentCurrency1;
    this._paymentCurrency2 = paymentCurrency2;
  }

  /**
   * Gets the payment in the first currency.
   * @return The payment in the first currency.
   */
  public PaymentFixed getPaymentCurrency1() {
    return _paymentCurrency1;
  }

  /**
   * Gets the payment in the second currency.
   * @return The payment in the second currency.
   */
  public PaymentFixed getPaymentCurrency2() {
    return _paymentCurrency2;
  }

  /**
   * Gets the first currency.
   * @return The currency.
   */
  public Currency getCurrency1() {
    return _paymentCurrency1.getCurrency();
  }

  /**
   * Gets the second currency.
   * @return The currency.
   */
  public Currency getCurrency2() {
    return _paymentCurrency2.getCurrency();
  }

  /**
   * Gets the payment time.
   * @return The time.
   */
  public double getPaymentTime() {
    return _paymentCurrency1.getPaymentTime();
  }

  @Override
  public String toString() {
    return _paymentCurrency1.toString() + "\n" + _paymentCurrency2.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _paymentCurrency1.hashCode();
    result = prime * result + _paymentCurrency2.hashCode();
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
    final Forex other = (Forex) obj;
    if (!ObjectUtils.equals(_paymentCurrency1, other._paymentCurrency1)) {
      return false;
    }
    if (!ObjectUtils.equals(_paymentCurrency2, other._paymentCurrency2)) {
      return false;
    }
    return true;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForex(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForex(this);
  }

}
