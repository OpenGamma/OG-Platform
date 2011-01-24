/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;
import com.opengamma.financial.interestrate.payments.FixedPayment;

/**
 * 
 */
public class BondForward implements InterestRateDerivative {
  private final Bond _bond;
  private final double _forwardTime;
  private final double _accruedInterest;
  private final double _accruedInterestAtDelivery;
  private final FixedCouponPayment[] _expiredCoupons;

  public BondForward(final Bond bond, final double forwardTime, final double accruedInterest, final double accruedInterestAtDelivery) {
    this(bond, forwardTime, accruedInterest, accruedInterestAtDelivery, new FixedCouponPayment[0]);
  }

  public BondForward(final Bond bond, final double forwardTime, final double accruedInterest, final double accruedInterestAtDelivery, final FixedCouponPayment[] expiredCoupons) {
    Validate.notNull(bond, "bond");
    Validate.isTrue(forwardTime >= 0, "forward Time is negative");
    Validate.noNullElements(expiredCoupons, "expired coupons");
    final FixedPayment principle = bond.getPrinciplePayment();
    Validate.isTrue(forwardTime < principle.getPaymentTime(), "forward time beyond maturity of bond");
    _bond = bond;
    _forwardTime = forwardTime;
    _accruedInterest = accruedInterest;
    _accruedInterestAtDelivery = accruedInterestAtDelivery;
    _expiredCoupons = expiredCoupons;
  }

  public Bond getBond() {
    return _bond;
  }

  public double getForwardTime() {
    return _forwardTime;
  }

  public double getAccruedInterest() {
    return _accruedInterest;
  }

  public double getAccruedInterestAtDelivery() {
    return _accruedInterestAtDelivery;
  }

  //TODO change this name
  public FixedCouponPayment[] getTimeBetweenExpiredCoupons() {
    return _expiredCoupons;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_accruedInterest);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_accruedInterestAtDelivery);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _bond.hashCode();
    temp = Double.doubleToLongBits(_forwardTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_expiredCoupons);
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
    if (Double.doubleToLongBits(_accruedInterest) != Double.doubleToLongBits(other._accruedInterest)) {
      return false;
    }
    if (Double.doubleToLongBits(_accruedInterestAtDelivery) != Double.doubleToLongBits(other._accruedInterestAtDelivery)) {
      return false;
    }
    if (!ObjectUtils.equals(_bond, other._bond)) {
      return false;
    }
    if (Double.doubleToLongBits(_forwardTime) != Double.doubleToLongBits(other._forwardTime)) {
      return false;
    }
    return Arrays.equals(_expiredCoupons, other._expiredCoupons);
  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visit(this, data);
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visit(this);
  }

}
