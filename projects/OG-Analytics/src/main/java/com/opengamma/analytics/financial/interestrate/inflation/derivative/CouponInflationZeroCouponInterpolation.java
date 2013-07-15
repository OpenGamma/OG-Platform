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
   * The time for which the index at the coupon end is paid by the standard corresponding  zero coupon. 
   * There is usually a difference of two or three month between the reference date and the natural payment date.
   * the natural payment date is equal to the payment date when the lag is the conventional one.
   * The time can be negative (when the price index for the current and last month is not yet published).
   */
  private final double _naturalPaymentTime;

  /**
   * The weight on the first month index in the interpolation.
   */
  private final double _weight;
  /**
  * Flag indicating if the notional is paid (true) or not (false) at the end of the period.
  */
  private final boolean _payNotional;

  /**
   * Inflation zero-coupon constructor.
   * @param currency The coupon currency.
   * @param paymentTime The time to payment.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param indexStartValue The index value at the start of the coupon.
   * @param referenceEndTime The reference time for the index at the coupon end.
   * @param naturalPaymentTime The time for which the index at the coupon end is paid by the standard corresponding  zero coupon.
   * @param weight The weight on the first month index in the interpolation.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   */
  public CouponInflationZeroCouponInterpolation(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final IndexPrice priceIndex,
      final double indexStartValue, final double[] referenceEndTime, final double naturalPaymentTime, final double weight, final boolean payNotional) {
    super(currency, paymentTime, paymentYearFraction, notional, priceIndex);
    _indexStartValue = indexStartValue;
    _referenceEndTime = referenceEndTime;
    _naturalPaymentTime = naturalPaymentTime;
    _weight = weight;
    _payNotional = payNotional;
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

  public double getNaturalPaymentTime() {
    return _naturalPaymentTime;
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

  @Override
  public CouponInflationZeroCouponInterpolation withNotional(final double notional) {
    return new CouponInflationZeroCouponInterpolation(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getPriceIndex(), _indexStartValue, _referenceEndTime, _naturalPaymentTime,
        _weight, _payNotional);
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
    long temp;
    temp = Double.doubleToLongBits(_indexStartValue);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_naturalPaymentTime);
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
    if (Double.doubleToLongBits(_indexStartValue) != Double.doubleToLongBits(other._indexStartValue)) {
      return false;
    }
    if (Double.doubleToLongBits(_naturalPaymentTime) != Double.doubleToLongBits(other._naturalPaymentTime)) {
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
