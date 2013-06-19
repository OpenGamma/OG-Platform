/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.analytics.financial.interestrate.inflation.derivative;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing an Year on Year inflation coupon.
 * The index for a given month is given in the yield curve and in the time series on the first of the month.
 * The pay-off is paymentYearFraction*(final index / start index - 1) * notional if the notional is not paid and final index / start index * notional if the notional is paid.
 */

public class CouponInflationYearOnYearMonthly extends CouponInflation {

  /**
   * The reference time for the index at the coupon end. There is usually a difference of two or three month between the reference date and the payment date.
   * The time can be negative (when the price index for the current and last month is not yet published).
   */
  private final double _referenceStartTime;

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
  private final double _referenceEndTime;

  /**
   * The time for which the index at the coupon end is paid by the standard corresponding  zero coupon. 
   * There is usually a difference of two or three month between the reference date and the natural payment date.
   * the natural payment date is equal to the payment date when the lag is the conventional one.
   * The time can be negative (when the price index for the current and last month is not yet published).
   */
  private final double _naturalPaymentEndTime;

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
   */
  public CouponInflationYearOnYearMonthly(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final IndexPrice priceIndex,
      final double referenceStartTime, double naturalPaymentStartTime, final double referenceEndTime, double naturalPaymentEndTime, final boolean payNotional) {
    super(currency, paymentTime, paymentYearFraction, notional, priceIndex);
    _referenceStartTime = referenceStartTime;
    _naturalPaymentStartTime = naturalPaymentStartTime;
    _referenceEndTime = referenceEndTime;
    _naturalPaymentEndTime = naturalPaymentEndTime;
    _payNotional = payNotional;

  }

  /**
   * Gets the reference time for the index at the coupon start.
   * @return The reference time.
   */
  public double getReferenceStartTime() {
    return _referenceStartTime;
  }

  public double getNaturalPaymentStartTime() {
    return _naturalPaymentStartTime;
  }

  /**
   * Gets the reference time for the index at the coupon end.
   * @return The reference time.
   */
  public double getReferenceEndTime() {
    return _referenceEndTime;
  }

  public double getNaturalPaymentEndTime() {
    return _naturalPaymentEndTime;
  }

  /**
   * Gets the pay notional flag.
   * @return The flag.
   */
  public boolean payNotional() {
    return _payNotional;
  }

  @Override
  public CouponInflationYearOnYearMonthly withNotional(final double notional) {
    return new CouponInflationYearOnYearMonthly(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getPriceIndex(), _referenceStartTime, _naturalPaymentStartTime, _referenceEndTime,
        _naturalPaymentEndTime, _payNotional);
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponInflationYearOnYearMonthly(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponInflationYearOnYearMonthly(this);
  }

  @Override
  public String toString() {
    return "CouponInflationYearOnYearMonthly [_referenceStartTime=" + _referenceStartTime + ", _referenceEndTime=" + _referenceEndTime + "]";
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
    temp = Double.doubleToLongBits(_referenceEndTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_referenceStartTime);
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
    CouponInflationYearOnYearMonthly other = (CouponInflationYearOnYearMonthly) obj;
    if (Double.doubleToLongBits(_naturalPaymentEndTime) != Double.doubleToLongBits(other._naturalPaymentEndTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_naturalPaymentStartTime) != Double.doubleToLongBits(other._naturalPaymentStartTime)) {
      return false;
    }
    if (_payNotional != other._payNotional) {
      return false;
    }
    if (Double.doubleToLongBits(_referenceEndTime) != Double.doubleToLongBits(other._referenceEndTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_referenceStartTime) != Double.doubleToLongBits(other._referenceStartTime)) {
      return false;
    }
    return true;
  }

}
