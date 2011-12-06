/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.inflation.derivatives;

import com.opengamma.financial.instrument.index.IndexPrice;
import com.opengamma.financial.instrument.inflation.CouponInflationGearing;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.market.MarketBundle;
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
   * The time on which the end index is expected to be known. The index is usually known two week after the end of the reference month. 
   * The date is only an "expected date" as the index publication could be delayed for different reasons. The date should not be enforced to strictly in pricing and instrument creation.
   */
  private final double _fixingEndTime;
  /**
   * Flag indicating if the notional is paid (true) or not (false).
   */
  private final boolean _payNotional;
  /**
   * The gearing (multiplicative) factor applied to the inflation increment rate.
   */
  private final double _factor;

  /**
   * Inflation zero-coupon constructor.
   * @param currency The coupon currency.
   * @param paymentTime The time to payment.
   * @param fundingCurveName The discounting curve name.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param indexStartValue The index value at the start of the coupon.
   * @param referenceEndTime The reference time for the index at the coupon end.
   * @param fixingEndTime The time on which the end index is expected to be known.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param factor The multiplicative factor.
   */
  public CouponInflationZeroCouponMonthlyGearing(Currency currency, double paymentTime, String fundingCurveName, double paymentYearFraction, double notional, IndexPrice priceIndex,
      double indexStartValue, double referenceEndTime, double fixingEndTime, boolean payNotional, double factor) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional, priceIndex);
    this._indexStartValue = indexStartValue;
    this._referenceEndTime = referenceEndTime;
    this._fixingEndTime = fixingEndTime;
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
   * Gets the reference time for the index at the coupon end.
   * @return The reference time.
   */
  public double getReferenceEndTime() {
    return _referenceEndTime;
  }

  /**
   * Gets the time on which the end index is expected to be known.
   * @return The time on which the end index is expected to be known.
   */
  public double getFixingEndTime() {
    return _fixingEndTime;
  }

  /**
   * Gets the pay notional flag.
   * @return The flag.
   */
  public boolean payNotional() {
    return _payNotional;
  }

  @Override
  public CouponInflationZeroCouponMonthlyGearing withNotional(double notional) {
    return new CouponInflationZeroCouponMonthlyGearing(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), notional, getPriceIndex(), _indexStartValue,
        _referenceEndTime, _fixingEndTime, _payNotional, _factor);
  }

  @Override
  public double getFactor() {
    return _factor;
  }

  @Override
  public double estimatedIndex(MarketBundle market) {
    double estimatedIndex = market.getPriceIndex(getPriceIndex(), _referenceEndTime);
    return estimatedIndex;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitCouponInflationZeroCouponMonthlyGearing(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCouponInflationZeroCouponMonthlyGearing(this);
  }

  @Override
  public String toString() {
    return super.toString() + ", reference=" + _referenceEndTime + ", fixing=" + _fixingEndTime;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingEndTime);
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
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CouponInflationZeroCouponMonthlyGearing other = (CouponInflationZeroCouponMonthlyGearing) obj;
    if (Double.doubleToLongBits(_fixingEndTime) != Double.doubleToLongBits(other._fixingEndTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_indexStartValue) != Double.doubleToLongBits(other._indexStartValue)) {
      return false;
    }
    if (_payNotional != other._payNotional) {
      return false;
    }
    if (Double.doubleToLongBits(_referenceEndTime) != Double.doubleToLongBits(other._referenceEndTime)) {
      return false;
    }
    return true;
  }

}
