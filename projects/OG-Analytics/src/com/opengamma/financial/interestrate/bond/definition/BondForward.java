/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.payments.FixedPayment;

/**
 * 
 */
public class BondForward {
  private final Bond _bond;
  private final double _forwardTime;

  public BondForward(final Bond bond, final double forwardTime) {
    Validate.notNull(bond, "bond");
    Validate.isTrue(forwardTime >= 0, "forward Time is negative");
    final FixedPayment principle = bond.getPrinciplePayment();
    Validate.isTrue(forwardTime < principle.getPaymentTime(), "forward time beyond maturity of bond");
    _bond = bond;
    _forwardTime = forwardTime;
  }

  public Bond getBond() {
    return _bond;
  }

  public double getForwardTime() {
    return _forwardTime;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _bond.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_forwardTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final BondForward other = (BondForward) obj;
    if (!ObjectUtils.equals(_bond, other._bond)) {
      return false;
    }
    return Double.doubleToLongBits(_forwardTime) == Double.doubleToLongBits(other._forwardTime);
  }

}
