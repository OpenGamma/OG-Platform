/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.sun.tools.javac.util.List;

/**
 * 
 */
public class GenericAnnuity implements InterestRateDerivative {

  private final Payment[] _payments;
  private final double _notional;

  public GenericAnnuity(Payment[] payments) {
    this(payments, 1.0);
  }

  public GenericAnnuity(Payment[] payments, double notional) {
    Validate.noNullElements(payments);

    _payments = payments;
    _notional = notional;
  }

  public GenericAnnuity(List<Payment> payments, double notional) {
    Validate.noNullElements(payments);

    _payments = new Payment[payments.size()];
    payments.toArray(_payments);
    _notional = notional;
  }

  public int getNumberOfpayments() {
    return _payments.length;
  }

  public Payment getNthPayment(int n) {
    return _payments[n];
  }

  /**
   * Gets the payments field.
   * @return the payments
   */
  public Payment[] getPayments() {
    return _payments;
  }

  /**
   * Gets the notional field.
   * @return the notional
   */
  public double getNotional() {
    return _notional;
  }

  @Override
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return null;
  }

  @Override
  public InterestRateDerivative withRate(double rate) {
    return null;
  }

}
