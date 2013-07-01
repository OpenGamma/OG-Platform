/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.derivative;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.payment.CapFloor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CapFloorInflationYearOnYearMonthly extends CouponInflation implements CapFloor {

  /**
   *  The fixing time of the last known fixing.
   */
  private final double _lastKnownFixingTime;

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
   * The reference times for the index at the coupon end.  Two months are required for the interpolation.
   * There is usually a difference of two or three month between the reference date and the payment date.
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
   * The cap/floor strike.
   */
  private final double _strike;
  /**
   * The cap (true) / floor (false) flag.
   */
  private final boolean _isCap;

  /**
   * Constructor from all the cap/floor details.
   *  @param currency The coupon currency.
   * @param paymentTime The time to payment.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param lastKnownFixingTime  The fixing time of the last known fixing.
   * @param referenceStartTime The index value at the start of the coupon.
   * @param naturalPaymentStartTime The time for which the index at the coupon start is paid by the standard corresponding  zero coupon.
   * @param referenceEndTime The reference time for the index at the coupon end.
   * @param naturalPaymentEndTime The time for which the index at the coupon end is paid by the standard corresponding  zero coupon.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   */
  public CapFloorInflationYearOnYearMonthly(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final IndexPrice priceIndex,
      final double lastKnownFixingTime, final double referenceStartTime, final double naturalPaymentStartTime, final double referenceEndTime, final double naturalPaymentEndTime, double strike,
      boolean isCap) {
    super(currency, paymentTime, paymentYearFraction, notional, priceIndex);
    _lastKnownFixingTime = lastKnownFixingTime;
    _referenceStartTime = referenceStartTime;
    _naturalPaymentStartTime = naturalPaymentStartTime;
    _referenceEndTime = referenceEndTime;
    _naturalPaymentEndTime = naturalPaymentEndTime;
    _strike = strike;
    _isCap = isCap;
  }

  /**
   * Create a new cap/floor with the same characteristics except the strike.
   * @param strike The new strike.
   * @return The cap/floor.
   */
  public CapFloorInflationYearOnYearMonthly withStrike(final double strike) {
    return new CapFloorInflationYearOnYearMonthly(getCurrency(), getPaymentTime(), getPaymentYearFraction(), getNotional(), getPriceIndex(),
        _lastKnownFixingTime, _referenceStartTime, _naturalPaymentStartTime, _referenceEndTime, _naturalPaymentEndTime, strike, _isCap);
  }

  /**
   * Gets the fixing time of the last known fixing..
   * @return the last known fixing time.
   */
  public double getLastKnownFixingTime() {
    return _lastKnownFixingTime;
  }

  /**
   * Gets the reference time for the index at the coupon start.
   * @return The reference time for the index at the coupon start.
   */
  public double getReferenceStartTime() {
    return _referenceStartTime;
  }

  public double getNaturalPaymentStartTime() {
    return _naturalPaymentStartTime;
  }

  /**
   * Gets the reference time for the index at the coupon end.
   * @return The reference time for the index at the coupon end.
   */
  public double getReferenceEndTime() {
    return _referenceEndTime;
  }

  public double getNaturalPaymentEndTime() {
    return _naturalPaymentEndTime;
  }

  /**
   * Gets the cap/floor strike in years.
   * @return The strike.
   */
  @Override
  public double getStrike() {
    return _strike;
  }

  /**
   * Gets The cap (true) / floor (false) flag.
   * @return The flag.
   */
  @Override
  public boolean isCap() {
    return _isCap;
  }

  @Override
  public Coupon withNotional(double notional) {
    return new CapFloorInflationYearOnYearMonthly(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getPriceIndex(), _referenceStartTime,
        _lastKnownFixingTime, _naturalPaymentStartTime, _referenceEndTime, _naturalPaymentEndTime, _strike, _isCap);
  }

  @Override
  public double payOff(double fixing) {
    double omega = (_isCap) ? 1.0 : -1.0;
    return Math.max(omega * (fixing - _strike), 0);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCapFloorInflationYearOnYearMonthly(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCapFloorInflationYearOnYearMonthly(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_isCap ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_lastKnownFixingTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_naturalPaymentEndTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_naturalPaymentStartTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_referenceEndTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_referenceStartTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_strike);
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
    CapFloorInflationYearOnYearMonthly other = (CapFloorInflationYearOnYearMonthly) obj;
    if (_isCap != other._isCap) {
      return false;
    }
    if (Double.doubleToLongBits(_lastKnownFixingTime) != Double.doubleToLongBits(other._lastKnownFixingTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_naturalPaymentEndTime) != Double.doubleToLongBits(other._naturalPaymentEndTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_naturalPaymentStartTime) != Double.doubleToLongBits(other._naturalPaymentStartTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_referenceEndTime) != Double.doubleToLongBits(other._referenceEndTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_referenceStartTime) != Double.doubleToLongBits(other._referenceStartTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    return true;
  }

}
