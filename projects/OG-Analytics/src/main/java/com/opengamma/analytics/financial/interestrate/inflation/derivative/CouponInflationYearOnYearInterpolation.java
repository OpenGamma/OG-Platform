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
   * The time for which the index at the coupon start is paid by the standard corresponding  zero coupon. 
   * There is usually a difference of two or three month between the reference date and the natural payment date.
   * The time can be negative (when the price index for the current and last month is not yet published).
   */
  private final double _naturalPaymentStartTime;

  /**
   * The reference time for the index at the coupon end. There is usually a difference of two or three month between the reference date and the payment date.
   * The time can be negative (when the price index for the current and last month is not yet published).
   */
  private final double[] _referenceEndTime;

  /**
   * The time for which the index at the coupon end is paid by the standard corresponding  zero coupon. 
   * There is usually a difference of two or three month between the reference date and the natural payment date.
   * the natural payment date is equal to the payment date when the lag is the conventional one.
   * The time can be negative (when the price index for the current and last month is not yet published).
   */
  private final double _naturalPaymentEndTime;

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
   * @param naturalPaymentStartTime The time for which the index at the coupon start is paid by the standard corresponding  zero coupon. 
   * @param referenceEndTime The reference time for the index at the coupon end.
   * @param naturalPaymentEndTime The time for which the index at the coupon end is paid by the standard corresponding  zero coupon. 
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param weightStart The weight on the first month index in the interpolation of the index at the coupon start.
   * @param weightEnd The weight on the first month index in the interpolation of the index at the coupon end.
   */
  public CouponInflationYearOnYearInterpolation(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final IndexPrice priceIndex,
      final double[] referenceStartTime, final double naturalPaymentStartTime, final double[] referenceEndTime, final double naturalPaymentEndTime, final boolean payNotional,
      final double weightStart, final double weightEnd) {
    super(currency, paymentTime, paymentYearFraction, notional, priceIndex);
    _referenceStartTime = referenceStartTime;
    _naturalPaymentStartTime = naturalPaymentStartTime;
    _referenceEndTime = referenceEndTime;
    _naturalPaymentEndTime = naturalPaymentEndTime;
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

  public double getNaturalPaymentStartTime() {
    return _naturalPaymentStartTime;
  }

  /**
   * Gets the reference time for the index at the coupon end.
   * @return The reference time.
   */
  public double[] getReferenceEndTime() {
    return _referenceEndTime;
  }

  public double getNaturalPaymentEndTime() {
    return _naturalPaymentEndTime;
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
    return new CouponInflationYearOnYearInterpolation(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getPriceIndex(), _referenceStartTime, _naturalPaymentStartTime,
        _referenceEndTime, _naturalPaymentEndTime, _payNotional, _weightStart, _weightEnd);
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
    long temp;
    temp = Double.doubleToLongBits(_naturalPaymentEndTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_naturalPaymentStartTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (_payNotional ? 1231 : 1237);
    result = prime * result + Arrays.hashCode(_referenceEndTime);
    result = prime * result + Arrays.hashCode(_referenceStartTime);
    temp = Double.doubleToLongBits(_weightEnd);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_weightStart);
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
    CouponInflationYearOnYearInterpolation other = (CouponInflationYearOnYearInterpolation) obj;
    if (Double.doubleToLongBits(_naturalPaymentEndTime) != Double.doubleToLongBits(other._naturalPaymentEndTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_naturalPaymentStartTime) != Double.doubleToLongBits(other._naturalPaymentStartTime)) {
      return false;
    }
    if (_payNotional != other._payNotional) {
      return false;
    }
    if (!Arrays.equals(_referenceEndTime, other._referenceEndTime)) {
      return false;
    }
    if (!Arrays.equals(_referenceStartTime, other._referenceStartTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_weightEnd) != Double.doubleToLongBits(other._weightEnd)) {
      return false;
    }
    if (Double.doubleToLongBits(_weightStart) != Double.doubleToLongBits(other._weightStart)) {
      return false;
    }
    return true;
  }

}
