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

/**
 * A generic annuity is a set of payments (cash flows) at known future times. 
 * There payments can be known in advance, or depend on the future value of  some (possibly several) indices, e.g. the future Libor
 * @param <P> The payment type 
 */
public class GenericAnnuity<P extends Payment> implements InterestRateDerivative {
  private final P[] _payments;

  public GenericAnnuity(final P[] payments) {
    Validate.noNullElements(payments);
    Validate.isTrue(payments.length > 0);
    _payments = payments;
  }

  @SuppressWarnings("unchecked")
  public GenericAnnuity(final List<? extends P> payments, final Class<P> pType) {
    Validate.noNullElements(payments);
    Validate.notNull(pType);
    Validate.isTrue(payments.size() > 0);
    _payments = payments.toArray((P[]) Array.newInstance(pType, 0));
  }

  public int getNumberOfPayments() {
    return _payments.length;
  }

  public P getNthPayment(final int n) {
    return _payments[n];
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
