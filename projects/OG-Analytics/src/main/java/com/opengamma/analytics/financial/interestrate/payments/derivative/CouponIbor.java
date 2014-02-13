/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a single currency Ibor-like coupon.
 */
public class CouponIbor extends CouponFloating implements DepositIndexCoupon<IborIndex> {

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
   * The forward curve name used in to estimate the fixing index.
   */
  private final String _forwardCurveName;

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
   * @param forwardCurveName Name of the forward (or estimation) curve.
   * @deprecated Use the constructor that does not take yield curve names.
   */
  @Deprecated
  public CouponIbor(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double notional, final double fixingTime,
      final IborIndex index, final double fixingPeriodStartTime, final double fixingPeriodEndTime, final double fixingYearFraction, final String forwardCurveName) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional, fixingTime);
    ArgumentChecker.isTrue(fixingPeriodStartTime >= fixingTime, "fixing period start < fixing time");
    ArgumentChecker.isTrue(fixingPeriodEndTime >= fixingPeriodStartTime, "fixing period end < fixing period start");
    ArgumentChecker.isTrue(fixingYearFraction >= 0, "forward year fraction < 0");
    ArgumentChecker.notNull(forwardCurveName, "forward curve name");
    ArgumentChecker.notNull(index, "Index");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "Index currency incompatible with coupon currency");
    _fixingPeriodStartTime = fixingPeriodStartTime;
    _fixingPeriodEndTime = fixingPeriodEndTime;
    _fixingAccrualFactor = fixingYearFraction;
    _forwardCurveName = forwardCurveName;
    _index = index;
  }

  /**
   * Constructor from all details.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   * @param index The Ibor-like index on which the coupon fixes.
   * @param fixingPeriodStartTime The fixing period start time (in years).
   * @param fixingPeriodEndTime The fixing period end time (in years).
   * @param fixingYearFraction The year fraction (or accrual factor) for the fixing period.
   */
  public CouponIbor(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final double fixingTime,
      final IborIndex index, final double fixingPeriodStartTime, final double fixingPeriodEndTime, final double fixingYearFraction) {
    super(currency, paymentTime, paymentYearFraction, notional, fixingTime);
    ArgumentChecker.isTrue(fixingPeriodStartTime >= fixingTime, "fixing period start < fixing time");
    ArgumentChecker.isTrue(fixingPeriodEndTime >= fixingPeriodStartTime, "fixing period end < fixing period start");
    ArgumentChecker.isTrue(fixingYearFraction >= 0, "forward year fraction < 0");
    ArgumentChecker.notNull(index, "Index");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "Index currency incompatible with coupon currency");
    _fixingPeriodStartTime = fixingPeriodStartTime;
    _fixingPeriodEndTime = fixingPeriodEndTime;
    _fixingAccrualFactor = fixingYearFraction;
    _forwardCurveName = null;
    _index = index;
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
  public double getFixingAccrualFactor() {
    return _fixingAccrualFactor;
  }

  /**
   * Gets the forward curve name.
   * @return The name.
   * @deprecated Curve names should no longer be set in {@link InstrumentDefinition}s
   */
  @Deprecated
  public String getForwardCurveName() {
    if (_forwardCurveName == null) {
      throw new IllegalStateException("Forward curve name was not set");
    }
    return _forwardCurveName;
  }

  /**
   * Gets the Ibor-like index.
   * @return The index.
   */
  @Override
  public IborIndex getIndex() {
    return _index;
  }

  @SuppressWarnings("deprecation")
  @Override
  public CouponIbor withNotional(final double notional) {
    try {
      return new CouponIbor(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), notional, getFixingTime(), _index, getFixingPeriodStartTime(), getFixingPeriodEndTime(),
          getFixingAccrualFactor(), getForwardCurveName());
    } catch (final IllegalStateException e) {
      return new CouponIbor(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getFixingTime(), _index, getFixingPeriodStartTime(), getFixingPeriodEndTime(),
          getFixingAccrualFactor());
    }
  }

  @Override
  public String toString() {
    return "CouponIbor: " + super.toString() + ", fixing : [" + _fixingPeriodStartTime + " - " + _fixingPeriodEndTime + " - " + _fixingAccrualFactor + "], forward curve = " + _forwardCurveName;
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
    return true;
  }

  @SuppressWarnings("deprecation")
  public CouponFixed withUnitCoupon() {
    try {
      return new CouponFixed(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), getNotional(), 1.0);
    } catch (final IllegalStateException e) {
      return new CouponFixed(getCurrency(), getPaymentTime(), getPaymentYearFraction(), getNotional(), 1.0);
    }
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
