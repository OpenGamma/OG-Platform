/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import java.util.Arrays;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *  Class describing a ON compounded floating coupon (Brazilian OverNight like coupon).
 *
 */
public class CouponONCompounded extends Coupon implements DepositIndexCoupon<IndexON> {

  /**
   * The OIS-like index on which the coupon fixes. The index currency should be the same as the coupon currency. Not null.
   */
  private final IndexON _index;
  /**
   * The fixing period start time (in years). The fixing period does take into account the already fixed period,
   * i.e. the fixing period start time is the first date for which the coupon is not fixed yet.
   */
  private final double[] _fixingPeriodStartTimes;
  /**
   * The fixing period end time (in years).
   */
  private final double[] _fixingPeriodEndTimes;
  /**
   * The accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   */
  private final double[] _fixingPeriodAccrualFactors;

  /**
   * The notional augmented by the interest accrued over the periods already fixed.
   */
  private final double _notionalAccrued;

  /**
   * The forward curve name used in to estimate the fixing index.
   */
  private final String _forwardCurveName;

  /**
   * Constructor of a generic coupon from details. Deprecated version using the funding curve name.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param fundingCurveName Name of the funding curve.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param index The OIS-like index on which the coupon fixes. Not null.
   * @param fixingPeriodStartTimes The fixing period start time (in years).
   * @param fixingPeriodEndTimes The fixing period end time (in years).
   * @param fixingPeriodAccrualFactors The accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   * @param notionalAccrued the notional accrued.
   * @param forwardCurveName The name of the forward curve.
   * @deprecated Use the constructor that does not take yield curve names
   */
  @Deprecated
  public CouponONCompounded(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double notional, final IndexON index,
      final double[] fixingPeriodStartTimes, final double[] fixingPeriodEndTimes, final double[] fixingPeriodAccrualFactors, final double notionalAccrued,
      final String forwardCurveName) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional);
    ArgumentChecker.notNull(index, "Coupon OIS: index");
    _index = index;
    _fixingPeriodStartTimes = fixingPeriodStartTimes;
    _fixingPeriodEndTimes = fixingPeriodEndTimes;
    _fixingPeriodAccrualFactors = fixingPeriodAccrualFactors;
    _notionalAccrued = notionalAccrued;
    _forwardCurveName = forwardCurveName;
  }

  /**
   * Constructor of a generic coupon from details. Same as the previous constructor but not using the funding curve name.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param index The OIS-like index on which the coupon fixes. Not null.
   * @param fixingPeriodStartTimes The fixing period start time (in years).
   * @param fixingPeriodEndTimes The fixing period end time (in years).
   * @param fixingPeriodAccrualFactors The accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   * @param notionalAccrued the notional accrued.
   */
  public CouponONCompounded(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final IndexON index,
      final double[] fixingPeriodStartTimes, final double[] fixingPeriodEndTimes, final double[] fixingPeriodAccrualFactors, final double notionalAccrued) {
    super(currency, paymentTime, paymentYearFraction, notional);
    ArgumentChecker.notNull(index, "Coupon ON conpounded : index");
    _index = index;
    _fixingPeriodStartTimes = fixingPeriodStartTimes;
    _fixingPeriodEndTimes = fixingPeriodEndTimes;
    _fixingPeriodAccrualFactors = fixingPeriodAccrualFactors;
    _notionalAccrued = notionalAccrued;
    _forwardCurveName = null;
  }

  /**
   * Gets the OIS index of the instrument.
   * @return The index.
   */
  @Override
  public IndexON getIndex() {
    return _index;
  }

  /**
   * Gets the fixing period start time (in years).
   * @return The fixing period start time.
   */
  public double[] getFixingPeriodStartTimes() {
    return _fixingPeriodStartTimes;
  }

  /**
   * Gets the fixing period end time (in years).
   * @return The fixing period end time.
   */
  public double[] getFixingPeriodEndTimes() {
    return _fixingPeriodEndTimes;
  }

  /**
   * Gets the accrual factor for the fixing period.
   * @return The accrual factor.
   */
  public double[] getFixingPeriodAccrualFactors() {
    return _fixingPeriodAccrualFactors;
  }

  /**
   * Gets the notional augmented by the interest accrued over the periods already fixed.
   * @return The augmented notional.
   */
  public double getNotionalAccrued() {
    return _notionalAccrued;
  }

  /**
   * Gets the forward curve name.
   * @return The name.
   */
  public String getForwardCurveName() {
    return _forwardCurveName;
  }

  @SuppressWarnings("deprecation")
  @Override
  public CouponONCompounded withNotional(final double notional) {
    return new CouponONCompounded(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), notional, _index, _fixingPeriodStartTimes, _fixingPeriodEndTimes,
        _fixingPeriodAccrualFactors, _notionalAccrued, _forwardCurveName);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponONCompounded(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponONCompounded(this);
  }

  @Override
  public String toString() {
    return "CouponONCompounded [_fixingPeriodStartTimes=" + Arrays.toString(_fixingPeriodStartTimes) + ", _fixingPeriodEndTimes=" + Arrays.toString(_fixingPeriodEndTimes) +
        ", _fixingPeriodAccrualFactors=" + Arrays.toString(_fixingPeriodAccrualFactors) + ", _fixingPeriodAccrualFactorsActAct=" + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_fixingPeriodAccrualFactors);
    result = prime * result + Arrays.hashCode(_fixingPeriodEndTimes);
    result = prime * result + Arrays.hashCode(_fixingPeriodStartTimes);
    result = prime * result + ((_forwardCurveName == null) ? 0 : _forwardCurveName.hashCode());
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_notionalAccrued);
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
    final CouponONCompounded other = (CouponONCompounded) obj;
    if (!Arrays.equals(_fixingPeriodAccrualFactors, other._fixingPeriodAccrualFactors)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodEndTimes, other._fixingPeriodEndTimes)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodStartTimes, other._fixingPeriodStartTimes)) {
      return false;
    }
    if (_forwardCurveName == null) {
      if (other._forwardCurveName != null) {
        return false;
      }
    } else if (!_forwardCurveName.equals(other._forwardCurveName)) {
      return false;
    }
    if (_index == null) {
      if (other._index != null) {
        return false;
      }
    } else if (!_index.equals(other._index)) {
      return false;
    }
    if (Double.doubleToLongBits(_notionalAccrued) != Double.doubleToLongBits(other._notionalAccrued)) {
      return false;
    }
    return true;
  }

}
