/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 * Class describing a fixed coupon.
 */
public class CouponFixed extends Coupon {

  /**
   * The coupon fixed rate.
   */
  private final double _fixedRate;
  /**
  * The paid amount.
  */
  private final double _amount;
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
  public CouponFixed(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double notional, final double rate) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional);
    _fixedRate = rate;
    _accrualStartDate = null;
    _accrualEndDate = null;
    _amount = paymentYearFraction * notional * rate;
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
   */
  public CouponFixed(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double notional, final double rate,
      final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional);
    _fixedRate = rate;
    _amount = paymentYearFraction * notional * rate;
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
  public CouponFixed(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double rate) {
    this(currency, paymentTime, fundingCurveName, paymentYearFraction, 1.0, rate);
  }

  /**
   * Gets the coupon fixed rate.
   * @return The fixed rate.
   */
  public double getFixedRate() {
    return _fixedRate;
  }

  /**
   * Gets the start date of the coupon accrual period.
   * @return The accrual start date.
   */
  public ZonedDateTime getAccrualStartDate() {
    return _accrualStartDate;
  }

  /**
   * Gets the end date of the coupon accrual period.
   * @return The accrual end date.
   */
  public ZonedDateTime getAccrualEndDate() {
    return _accrualEndDate;
  }

  /**
   * Gets the paid amount.
   * @return The amount.
   */
  public double getAmount() {
    return _amount;
  }

  /**
   * Creates a new coupon with the same characteristics, except the rate which is 1.0.
   * @return The new coupon.
   */
  public CouponFixed withUnitCoupon() {
    return new CouponFixed(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), getNotional(), 1);
  }

  @Override
  public CouponFixed withNotional(double notional) {
    return new CouponFixed(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), notional, getFixedRate(), getAccrualStartDate(), getAccrualEndDate());
  }

  /**
   * Returns a fixed payment with the same features (currency, payment time, amount) as the fixed coupon.
   * @return A fixed payment.
   */
  public PaymentFixed toPaymentFixed() {
    return new PaymentFixed(getCurrency(), getPaymentTime(), _amount, getFundingCurveName());
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitFixedCouponPayment(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitFixedCouponPayment(this);
  }

  @Override
  public String toString() {
    return super.toString() + ", [Rate=" + _fixedRate + ", notional=" + getNotional() + ", year fraction=" + getPaymentYearFraction() + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_accrualEndDate == null) ? 0 : _accrualEndDate.hashCode());
    result = prime * result + ((_accrualStartDate == null) ? 0 : _accrualStartDate.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_amount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixedRate);
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
    if (Double.doubleToLongBits(_amount) != Double.doubleToLongBits(other._amount)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixedRate) != Double.doubleToLongBits(other._fixedRate)) {
      return false;
    }
    if (!ObjectUtils.equals(_accrualEndDate, other._accrualEndDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_accrualStartDate, other._accrualStartDate)) {
      return false;
    }
    return true;
  }

}
