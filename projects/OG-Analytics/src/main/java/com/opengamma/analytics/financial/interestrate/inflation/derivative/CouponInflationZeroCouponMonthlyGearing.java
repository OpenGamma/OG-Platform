/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.derivative;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationGearing;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing an zero-coupon inflation coupon.
 * The start index value is known when the coupon is traded/issued.
 * The index for a given month is given in the yield curve and in the time series on the first of the month.
 * The pay-off is factor*(final index / start index - 1) * notional.
 */
public class CouponInflationZeroCouponMonthlyGearing extends CouponInflation implements CouponInflationGearing {

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
   * Flag indicating if the notional is paid (true) or not (false).
   */
  private final boolean _payNotional;
  /**
   * The gearing (multiplicative) factor applied to the inflation increment rate.
   */
  private final double _factor;

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
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param indexStartValue The index value at the start of the coupon.
   * @param referenceEndTime The reference time for the index at the coupon end.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param factor The multiplicative factor.
   */
  public CouponInflationZeroCouponMonthlyGearing(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final IndexPrice priceIndex,
      final int conventionalMonthLag, final double indexStartValue, final double referenceEndTime, final boolean payNotional, final double factor) {
    super(currency, paymentTime, paymentYearFraction, notional, priceIndex);
    _conventionalMonthLag = conventionalMonthLag;
    _indexStartValue = indexStartValue;
    _referenceEndTime = referenceEndTime;
    _payNotional = payNotional;
    _factor = factor;
  }

  /**
   * Gets the index value at the start of the coupon.
   * @return The index value.
   */
  public double getIndexStartValue() {
    return _indexStartValue;
  }

  /**
   * Gets the lag in month between the index validity and the coupon dates for the standard product.
   * @return The lag.
   */
  public int getConventionalMonthLag() {
    return _conventionalMonthLag;
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

  @Override
  public CouponInflationZeroCouponMonthlyGearing withNotional(final double notional) {
    return new CouponInflationZeroCouponMonthlyGearing(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getPriceIndex(), _conventionalMonthLag, _indexStartValue,
        _referenceEndTime, _payNotional, _factor);
  }

  @Override
  public double getFactor() {
    return _factor;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponInflationZeroCouponMonthlyGearing(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponInflationZeroCouponMonthlyGearing(this);
  }

  @Override
  public String toString() {
    return "CouponInflationZeroCouponMonthlyGearing [_referenceEndTime=" + _referenceEndTime + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _conventionalMonthLag;
    long temp;
    temp = Double.doubleToLongBits(_factor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_indexStartValue);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (_payNotional ? 1231 : 1237);
    temp = Double.doubleToLongBits(_referenceEndTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    CouponInflationZeroCouponMonthlyGearing other = (CouponInflationZeroCouponMonthlyGearing) obj;
    if (_conventionalMonthLag != other._conventionalMonthLag)
      return false;
    if (Double.doubleToLongBits(_factor) != Double.doubleToLongBits(other._factor))
      return false;
    if (Double.doubleToLongBits(_indexStartValue) != Double.doubleToLongBits(other._indexStartValue))
      return false;
    if (_payNotional != other._payNotional)
      return false;
    if (Double.doubleToLongBits(_referenceEndTime) != Double.doubleToLongBits(other._referenceEndTime))
      return false;
    return true;
  }

}
