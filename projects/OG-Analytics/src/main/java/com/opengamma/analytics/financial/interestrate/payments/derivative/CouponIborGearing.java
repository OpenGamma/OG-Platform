/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * Class describing a Ibor-like floating coupon with a gearing (multiplicative) factor and a spread. The coupon payment is: notional * accrual factor * (factor * Ibor + spread).
 */
public class CouponIborGearing extends CouponFloating {

  /**
   * Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
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
   * The spread paid above the Ibor rate.
   */
  private final double _spread;
  /**
   * The fixed amount related to the spread.
   */
  private final double _spreadAmount;
  /**
   * The gearing (multiplicative) factor applied to the Ibor rate.
   */
  private final double _factor;
  /**
   * The forward curve name used in to estimate the fixing index.
   */
  private final String _forwardCurveName;

  /**
   * Constructor from all the details.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param discountingCurveName Name of the funding curve.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   * @param index Ibor-like index on which the coupon fixes. The index currency should be the same as the index currency.
   * @param fixingPeriodStartTime The fixing period start time (in years).
   * @param fixingPeriodEndTime The fixing period end time (in years).
   * @param fixingAccrualFactor The fixing period accrual factor (or year fraction) in the fixing convention.
   * @param spread The spread paid above the Ibor rate.
   * @param factor The gearing (multiplicative) factor applied to the Ibor rate.
   * @param forwardCurveName The forward curve name used in to estimate the fixing index.
   * @deprecated Use the constructor that does not take yield curve names.
   */
  @Deprecated
  public CouponIborGearing(final Currency currency, final double paymentTime, final String discountingCurveName, final double paymentYearFraction,
      final double notional, final double fixingTime, final IborIndex index, final double fixingPeriodStartTime, final double fixingPeriodEndTime,
      final double fixingAccrualFactor, final double spread, final double factor, final String forwardCurveName) {
    super(currency, paymentTime, discountingCurveName, paymentYearFraction, notional, fixingTime);
    ArgumentChecker.notNull(index, "Index");
    ArgumentChecker.notNull(forwardCurveName, "Forward curve");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "currency does not match index currency");
    _index = index;
    _fixingPeriodStartTime = fixingPeriodStartTime;
    _fixingPeriodEndTime = fixingPeriodEndTime;
    _fixingAccrualFactor = fixingAccrualFactor;
    _spread = spread;
    _factor = factor;
    _forwardCurveName = forwardCurveName;
    _spreadAmount = getNotional() * getPaymentYearFraction() * spread;
  }

  /**
   * Constructor from all the details.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   * @param index Ibor-like index on which the coupon fixes. The index currency should be the same as the index currency.
   * @param fixingPeriodStartTime The fixing period start time (in years).
   * @param fixingPeriodEndTime The fixing period end time (in years).
   * @param fixingAccrualFactor The fixing period accrual factor (or year fraction) in the fixing convention.
   * @param spread The spread paid above the Ibor rate.
   * @param factor The gearing (multiplicative) factor applied to the Ibor rate.
   */
  public CouponIborGearing(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final double fixingTime, final IborIndex index,
      final double fixingPeriodStartTime, final double fixingPeriodEndTime, final double fixingAccrualFactor, final double spread, final double factor) {
    super(currency, paymentTime, paymentYearFraction, notional, fixingTime);
    ArgumentChecker.notNull(index, "Index");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "currency does not match index currency");
    _index = index;
    _fixingPeriodStartTime = fixingPeriodStartTime;
    _fixingPeriodEndTime = fixingPeriodEndTime;
    _fixingAccrualFactor = fixingAccrualFactor;
    _spread = spread;
    _factor = factor;
    _forwardCurveName = null;
    _spreadAmount = getNotional() * getPaymentYearFraction() * spread;
  }

  /**
   * Gets the Ibor index.
   * @return The index.
   */
  public IborIndex getIndex() {
    return _index;
  }

  /**
   * Gets the fixing period start date.
   * @return The fixing period start date.
   */
  public double getFixingPeriodStartTime() {
    return _fixingPeriodStartTime;
  }

  /**
   * Gets the fixing period end date.
   * @return The fixing period end date.
   */
  public double getFixingPeriodEndTime() {
    return _fixingPeriodEndTime;
  }

  /**
   * Gets the fixing period accrual factor.
   * @return The fixing period accrual factor.
   */
  public double getFixingAccrualFactor() {
    return _fixingAccrualFactor;
  }

  /**
   * Gets the spread.
   * @return The spread.
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
   * Gets the factor.
   * @return The factor.
   */
  public double getFactor() {
    return _factor;
  }

  /**
   * Gets the forward curve name.
   * @return the _forward curve name
   * @deprecated Curve names should no longer be set in {@link InstrumentDefinition}s
   */
  @Deprecated
  public String getForwardCurveName() {
    if (_forwardCurveName == null) {
      throw new IllegalStateException("Forward curve name was not set");
    }
    return _forwardCurveName;
  }

  @SuppressWarnings("deprecation")
  @Override
  public CouponIborGearing withNotional(final double notional) {
    try {
      return new CouponIborGearing(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), notional, getFixingTime(), _index, _fixingPeriodStartTime, _fixingPeriodEndTime,
          _fixingAccrualFactor, _spread, _factor, _forwardCurveName);
    } catch (final IllegalStateException e) {
      return new CouponIborGearing(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getFixingTime(), _index, _fixingPeriodStartTime, _fixingPeriodEndTime,
          _fixingAccrualFactor, _spread, _factor);
    }
  }

  @Override
  public String toString() {
    return "CouponIborGearing: " + super.toString() + ", fixing: [" + _fixingPeriodStartTime + " - " + _fixingPeriodEndTime + " - " + _fixingAccrualFactor + "], spread: " + _spread + ", factor: "
        + _factor + ",forward curve: " + _forwardCurveName;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCouponIborGearing(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCouponIborGearing(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_factor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingPeriodEndTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingPeriodStartTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (_forwardCurveName == null ? 0 : _forwardCurveName.hashCode());
    result = prime * result + _index.hashCode();
    temp = Double.doubleToLongBits(_spread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_spreadAmount);
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
    final CouponIborGearing other = (CouponIborGearing) obj;
    if (Double.doubleToLongBits(_factor) != Double.doubleToLongBits(other._factor)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingAccrualFactor) != Double.doubleToLongBits(other._fixingAccrualFactor)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodEndTime) != Double.doubleToLongBits(other._fixingPeriodEndTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodStartTime) != Double.doubleToLongBits(other._fixingPeriodStartTime)) {
      return false;
    }
    if (!ObjectUtils.equals(_forwardCurveName, other._forwardCurveName)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    if (Double.doubleToLongBits(_spreadAmount) != Double.doubleToLongBits(other._spreadAmount)) {
      return false;
    }
    return true;
  }

}
