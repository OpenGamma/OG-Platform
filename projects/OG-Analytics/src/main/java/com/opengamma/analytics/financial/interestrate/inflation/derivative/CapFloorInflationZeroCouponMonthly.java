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
 *  Class describing an zero-coupon inflation caplet/floorlet were the inflation figure are the one of the reference month and are not interpolated.
 */
public class CapFloorInflationZeroCouponMonthly extends CouponInflation implements CapFloor {

  /**
   *  The fixing time of the last known fixing.
   */
  private final double _lastKnownFixingTime;
  /**
   * The index value at the start of the coupon.
   */
  private final double _indexStartValue;
  /**
   * The reference date for the index at the coupon end. The first of the month. There is usually a difference of two or three month between the reference date and the payment date.
   */
  private final double _referenceEndTime;

  /**
   * The time for which the index at the coupon end is paid by the standard corresponding  zero coupon. 
   * There is usually a difference of two or three month between the reference date and the natural payment date.
   * the natural payment date is equal to the payment date when the lag is the conventional one.
   * The time can be negative (when the price index for the current and last month is not yet published).
   */
  private final double _naturalPaymentTime;

  /**
   *  The cap/floor maturity in years.
   */
  private final int _maturity;
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
   * @param lastKnownFixingTime The fixing time of the last known fixing.
   * @param indexStartValue The index value at the start of the coupon dates for the standard product.
   * @param referenceEndTime The reference time for the index at the coupon end.
   * @param naturalPaymentTime The time for which the index at the coupon end is paid by the standard corresponding  zero coupon. 
   * @param maturity The cap/floor maturity in years.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   */
  public CapFloorInflationZeroCouponMonthly(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final IndexPrice priceIndex,
      final double lastKnownFixingTime, final double indexStartValue, final double referenceEndTime, final double naturalPaymentTime, final int maturity, final double strike, final boolean isCap) {
    super(currency, paymentTime, paymentYearFraction, notional, priceIndex);
    _lastKnownFixingTime = lastKnownFixingTime;
    _indexStartValue = indexStartValue;
    _referenceEndTime = referenceEndTime;
    _naturalPaymentTime = naturalPaymentTime;
    _maturity = maturity;
    _strike = strike;
    _isCap = isCap;
  }

  /**
   * Create a new cap/floor with the same characteristics except the strike.
   * @param strike The new strike.
   * @return The cap/floor.
   */
  public CapFloorInflationZeroCouponMonthly withStrike(final double strike) {
    return new CapFloorInflationZeroCouponMonthly(getCurrency(), getPaymentTime(), getPaymentYearFraction(), getNotional(), getPriceIndex(),
        _lastKnownFixingTime, _indexStartValue, _referenceEndTime, _naturalPaymentTime, _maturity, strike, _isCap);
  }

  /**
   * Builder from a Ibor coupon, the strike and the cap/floor flag.
   * @param coupon An Ibor coupon.
   * @param lastKnownFixingTime The fixing time of the last known fixing.
   * @param maturity The cap/floor maturity in years.
   * @param strike The strike.
   * @param isCap The cap/floor flag.
   * @return The cap/floor.
   */
  public static CapFloorInflationZeroCouponMonthly from(final CouponInflationZeroCouponMonthly coupon, final double lastKnownFixingTime, final int maturity, final double strike, final boolean isCap) {
    return new CapFloorInflationZeroCouponMonthly(coupon.getCurrency(), coupon.getPaymentTime(), coupon.getPaymentYearFraction(), coupon.getNotional(), coupon.getPriceIndex(),
        lastKnownFixingTime, coupon.getIndexStartValue(), coupon.getReferenceEndTime(), coupon.getNaturalPaymentTime(), maturity, strike, isCap);
  }

  public double getLastKnownFixingTime() {
    return _lastKnownFixingTime;
  }

  public double getIndexStartValue() {
    return _indexStartValue;
  }

  public double getReferenceEndTime() {
    return _referenceEndTime;
  }

  public double getNaturalPaymentTime() {
    return _naturalPaymentTime;
  }

  public int getMaturity() {
    return _maturity;
  }

  @Override
  public double getStrike() {
    return _strike;
  }

  @Override
  public boolean isCap() {
    return _isCap;
  }

  @Override
  public double payOff(double fixing) {
    double omega = (_isCap) ? 1.0 : -1.0;
    return Math.max(omega * (fixing - _strike), 0);
  }

  @Override
  public Coupon withNotional(double notional) {
    return new CapFloorInflationZeroCouponMonthly(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getPriceIndex(), _lastKnownFixingTime, _indexStartValue,
        _referenceEndTime, _naturalPaymentTime, _maturity, _strike, _isCap);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCapFloorInflationZeroCouponMonthly(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCapFloorInflationZeroCouponMonthly(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_indexStartValue);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (_isCap ? 1231 : 1237);
    temp = Double.doubleToLongBits(_lastKnownFixingTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _maturity;
    temp = Double.doubleToLongBits(_naturalPaymentTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_referenceEndTime);
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
    CapFloorInflationZeroCouponMonthly other = (CapFloorInflationZeroCouponMonthly) obj;
    if (Double.doubleToLongBits(_indexStartValue) != Double.doubleToLongBits(other._indexStartValue)) {
      return false;
    }
    if (_isCap != other._isCap) {
      return false;
    }
    if (Double.doubleToLongBits(_lastKnownFixingTime) != Double.doubleToLongBits(other._lastKnownFixingTime)) {
      return false;
    }
    if (_maturity != other._maturity) {
      return false;
    }
    if (Double.doubleToLongBits(_naturalPaymentTime) != Double.doubleToLongBits(other._naturalPaymentTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_referenceEndTime) != Double.doubleToLongBits(other._referenceEndTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    return true;
  }

}
