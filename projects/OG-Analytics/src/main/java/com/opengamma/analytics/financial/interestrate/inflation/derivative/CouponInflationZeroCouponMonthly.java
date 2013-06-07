/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.derivative;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing an zero-coupon inflation coupon.
 * The start index value is known when the coupon is traded/issued.
 * The index for a given month is given in the yield curve and in the time series on the first of the month.
 * The pay-off is (final index / start index - 1) * notional if the notional is not paid and final index / start index * notional if the notional is paid.
 */
public class CouponInflationZeroCouponMonthly extends CouponInflation {

  /**
   * The index value at the start of the coupon.
   */
  private final double _indexStartValue;
  /**
   * The reference time for the index at the coupon end. There is usually a difference of two or three month between the reference date and the payment date.
   * The time can be negative (when the price index for the current and last month is not yet published).
   */
  private final double _referenceEndTime;
  /**
   * Flag indicating if the notional is paid (true) or not (false) at the end of the period.
   */
  private final boolean _payNotional;
  /**
   * The lag in month between the index validity and the coupon dates for the standard product (the one in exchange market, this lag is in most cases 3 month).
   */
  private final int _conventionalMonthLag;

  /**
   * Inflation zero-coupon constructor.
   * @param currency The coupon currency.
   * @param paymentTime The time to payment.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param indexStartValue The index value at the start of the coupon for the standard product .
   * @param referenceEndTime The reference time for the index at the coupon end.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates.
   */
  public CouponInflationZeroCouponMonthly(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final IndexPrice priceIndex,
      final double indexStartValue, final double referenceEndTime, final boolean payNotional, final int conventionalMonthLag) {
    super(currency, paymentTime, paymentYearFraction, notional, priceIndex);
    this._indexStartValue = indexStartValue;
    this._referenceEndTime = referenceEndTime;
    _payNotional = payNotional;
    _conventionalMonthLag = conventionalMonthLag;
  }

  /**
   * Gets the index value at the start of the coupon.
   * @return The index value.
   */
  public double getIndexStartValue() {
    return _indexStartValue;
  }

  /**
   * Gets the reference time for the index at the coupon end.
   * @return The reference time.
   */
  public double getReferenceEndTime() {
    return _referenceEndTime;
  }

  /**
  * Gets the pay notional flag.
  * @return The flag.
  */
  public boolean payNotional() {
    return _payNotional;
  }

  /**
   * Gets the lag in month between the index validity and the coupon dates for the standard product .
   * @return The lag.
   */
  public int getConventionalMonthLag() {
    return _conventionalMonthLag;
  }

  @Override
  public CouponInflationZeroCouponMonthly withNotional(final double notional) {
    return new CouponInflationZeroCouponMonthly(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getPriceIndex(), _indexStartValue, _referenceEndTime, _payNotional,
        _conventionalMonthLag);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponInflationZeroCouponMonthly(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponInflationZeroCouponMonthly(this);
  }

  @Override
  public String toString() {
    return "CouponInflationZeroCouponMonthly [_referenceEndTime=" + _referenceEndTime + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_indexStartValue);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _conventionalMonthLag;
    result = prime * result + (_payNotional ? 1231 : 1237);
    temp = Double.doubleToLongBits(_referenceEndTime);
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
    CouponInflationZeroCouponMonthly other = (CouponInflationZeroCouponMonthly) obj;
    if (Double.doubleToLongBits(_indexStartValue) != Double.doubleToLongBits(other._indexStartValue)) {
      return false;
    }
    if (_conventionalMonthLag != other._conventionalMonthLag) {
      return false;
    }
    if (_payNotional != other._payNotional) {
      return false;
    }
    if (Double.doubleToLongBits(_referenceEndTime) != Double.doubleToLongBits(other._referenceEndTime)) {
      return false;
    }
    return true;
  }

}
