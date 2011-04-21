/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 * Class describing a generic coupon.
 */
public class Coupon extends Payment {

  /**
   * The payment period year fraction (or accrual factor).
   */
  private final double _paymentYearFraction;
  /**
   * The coupon notional.
   */
  private final double _notional;

  /**
   * Constructor of a generic coupon from details.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param fundingCurveName Name of the funding curve.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   */
  public Coupon(Currency currency, double paymentTime, String fundingCurveName, double paymentYearFraction, double notional) {
    super(currency, paymentTime, fundingCurveName);
    Validate.isTrue(paymentYearFraction >= 0, "year fraction < 0");
    _paymentYearFraction = paymentYearFraction;
    _notional = notional;
  }

  /**
   * Gets the payment year fraction (or accrual factor).
   * @return The payment year fraction.
   */
  public double getPaymentYearFraction() {
    return _paymentYearFraction;
  }

  /**
   * Gets the coupon notional.
   * @return The notional.
   */
  public double getNotional() {
    return _notional;
  }

  @Override
  public double getReferenceAmount() {
    return _notional;
  }

  @Override
  public String toString() {
    return super.toString() + ", year fraction = " + getPaymentYearFraction() + ", notional = " + _notional;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
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
    Coupon other = (Coupon) obj;
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
    return null;
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<?, T> visitor) {
    return null;
  }

}
