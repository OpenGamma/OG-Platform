/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class FixedCouponPayment implements FixedPayment {

  private final double _time;
  private final double _yearFraction;
  private final double _coupon;

  public FixedCouponPayment(double paymentTime, double yearFraction, double coupon) {

    Validate.isTrue(paymentTime >= 0.0, "Payment time < 0");
    Validate.isTrue(yearFraction > 0.0, "year fraction < 0");

    _time = paymentTime;
    _yearFraction = yearFraction;
    _coupon = coupon;
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

  @Override
  public double getPaymentTime() {
    return _time;
  }

  @Override
  public double getAmount() {
    return _yearFraction * _coupon;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_coupon);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_time);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_yearFraction);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    FixedCouponPayment other = (FixedCouponPayment) obj;
    if (Double.doubleToLongBits(_coupon) != Double.doubleToLongBits(other._coupon)) {
      return false;
    }
    if (Double.doubleToLongBits(_time) != Double.doubleToLongBits(other._time)) {
      return false;
    }
    if (Double.doubleToLongBits(_yearFraction) != Double.doubleToLongBits(other._yearFraction)) {
      return false;
    }
    return true;
  }

  @Override
  public <S, T> T accept(PaymentVisitor<S, T> visitor, S data) {
    return visitor.visitFixedCouponPayment(this, data);
  }

}
