/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a OIS-like floating coupon. The description is simplified by not creating the full set of fixing times.
 * Only the start and the end of the fixing period times are described. The description is enough to construct curves from OIS and price OIS coupons (even if some fixing already took place).
 */
public class CouponON extends Coupon implements DepositIndexCoupon<IndexON> {

  /**
   * The OIS-like index on which the coupon fixes. The index currency should be the same as the coupon currency. Not null.
   */
  private final IndexON _index;
  /**
   * The fixing period start time (in years). The fixing period does take into account the already fixed period,
   * i.e. the fixing period start time is the first date for which the coupon is not fixed yet.
   */
  private final double _fixingPeriodStartTime;
  /**
   * The fixing period end time (in years).
   */
  private final double _fixingPeriodEndTime;
  /**
   * The accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   */
  private final double _fixingPeriodAccrualFactor;
  /**
   * The notional augmented by the interest accrued over the periods already fixed.
   */
  private final double _notionalAccrued;
  /**
   * The forward curve name used in to estimate the fixing index.
   */
  private final String _forwardCurveName;

  /**
   * Constructor of a generic coupon from details.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param fundingCurveName Name of the funding curve.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param index The OIS-like index on which the coupon fixes. Not null.
   * @param fixingPeriodStartTime The fixing period start time (in years).
   * @param fixingPeriodEndTime The fixing period end time (in years).
   * @param fixingPeriodAccrualFactor The accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   * @param notionalAccrued The notional accrued by the interest periods already fixed.
   * @param forwardCurveName The name of the forward curve.
   * @deprecated Use the constructor that does not take yield curve names
   */
  @Deprecated
  public CouponON(final Currency currency, final double paymentTime, final String fundingCurveName, final double paymentYearFraction, final double notional, final IndexON index,
      final double fixingPeriodStartTime, final double fixingPeriodEndTime, final double fixingPeriodAccrualFactor, final double notionalAccrued, final String forwardCurveName) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional);
    ArgumentChecker.notNull(index, "Coupon OIS: index");
    _index = index;
    _fixingPeriodStartTime = fixingPeriodStartTime;
    _fixingPeriodEndTime = fixingPeriodEndTime;
    _fixingPeriodAccrualFactor = fixingPeriodAccrualFactor;
    _notionalAccrued = notionalAccrued;
    _forwardCurveName = forwardCurveName;
  }

  /**
   * Constructor of a generic coupon from details.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param index The OIS-like index on which the coupon fixes. Not null.
   * @param fixingPeriodStartTime The fixing period start time (in years).
   * @param fixingPeriodEndTime The fixing period end time (in years).
   * @param fixingPeriodAccrualFactor The accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   * @param notionalAccrued The notional accrued by the interest periods already fixed.
   */
  public CouponON(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final IndexON index,
      final double fixingPeriodStartTime, final double fixingPeriodEndTime, final double fixingPeriodAccrualFactor, final double notionalAccrued) {
    super(currency, paymentTime, paymentYearFraction, notional);
    ArgumentChecker.notNull(index, "Coupon OIS: index");
    _index = index;
    _fixingPeriodStartTime = fixingPeriodStartTime;
    _fixingPeriodEndTime = fixingPeriodEndTime;
    _fixingPeriodAccrualFactor = fixingPeriodAccrualFactor;
    _notionalAccrued = notionalAccrued;
    _forwardCurveName = null;
  }

  /**
   * Gets the OIS index of the instrument.
   * @return The index.
   */
  public IndexON getIndex() {
    return _index;
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
  public double getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
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
  public CouponON withNotional(final double notional) {
    return new CouponON(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), notional, _index, _fixingPeriodStartTime, _fixingPeriodEndTime, _fixingPeriodAccrualFactor,
        _notionalAccrued / getNotional() * notional, _forwardCurveName);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponOIS(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponOIS(this);
  }

  @Override
  public String toString() {
    return super.toString() + ", period = [" + _fixingPeriodStartTime + ", " + _fixingPeriodEndTime + "/" + _fixingPeriodAccrualFactor + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingPeriodEndTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingPeriodStartTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _index.hashCode();
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
    final CouponON other = (CouponON) obj;
    if (Double.doubleToLongBits(_fixingPeriodAccrualFactor) != Double.doubleToLongBits(other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodEndTime) != Double.doubleToLongBits(other._fixingPeriodEndTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodStartTime) != Double.doubleToLongBits(other._fixingPeriodStartTime)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (Double.doubleToLongBits(_notionalAccrued) != Double.doubleToLongBits(other._notionalAccrued)) {
      return false;
    }
    return true;
  }

}
