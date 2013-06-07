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
   * Inflation year on year coupon constructor.
   * @param currency The coupon currency.
   * @param paymentTime The time to payment.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param referenceStartTime The reference time for the index at the coupon start.
   * @param referenceEndTime The reference time for the index at the coupon end.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   */
  public CouponInflationYearOnYearMonthly(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final IndexPrice priceIndex,
      final double referenceStartTime, final double referenceEndTime, final boolean payNotional, final int conventionalMonthLag) {
    super(currency, paymentTime, paymentYearFraction, notional, priceIndex);
    this._referenceStartTime = referenceStartTime;
    this._referenceEndTime = referenceEndTime;
    _payNotional = payNotional;
    _conventionalMonthLag = conventionalMonthLag;
  }

  /**
   * Gets the reference time for the index at the coupon start.
   * @return The reference time.
   */
  public double getReferenceStartTime() {
    return _referenceStartTime;
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
   * Gets the lag in month between the index validity and the coupon dates for the standard product.
   * @return The lag.
   */
  public int getConventionalMonthLag() {
    return _conventionalMonthLag;
  }

  @Override
  public CouponInflationYearOnYearMonthly withNotional(final double notional) {
    return new CouponInflationYearOnYearMonthly(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getPriceIndex(), _referenceStartTime, _referenceEndTime, _payNotional,
        _conventionalMonthLag);
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
    result = prime * result + _conventionalMonthLag;
    result = prime * result + (_payNotional ? 1231 : 1237);
    long temp;
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
    if (_conventionalMonthLag != other._conventionalMonthLag) {
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
