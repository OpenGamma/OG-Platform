/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * Class describing an Year on Year inflation coupon.
 * The index for a given month is given in the yield curve and in the time series on the first of the month.
 * The pay-off is paymentYearFraction*(final index / start index - 1) * notional if the notional is not paid and final index / start index * notional if the notional is paid.
 */

public class CouponInflationYearOnYearInterpolation extends CouponInflation {

  /**
   * The reference time for the index at the coupon end. There is usually a difference of two or three month between the reference date and the payment date.
   * The time can be negative (when the price index for the current and last month is not yet published).
   */
  private final double[] _referenceStartTime;

  /**
   * The reference time for the index at the coupon end. There is usually a difference of two or three month between the reference date and the payment date.
   * The time can be negative (when the price index for the current and last month is not yet published).
   */
  private final double[] _referenceEndTime;

  /**
   * The weight on the first month index in the interpolation of the index at the coupon start.
   */
  private final double _weightStart;

  /**
   * The weight on the first month index in the interpolation of the index at the coupon end.
   */
  private final double _weightEnd;

  /**
   * Flag indicating if the notional is paid (true) or not (false) at the end of the period.
   */
  private final boolean _payNotional;

  /**
   * Inflation year on year coupon constructor.
   * @param currency The coupon currency.
   * @param paymentTime The time to payment.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param referenceStartTime The reference time for the index at the coupon start.
   * @param referenceEndTime The reference time for the index at the coupon end.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param weightStart The weight on the first month index in the interpolation of the index at the coupon start.
   * @param weightEnd The weight on the first month index in the interpolation of the index at the coupon end.
   */
  public CouponInflationYearOnYearInterpolation(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final IndexPrice priceIndex,
      final double[] referenceStartTime, final double[] referenceEndTime,
      final boolean payNotional, final double weightStart, final double weightEnd) {
    super(currency, paymentTime, paymentYearFraction, notional, priceIndex);
    this._referenceStartTime = referenceStartTime;
    this._referenceEndTime = referenceEndTime;
    _weightStart = weightStart;
    _weightEnd = weightEnd;
    _payNotional = payNotional;

  }

  /**
   * Gets the reference time for the index at the coupon start.
   * @return The reference time.
   */
  public double[] getReferenceStartTime() {
    return _referenceStartTime;
  }

  /**
   * Gets the reference time for the index at the coupon end.
   * @return The reference time.
   */
  public double[] getReferenceEndTime() {
    return _referenceEndTime;
  }

  /**
   * Gets the weight on the first month index in the interpolation of the index at the coupon start.
   * @return The weight.
   */
  public double getWeightStart() {
    return _weightStart;
  }

  /**
   * Gets the weight on the first month index in the interpolation of the index at the coupon end.
   * @return The weight.
   */
  public double getWeightEnd() {
    return _weightEnd;
  }

  /**
   * Gets the pay notional flag.
   * @return The flag.
   */
  public boolean payNotional() {
    return _payNotional;
  }

  @Override
  public CouponInflationYearOnYearInterpolation withNotional(final double notional) {
    return new CouponInflationYearOnYearInterpolation(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getPriceIndex(), _referenceStartTime, _referenceEndTime,
        _payNotional,
        _weightStart, _weightEnd);
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponInflationYearOnYearInterpolation(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponInflationYearOnYearInterpolation(this);
  }

  @Override
  public String toString() {
    return "CouponInflationYearOnYearInterpolation [_referenceStartTime=" + Arrays.toString(_referenceStartTime) + ", _referenceEndTime=" + Arrays.toString(_referenceEndTime) + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_referenceEndTime);
    result = prime * result + Arrays.hashCode(_referenceStartTime);
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
    CouponInflationYearOnYearInterpolation other = (CouponInflationYearOnYearInterpolation) obj;
    if (!Arrays.equals(_referenceEndTime, other._referenceEndTime)) {
      return false;
    }
    if (!Arrays.equals(_referenceStartTime, other._referenceStartTime)) {
      return false;
    }
    return true;
  }

}
