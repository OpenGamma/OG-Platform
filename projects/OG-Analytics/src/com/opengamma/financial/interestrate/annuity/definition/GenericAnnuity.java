/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.util.money.Currency;

/**
 * A generic annuity is a set of payments (cash flows) at known future times. All payments have the same currency.
 * There payments can be known in advance, or depend on the future value of some (possibly several) indices, e.g. the Libor.
 * @param <P> The payment type 
 */
public class GenericAnnuity<P extends Payment> implements InstrumentDerivative {

  /**
   * The list of the annuity payments.
   */
  private final P[] _payments;
  /**
   * Flag indicating if the annuity is payer (true) or receiver (false). Deduced from the first non-zero amount; 
   * if all amounts don't have the same sign, the flag may be incorrect.
   */
  private final boolean _isPayer;

  public GenericAnnuity(final P[] payments) {
    Validate.noNullElements(payments);
    Validate.isTrue(payments.length > 0, "Have no payments in annuity");
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
   * @return  True if IborCoupon or FixedCoupon 
   */
  public boolean isIborOrFixed() {
    boolean result = true;
    for (int looppayment = 0; looppayment < _payments.length; looppayment++) {
      result = (result & _payments[looppayment].isIborOrFixed());
    }
    return result;
  }

  /**
   * Gets the payments array.
   * @return the payments
   */
  public P[] getPayments() {
    return _payments;
  }

  /**
   * Gets the payer flag: payer (true) or receiver (false)
   * @return The payer flag.
   */
  public boolean isPayer() {
    return _isPayer;
  }

  /**
   * Return the discounting (or funding) curve name. Deduced from the first payment.
   * @return The name.
   */
  public String getDiscountCurve() {
    return getNthPayment(0).getFundingCurveName();
  }

  /**
   * Create a new annuity with the payments of the original one paying strictly after the given time.
   * @param trimTime The time.
   * @return The trimmed annuity.
   */
  @SuppressWarnings("unchecked")
  public GenericAnnuity<P> trimBefore(double trimTime) {
    List<P> list = new ArrayList<P>();
    list.clear();
    for (P payment : _payments) {
      if (payment.getPaymentTime() > trimTime) {
        list.add(payment);
      }
    }
    return new GenericAnnuity<P>(list.toArray((P[]) new Payment[0]));
  }

  /**
   * Create a new annuity with the payments of the original one paying before or on the given time.
   * @param trimTime The time.
   * @return The trimmed annuity.
   */
  @SuppressWarnings("unchecked")
  public GenericAnnuity<P> trimAfter(double trimTime) {
    List<P> list = new ArrayList<P>();
    for (P payment : _payments) {
      if (payment.getPaymentTime() <= trimTime) {
        list.add(payment);
      }
    }
    return new GenericAnnuity<P>(list.toArray((P[]) new Payment[0]));
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
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitGenericAnnuity(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitGenericAnnuity(this);
  }

}
