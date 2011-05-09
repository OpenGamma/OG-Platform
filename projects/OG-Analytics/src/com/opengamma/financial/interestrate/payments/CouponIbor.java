/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 * Class describing an Ibor-like coupon.
 */
public class CouponIbor extends CouponFloating {

  /**
   * Ibor-like index on which the coupon fixes. The index currency should be the same as the index currency.
   */
  //  private final IborIndex _index;
  // TODO: add Ibor index
  /**
   * The fixing period start time (in years).
   */
  private final double _fixingPeriodStartTime;
  /**
   * The fixing period end time (in years).
   */
  private final double _fixingPeriodEndTime;
  /**
   * The fixing period year fraction (or accrual factor) in the fixing convention.
   */
  private final double _fixingYearFraction;
  /**
   * The spread paid above Ibor.
   */
  private final double _spread;
  /**
   * The forward curve name used in to estimate the fixing index..
   */
  private final String _forwardCurveName;

  //TODO: Should the spread be in CouponIbor or in a more generic coupon?

  /**
   * Constructor from all details.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param fundingCurveName Name of the funding curve.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   * @param fixingPeriodStartTime Time (in years) up to the start of the fixing period.
   * @param fixingPeriodEndTime Time (in years) up to the end of the fixing period.
   * @param fixingYearFraction The year fraction (or accrual factor) for the fixing period.
   * @param spread The spread.
   * @param forwardCurveName Name of the forward (or estimation) curve.
   */
  public CouponIbor(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double notional, final double fixingTime,
      final double fixingPeriodStartTime, final double fixingPeriodEndTime, final double fixingYearFraction, final double spread, final String forwardCurveName) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional, fixingTime);
    Validate.isTrue(fixingPeriodStartTime >= fixingTime, "fixing period start < fixing time");
    _fixingPeriodStartTime = fixingPeriodStartTime;
    Validate.isTrue(fixingPeriodEndTime >= fixingPeriodStartTime, "fixing period end < fixing period start");
    _fixingPeriodEndTime = fixingPeriodEndTime;
    Validate.isTrue(fixingYearFraction >= 0, "forward year fraction < 0");
    _fixingYearFraction = fixingYearFraction;
    Validate.notNull(forwardCurveName);
    _forwardCurveName = forwardCurveName;
    _spread = spread;
  }

  /**
   * Constructor from details with spread defaulted to 0.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param fundingCurveName Name of the funding curve.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   * @param fixingPeriodStartTime Time (in years) up to the start of the fixing period.
   * @param fixingPeriodEndTime Time (in years) up to the end of the fixing period.
   * @param fixingYearFraction The year fraction (or accrual factor) for the fixing period.
   * @param forwardCurveName Name of the forward (or estimation) curve.
   */
  public CouponIbor(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double notional, final double fixingTime,
      final double fixingPeriodStartTime,
      final double fixingPeriodEndTime, final double fixingYearFraction, final String forwardCurveName) {
    this(currency, paymentTime, fundingCurveName, paymentYearFraction, notional, fixingTime, fixingPeriodStartTime, fixingPeriodEndTime, fixingYearFraction, 0.0, forwardCurveName);
  }

  /**
   * Gets the _fixingPeriodStartTime field.
   * @return the _fixingPeriodStartTime
   */
  public double getFixingPeriodStartTime() {
    return _fixingPeriodStartTime;
  }

  /**
   * Gets the _fixingPeriodEndTime field.
   * @return the _fixingPeriodEndTime
   */
  public double getFixingPeriodEndTime() {
    return _fixingPeriodEndTime;
  }

  /**
   * Gets the _fixingYearFraction field.
   * @return the _fixingYearFraction
   */
  public double getFixingYearFraction() {
    return _fixingYearFraction;
  }

  /**
   * Gets the _spread field.
   * @return the _spread
   */
  public double getSpread() {
    return _spread;
  }

  /**
   * Gets the _forwardCurveName field.
   * @return the _forwardCurveName
   */
  public String getForwardCurveName() {
    return _forwardCurveName;
  }

  public CouponIbor withZeroSpread() {
    if (getSpread() == 0.0) {
      return this;
    }
    return withSpread(0.0);
  }

  @Override
  public String toString() {
    return super.toString() + ", fixing : [" + _fixingPeriodStartTime + " - " + _fixingPeriodEndTime + " - " + _fixingYearFraction + "], spread = " + _spread + ", forward curve = "
        + _forwardCurveName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodEndTime);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_fixingPeriodStartTime);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(_fixingYearFraction);
    result = prime * result + (int) (temp ^ temp >>> 32);
    result = prime * result + (_forwardCurveName == null ? 0 : _forwardCurveName.hashCode());
    temp = Double.doubleToLongBits(_spread);
    result = prime * result + (int) (temp ^ temp >>> 32);
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
    final CouponIbor other = (CouponIbor) obj;
    if (Double.doubleToLongBits(_fixingPeriodEndTime) != Double.doubleToLongBits(other._fixingPeriodEndTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodStartTime) != Double.doubleToLongBits(other._fixingPeriodStartTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingYearFraction) != Double.doubleToLongBits(other._fixingYearFraction)) {
      return false;
    }
    if (!ObjectUtils.equals(_forwardCurveName, other._forwardCurveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    return true;
  }

  public CouponIbor withSpread(final double spread) {
    return new CouponIbor(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), getNotional(), getFixingTime(), getFixingPeriodStartTime(), getFixingPeriodEndTime(),
        getFixingYearFraction(), spread, getForwardCurveName());
  }

  public CouponFixed withUnitCoupon() {
    return new CouponFixed(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), getNotional(), 1.0);
  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCouponIbor(this, data);
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitCouponIbor(this);
  }

}
