/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.derivative;

import java.util.Arrays;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing an zero-coupon inflation coupon.
 * The start index value is known when the coupon is traded/issued.
 * The index for a given month is given in the yield curve and in the time series on the first of the month.
 */
public class CouponInflationZeroCouponInterpolation extends CouponInflation {

  /**
   * The index value at the start of the coupon.
   */
  private final double _indexStartValue;
  /**
   * The reference times for the index at the coupon end.  Two months are required for the interpolation.
   * There is usually a difference of two or three month between the reference date and the payment date.
   * The time can be negative (when the price index for the current and last month is not yet published).
   */
  private final double[] _referenceEndTime;
  /**
   * The weight on the first month index in the interpolation.
   */
  private final double _weight;
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
   * @param indexStartValue The index value at the start of the coupon.
   * @param referenceEndTime The reference time for the index at the coupon end.
   * @param weight The weight on the first month index in the interpolation.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates.
   */
  public CouponInflationZeroCouponInterpolation(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final IndexPrice priceIndex,
      final double indexStartValue, final double[] referenceEndTime, final double weight, final boolean payNotional, final int conventionalMonthLag) {
    super(currency, paymentTime, paymentYearFraction, notional, priceIndex);
    this._indexStartValue = indexStartValue;
    this._referenceEndTime = referenceEndTime;
    _weight = weight;
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
  public double[] getReferenceEndTime() {
    return _referenceEndTime;
  }

  /**
   * Gets the weight on the first month index in the interpolation.
   * @return The weight.
   */
  public double getWeight() {
    return _weight;
  }

  /**
   * Gets the pay notional flag.
   * @return The flag.
   */
  public boolean payNotional() {
    return _payNotional;
  }

  /**
   * Gets the lag in month between the index validity and the coupon dates.
   * @return The lag.
   */
  public int getConventionalMonthLag() {
    return _conventionalMonthLag;
  }

  @Override
  public CouponInflationZeroCouponInterpolation withNotional(final double notional) {
    return new CouponInflationZeroCouponInterpolation(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getPriceIndex(), _indexStartValue, _referenceEndTime, _weight,
        _payNotional, _conventionalMonthLag);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponInflationZeroCouponInterpolation(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponInflationZeroCouponInterpolation(this);
  }

  @Override
  public String toString() {
    return super.toString() + ", reference=[" + _referenceEndTime[0] + ", " + _referenceEndTime[1] + ", fixing=";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _conventionalMonthLag;
    long temp;
    temp = Double.doubleToLongBits(_indexStartValue);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (_payNotional ? 1231 : 1237);
    result = prime * result + Arrays.hashCode(_referenceEndTime);
    temp = Double.doubleToLongBits(_weight);
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
    CouponInflationZeroCouponInterpolation other = (CouponInflationZeroCouponInterpolation) obj;
    if (_conventionalMonthLag != other._conventionalMonthLag) {
      return false;
    }
    if (Double.doubleToLongBits(_indexStartValue) != Double.doubleToLongBits(other._indexStartValue)) {
      return false;
    }
    if (_payNotional != other._payNotional) {
      return false;
    }
    if (!Arrays.equals(_referenceEndTime, other._referenceEndTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_weight) != Double.doubleToLongBits(other._weight)) {
      return false;
    }
    return true;
  }

}
