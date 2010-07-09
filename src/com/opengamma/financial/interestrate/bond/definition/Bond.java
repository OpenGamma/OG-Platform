/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class Bond {
  private final double[] _paymentTimes;
  private final double[] _payments;

  public Bond(final double[] paymentTimes, final double[] payments) {
    Validate.notNull(paymentTimes, "payment times");
    Validate.notNull(payments, "payments");
    ArgumentChecker.notEmpty(paymentTimes, "payment times");
    if (paymentTimes.length != payments.length) {
      throw new IllegalArgumentException("Must have a payment for each payment time");
    }
    _paymentTimes = paymentTimes;
    _payments = payments;
  }

  public double[] getPaymentTimes() {
    return _paymentTimes;
  }

  public double[] getPayments() {
    return _payments;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_paymentTimes);
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
    final Bond other = (Bond) obj;
    if (!Arrays.equals(_paymentTimes, other._paymentTimes)) {
      return false;
    }
    if (!Arrays.equals(_payments, other._payments)) {
      return false;
    }
    return true;
  }
}
