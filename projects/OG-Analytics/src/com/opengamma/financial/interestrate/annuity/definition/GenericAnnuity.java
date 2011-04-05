/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.util.money.Currency;

/**
 * A generic annuity is a set of payments (cash flows) at known future times. All payments have the same currency.
 * There payments can be known in advance, or depend on the future value of some (possibly several) indices, e.g. the Libor.
 * @param <P> The payment type 
 */
public class GenericAnnuity<P extends Payment> implements InterestRateDerivative {

  private final P[] _payments;

  /**
   * Flag indicating if the annuity is payer (true) or receiver (false). Deduced from the first non-zero amount; 
   * if all amounts don't have the same sign, the flag can be incorrect.
   */
  private final boolean _isPayer;

  public GenericAnnuity(final P[] payments) {
    Validate.noNullElements(payments);
    Validate.isTrue(payments.length > 0);
    Currency currency0 = payments[0].getCurrency();
    double amount = payments[0].getReferenceAmount();
    for (int loopcpn = 1; loopcpn < payments.length; loopcpn++) {
      Validate.isTrue(currency0.equals(payments[loopcpn].getCurrency()), "currency not the same for all payments");
      amount = (amount == 0) ? payments[loopcpn].getReferenceAmount() : amount;
    }
    _payments = payments;
    _isPayer = (amount < 0);
  }

  @SuppressWarnings("unchecked")
  public GenericAnnuity(final List<? extends P> payments, final Class<P> pType, boolean isPayer) {
    Validate.noNullElements(payments);
    Validate.notNull(pType);
    Validate.isTrue(payments.size() > 0);
    _payments = payments.toArray((P[]) Array.newInstance(pType, 0));
    _isPayer = isPayer;
  }

  public int getNumberOfPayments() {
    return _payments.length;
  }

  public P getNthPayment(final int n) {
    return _payments[n];
  }

  /**
   * Return the currency of the annuity. 
   * @return The currency.
   */
  public Currency getCurrency() {
    return _payments[0].getCurrency();
  }

  /**
   * Check if the payments of an annuity is of the type CouponFixed or CouponIbor. Used to check that payment are of vanilla type.
   * @return The check.
   */
  public boolean isIborOrFixed() {
    boolean result = true;
    for (int looppayment = 0; looppayment < _payments.length; looppayment++) {
      result = (result & _payments[looppayment].isIborOrFixed());
    }
    return result;
  }

  /**
   * Gets the payments field.
   * @return the payments
   */
  public P[] getPayments() {
    return _payments;
  }

  /**
   * Gets the _isPayer field.
   * @return the _isPayer
   */
  public boolean isPayer() {
    return _isPayer;
  }

  @Override
  public String toString() {
    String result = "Annuity:";
    for (int looppayment = 0; looppayment < _payments.length; looppayment++) {
      result += _payments[looppayment].toString();
    }
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_payments);
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
    final GenericAnnuity<?> other = (GenericAnnuity<?>) obj;
    if (_payments.length != other._payments.length) {
      return false;
    }
    for (int i = 0; i < _payments.length; i++) {
      if (!ObjectUtils.equals(_payments[i], other._payments[i])) {
        return false;
      }
    }
    return true;
  }

  @Override
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitGenericAnnuity(this, data);
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitGenericAnnuity(this);
  }

}
