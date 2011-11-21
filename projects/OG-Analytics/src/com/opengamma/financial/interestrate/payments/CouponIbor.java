/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 * Class describing an Ibor-like coupon.
 */
public class CouponIbor extends CouponFloating {

  /**
   * The Ibor-like index on which the coupon fixes. The index currency should be the same as the index currency.
   */
  private final IborIndex _index;
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
  private final double _fixingAccrualFactor;
  /**
   * The spread paid above Ibor.
   */
  private final double _spread;
  /**
   * The fixed amount related to the spread.
   */
  private final double _spreadAmount;
  /**
   * The forward curve name used in to estimate the fixing index.
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
   * @param index The Ibor-like index on which the coupon fixes.
   * @param fixingPeriodStartTime The fixing period start time (in years).
   * @param fixingPeriodEndTime The fixing period end time (in years).
   * @param fixingYearFraction The year fraction (or accrual factor) for the fixing period.
   * @param spread The spread.
   * @param forwardCurveName Name of the forward (or estimation) curve.
   */
  public CouponIbor(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double notional, final double fixingTime,
      IborIndex index, final double fixingPeriodStartTime, final double fixingPeriodEndTime, final double fixingYearFraction, final double spread, final String forwardCurveName) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional, fixingTime);
    Validate.isTrue(fixingPeriodStartTime >= fixingTime, "fixing period start < fixing time");
    _fixingPeriodStartTime = fixingPeriodStartTime;
    Validate.isTrue(fixingPeriodEndTime >= fixingPeriodStartTime, "fixing period end < fixing period start");
    _fixingPeriodEndTime = fixingPeriodEndTime;
    Validate.isTrue(fixingYearFraction >= 0, "forward year fraction < 0");
    _fixingAccrualFactor = fixingYearFraction;
    Validate.notNull(forwardCurveName);
    _forwardCurveName = forwardCurveName;
    _spread = spread;
    _spreadAmount = _spread * getPaymentYearFraction() * getNotional();
    _index = index;
  }

  /**
   * Constructor from details with spread defaulted to 0.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param fundingCurveName Name of the funding curve.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   * @param index The Ibor-like index on which the coupon fixes.
   * @param fixingPeriodStartTime The fixing period start time (in years).
   * @param fixingPeriodEndTime Time (in years) up to the end of the fixing period.
   * @param fixingYearFraction The year fraction (or accrual factor) for the fixing period.
   * @param forwardCurveName Name of the forward (or estimation) curve.
   */
  public CouponIbor(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double notional, final double fixingTime,
      IborIndex index, final double fixingPeriodStartTime, final double fixingPeriodEndTime, final double fixingYearFraction, final String forwardCurveName) {
    this(currency, paymentTime, fundingCurveName, paymentYearFraction, notional, fixingTime, index, fixingPeriodStartTime, fixingPeriodEndTime, fixingYearFraction, 0.0, forwardCurveName);
  }

  /**
   * Gets the fixing period start time (in years).
   * @return The fixing period start time.
   */
  public double getFixingPeriodStartTime() {
    return _fixingPeriodStartTime;
  }

  /**
   * Gets the fixing period end time (in years).
   * @return The fixing period end time.
   */
  public double getFixingPeriodEndTime() {
    return _fixingPeriodEndTime;
  }

  /**
   * Gets the accrual factor for the fixing period.
   * @return The accrual factor.
   */
  public double getFixingYearFraction() {
    return _fixingAccrualFactor;
  }

  /**
   * Gets the _spread field.
   * @return the _spread
   */
  public double getSpread() {
    return _spread;
  }

  /**
   * Gets the spread amount.
   * @return The spread amount.
   */
  public double getSpreadAmount() {
    return _spreadAmount;
  }

  /**
   * Gets the forward curve name.
   * @return The name.
   */
  public String getForwardCurveName() {
    return _forwardCurveName;
  }

  /**
   * Gets the Ibor-like index.
   * @return The index.
   */
  public IborIndex getIndex() {
    return _index;
  }

  public CouponIbor withZeroSpread() {
    if (getSpread() == 0.0) {
      return this;
    }
    return withSpread(0.0);
  }

  @Override
  public CouponIbor withNotional(double notional) {
    return new CouponIbor(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), notional, getFixingTime(), _index, getFixingPeriodStartTime(), getFixingPeriodEndTime(),
        getFixingYearFraction(), getSpread(), getForwardCurveName());
  }

  @Override
  public String toString() {
    return super.toString() + ", fixing : [" + _fixingPeriodStartTime + " - " + _fixingPeriodEndTime + " - " + _fixingAccrualFactor + "], spread = " + _spread + ", forward curve = "
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
    temp = Double.doubleToLongBits(_fixingAccrualFactor);
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
    if (Double.doubleToLongBits(_fixingAccrualFactor) != Double.doubleToLongBits(other._fixingAccrualFactor)) {
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
    return new CouponIbor(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), getNotional(), getFixingTime(), _index, getFixingPeriodStartTime(),
        getFixingPeriodEndTime(), getFixingYearFraction(), spread, getForwardCurveName());
  }

  public CouponFixed withUnitCoupon() {
    return new CouponFixed(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), getNotional(), 1.0);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCouponIbor(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCouponIbor(this);
  }

}
