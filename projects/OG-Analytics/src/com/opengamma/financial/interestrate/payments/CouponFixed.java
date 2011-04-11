/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 * Class describing a fixed coupon.
 */
public class CouponFixed extends PaymentFixed {
  private final double _fixedRate;
  private final double _notional;
  private final double _paymentYearFraction;
  /**
   * The start date of the coupon accrual period. Can be null if of no use.
   */
  private final ZonedDateTime _accrualStartDate;
  /**
   * The end date of the coupon accrual period. Can be null if of no use.
   */
  private final ZonedDateTime _accrualEndDate;

  /**
   * Constructor from all details but accrual dates.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param fundingCurveName Name of the funding curve.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param rate The coupon fixed rate.
   */
  public CouponFixed(Currency currency, double paymentTime, String fundingCurveName, double paymentYearFraction, double notional, final double rate) {
    super(currency, paymentTime, paymentYearFraction * notional * rate, fundingCurveName);
    _fixedRate = rate;
    _notional = notional;
    Validate.isTrue(paymentYearFraction >= 0, "payment year fraction < 0");
    _paymentYearFraction = paymentYearFraction;
    _accrualStartDate = null;
    _accrualEndDate = null;
  }

  /**
   * Constructor from all details.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param fundingCurveName Name of the funding curve.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param rate The coupon fixed rate.
   * @param accrualStartDate The start date of the coupon accrual period.
   * @param accrualEndDate The end date of the coupon accrual period.
   * @
   */
  public CouponFixed(Currency currency, double paymentTime, String fundingCurveName, double paymentYearFraction, double notional, final double rate, ZonedDateTime accrualStartDate,
      ZonedDateTime accrualEndDate) {
    super(currency, paymentTime, paymentYearFraction * notional * rate, fundingCurveName);
    _fixedRate = rate;
    _notional = notional;
    Validate.isTrue(paymentYearFraction >= 0, "payment year fraction < 0");
    _paymentYearFraction = paymentYearFraction;
    _accrualStartDate = accrualStartDate;
    _accrualEndDate = accrualEndDate;
  }

  /**
   * Constructor from details with notional defaulted to 1.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param fundingCurveName Name of the funding curve.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param rate The coupon fixed rate.
   */
  public CouponFixed(Currency currency, double paymentTime, String fundingCurveName, double paymentYearFraction, final double rate) {
    this(currency, paymentTime, fundingCurveName, paymentYearFraction, 1.0, rate);
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

  /**
   * Gets the _accrualStartDate field.
   * @return The accrual start date.
   */
  public ZonedDateTime getAccrualStartDate() {
    return _accrualStartDate;
  }

  /**
   * Gets the _accrualEndDate field.
   * @return The accrual end date.
   */
  public ZonedDateTime getAccrualEndDate() {
    return _accrualEndDate;
  }

  public CouponFixed withUnitCoupon() {
    return new CouponFixed(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), getNotional(), 1);
  }

  @Override
  public String toString() {
    return super.toString() + ", [Rate=" + _fixedRate + ", notional=" + getNotional() + ", year fraction=" + getPaymentYearFraction() + "]";
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
