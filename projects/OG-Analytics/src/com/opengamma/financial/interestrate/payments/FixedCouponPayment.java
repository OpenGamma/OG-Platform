/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;

/**
 * 
 */
public class FixedCouponPayment extends FixedPayment {
  private final double _yearFraction;
  private final double _coupon;
  private final double _notional;

  public FixedCouponPayment(final double paymentTime, final double yearFraction, final double coupon, final String fundingCurve) {
    this(paymentTime, 1.0, yearFraction, coupon, fundingCurve);
  }

  public FixedCouponPayment(final double paymentTime, final double notional, final double yearFraction, final double coupon, final String fundingCurve) {
    super(paymentTime, notional * yearFraction * coupon, fundingCurve);
    Validate.isTrue(yearFraction >= 0.0, "year fraction < 0");
    _yearFraction = yearFraction;
    _coupon = coupon;
    _notional = notional;
  }

  /**
   * Gets the yearFraction field.
   * @return the yearFraction
   */
  public double getYearFraction() {
    return _yearFraction;
  }

  /**
   * Gets the coupon field.
   * @return the coupon
   */
  public double getCoupon() {
    return _coupon;
  }

  public double getNotional() {
    return _notional;
  }

  public FixedCouponPayment withUnitCoupon() {
    return new FixedCouponPayment(getPaymentTime(), getNotional(), getYearFraction(), 1, getFundingCurveName());
  }

  @Override
  public String toString() {
    return "FixedCouponPayment[" + _coupon + ", notional = " + _notional + ", t = " + _yearFraction + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_coupon);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_yearFraction);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

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
    final FixedCouponPayment other = (FixedCouponPayment) obj;
    if (Double.doubleToLongBits(_coupon) != Double.doubleToLongBits(other._coupon)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    return Double.doubleToLongBits(_yearFraction) == Double.doubleToLongBits(other._yearFraction);
  }

  @Override
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitFixedCouponPayment(this, data);
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitFixedCouponPayment(this);
  }
}
