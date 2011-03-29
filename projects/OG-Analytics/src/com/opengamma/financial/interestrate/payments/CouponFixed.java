/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;

/**
 * Class describing a fixed coupon.
 */
public class CouponFixed extends PaymentFixed {
  private final double _fixedRate;
  private final double _notional;
  private final double _paymentYearFraction;

  /**
   * Constructor from all details.
   * @param paymentTime Time (in years) up to the payment.
   * @param fundingCurveName Name of the funding curve.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param rate The coupon fixed rate.
   */
  public CouponFixed(double paymentTime, String fundingCurveName, double paymentYearFraction, double notional, final double rate) {
    super(paymentTime, paymentYearFraction * notional * rate, fundingCurveName);
    _fixedRate = rate;
    _notional = notional;
    Validate.isTrue(paymentYearFraction >= 0, "payment year fraction < 0");
    _paymentYearFraction = paymentYearFraction;
  }

  /**
   * Constructor from details with notional defaulted to 1.
   * @param paymentTime Time (in years) up to the payment.
   * @param fundingCurveName Name of the funding curve.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param rate The coupon fixed rate.
   */
  public CouponFixed(double paymentTime, String fundingCurveName, double paymentYearFraction, final double rate) {
    this(paymentTime, fundingCurveName, paymentYearFraction, 1.0, rate);
  }

  /**
   * Gets the _fixedRate field.
   * @return the _fixedRate
   */
  public double getFixedRate() {
    return _fixedRate;
  }

  /**
   * Gets the _notional field.
   * @return the _notional
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the _paymentYearFraction field.
   * @return the _paymentYearFraction
   */
  public double getPaymentYearFraction() {
    return _paymentYearFraction;
  }

  public CouponFixed withUnitCoupon() {
    return new CouponFixed(getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), getNotional(), 1);
  }

  @Override
  public String toString() {
    return "FixedCouponPayment[t = " + getPaymentTime() + ", " + _fixedRate + ", notional = " + getNotional() + ", t = " + getPaymentYearFraction() + "]";
  }

  @Override
  public double getReferenceAmount() {
    return _notional;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixedRate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_paymentYearFraction);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CouponFixed other = (CouponFixed) obj;
    if (Double.doubleToLongBits(_fixedRate) != Double.doubleToLongBits(other._fixedRate)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentYearFraction) != Double.doubleToLongBits(other._paymentYearFraction)) {
      return false;
    }
    return true;
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
