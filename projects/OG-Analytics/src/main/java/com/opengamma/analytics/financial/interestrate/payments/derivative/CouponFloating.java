/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a generic floating coupon with a unique fixing date.
 */
public abstract class CouponFloating extends Coupon {

  /**
   * The floating coupon fixing time.
   */
  private final double _fixingTime;

  /**
   * Constructor from all the details.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param fundingCurveName Name of the funding curve.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   * @deprecated Use the constructor that does not take a curve name
   */
  @Deprecated
  public CouponFloating(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double notional, final double fixingTime) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional);
    ArgumentChecker.isTrue(fixingTime >= 0.0, "fixing time < 0");
    _fixingTime = fixingTime;
  }

  /**
   * Constructor from all the details.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   */
  public CouponFloating(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final double fixingTime) {
    super(currency, paymentTime, paymentYearFraction, notional);
    ArgumentChecker.isTrue(fixingTime >= 0.0, "fixing time < 0");
    _fixingTime = fixingTime;
  }

  /**
   * Gets the floating coupon fixing time.
   * @return The fixing time.
   */
  public double getFixingTime() {
    return _fixingTime;
  }

  @Override
  public String toString() {
    return super.toString() + ", fixing time = " + _fixingTime;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingTime);
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
    final CouponFloating other = (CouponFloating) obj;
    if (Double.doubleToLongBits(_fixingTime) != Double.doubleToLongBits(other._fixingTime)) {
      return false;
    }
    return true;
  }

}
