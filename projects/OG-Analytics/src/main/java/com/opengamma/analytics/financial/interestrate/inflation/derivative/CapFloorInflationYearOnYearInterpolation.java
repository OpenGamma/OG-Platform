/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.derivative;

import java.util.Arrays;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.payment.CapFloor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CapFloorInflationYearOnYearInterpolation extends CouponInflation implements CapFloor {

  /**
   *  The fixing time of the last known fixing.
   */
  private final double _lastKnownFixingTime;

  /**
   * The reference time for the index at the coupon end. There is usually a difference of two or three month between the reference date and the payment date.
   * The time can be negative (when the price index for the current and last month is not yet published).
   */
  private final double[] _referenceStartTime;
  /**
   * The reference times for the index at the coupon end.  Two months are required for the interpolation.
   * There is usually a difference of two or three month between the reference date and the payment date.
   * The time can be negative (when the price index for the current and last month is not yet published).
   */
  private final double[] _referenceEndTime;
  /**
   * The lag in month between the index validity and the coupon dates for the standard product (the one in exchange market, this lag is in most cases 3 month).
   */
  private final int _conventionalMonthLag;
  /**
   * The weight on the first month index in the interpolation of the index at the coupon start.
   */
  private final double _weightStart;

  /**
   * The weight on the first month index in the interpolation of the index at the coupon end.
   */
  private final double _weightEnd;
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
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param referenceStartTime The index value at the start of the coupon.
   * @param referenceEndTime The reference time for the index at the coupon end.
   * @param weightStart The weight on the first month index in the interpolation of the index at the coupon start.
   * @param weightEnd The weight on the first month index in the interpolation of the index at the coupon end.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   */
  public CapFloorInflationYearOnYearInterpolation(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final IndexPrice priceIndex,
      final double lastKnownFixingTime, final int conventionalMonthLag, final double[] referenceStartTime, final double[] referenceEndTime, final double weightStart, final double weightEnd,
      double strike, boolean isCap) {
    super(currency, paymentTime, paymentYearFraction, notional, priceIndex);
    _lastKnownFixingTime = lastKnownFixingTime;
    _conventionalMonthLag = conventionalMonthLag;
    _referenceStartTime = referenceStartTime;
    _referenceEndTime = referenceEndTime;
    _weightStart = weightStart;
    _weightEnd = weightEnd;
    _strike = strike;
    _isCap = isCap;
  }

  /**
   * Create a new cap/floor with the same characteristics except the strike.
   * @param strike The new strike.
   * @return The cap/floor.
   */
  public CapFloorInflationYearOnYearInterpolation withStrike(final double strike) {
    return new CapFloorInflationYearOnYearInterpolation(getCurrency(), getPaymentTime(), getPaymentYearFraction(), getNotional(), getPriceIndex(),
        _lastKnownFixingTime, _conventionalMonthLag, _referenceStartTime, _referenceEndTime, _weightStart, _weightEnd, strike, _isCap);
  }

  /**
   * Gets the fixing time of the last known fixing..
   * @return the last known fixing time.
   */
  public double getLastKnownFixingTime() {
    return _lastKnownFixingTime;
  }

  /**
   * Gets the lag in month between the index validity and the coupon dates for the standard product.
   * @return The lag.
   */
  public int getConventionalMonthLag() {
    return _conventionalMonthLag;
  }

  public double[] getReferenceStartTime() {
    return _referenceStartTime;
  }

  public double[] getReferenceEndTime() {
    return _referenceEndTime;
  }

  public double getWeightStart() {
    return _weightStart;
  }

  public double getWeightEnd() {
    return _weightEnd;
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
  public Coupon withNotional(double notional) {
    return new CapFloorInflationYearOnYearInterpolation(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getPriceIndex(), _lastKnownFixingTime,
        _conventionalMonthLag, _referenceStartTime, _referenceEndTime, _weightStart, _weightEnd, _strike, _isCap);
  }

  @Override
  public double payOff(double fixing) {
    double omega = (_isCap) ? 1.0 : -1.0;
    return Math.max(omega * (fixing - _strike), 0);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCapFloorInflationYearOnYearInterpolation(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCapFloorInflationYearOnYearInterpolation(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _conventionalMonthLag;
    result = prime * result + (_isCap ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_lastKnownFixingTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_referenceEndTime);
    result = prime * result + Arrays.hashCode(_referenceStartTime);
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    CapFloorInflationYearOnYearInterpolation other = (CapFloorInflationYearOnYearInterpolation) obj;
    if (_conventionalMonthLag != other._conventionalMonthLag) {
      return false;
    }
    if (_isCap != other._isCap) {
      return false;
    }
    if (Double.doubleToLongBits(_lastKnownFixingTime) != Double.doubleToLongBits(other._lastKnownFixingTime)) {
      return false;
    }
    if (!Arrays.equals(_referenceEndTime, other._referenceEndTime)) {
      return false;
    }
    if (!Arrays.equals(_referenceStartTime, other._referenceStartTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
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
