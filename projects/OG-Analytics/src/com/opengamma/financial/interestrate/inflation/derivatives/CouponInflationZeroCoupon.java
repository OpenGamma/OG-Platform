/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.inflation.derivatives;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.PriceIndex;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.util.money.Currency;

/**
 * Class describing an zero-coupon inflation coupon. 
 * The start index value is known when the coupon is traded/issued.
 * The index for a given month is given in the yield curve and in the time series on the first of the month.
 */
public class CouponInflationZeroCoupon extends Coupon {

  /**
   * The price index associated to the coupon.
   */
  private final PriceIndex _priceIndex;
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
   * The price index curve name.
   */
  private final String _priceIndexCurveName;

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
   * @param priceIndexCurveName The price index curve name.
   */
  public CouponInflationZeroCoupon(Currency currency, double paymentTime, String fundingCurveName, double paymentYearFraction, double notional, PriceIndex priceIndex, double indexStartValue,
      double referenceEndTime, double fixingEndTime, final String priceIndexCurveName) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional);
    Validate.notNull(priceIndex, "Price index");
    Validate.notNull(priceIndexCurveName, "Price index curve name");
    this._priceIndex = priceIndex;
    this._indexStartValue = indexStartValue;
    this._referenceEndTime = referenceEndTime;
    this._fixingEndTime = fixingEndTime;
    _priceIndexCurveName = priceIndexCurveName;
  }

  /**
   * Gets the price index associated to the coupon.
   * @return The price index.
   */
  public PriceIndex getPriceIndex() {
    return _priceIndex;
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
   * Gets the price index curve name.
   * @return The price index curve name.
   */
  public String getPriceIndexCurveName() {
    return _priceIndexCurveName;
  }

  @Override
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitCouponInflationZeroCoupon(this, data);
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitCouponInflationZeroCoupon(this);
  }

  @Override
  public String toString() {
    return super.toString() + ", price index=" + _priceIndex.toString() + ", reference=" + _referenceEndTime + ", fixing=" + _fixingEndTime;
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
    result = prime * result + _priceIndex.hashCode();
    result = prime * result + _priceIndexCurveName.hashCode();
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
    CouponInflationZeroCoupon other = (CouponInflationZeroCoupon) obj;
    if (Double.doubleToLongBits(_fixingEndTime) != Double.doubleToLongBits(other._fixingEndTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_indexStartValue) != Double.doubleToLongBits(other._indexStartValue)) {
      return false;
    }
    if (!ObjectUtils.equals(_priceIndex, other._priceIndex)) {
      return false;
    }
    if (!ObjectUtils.equals(_priceIndexCurveName, other._priceIndexCurveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_referenceEndTime) != Double.doubleToLongBits(other._referenceEndTime)) {
      return false;
    }
    return true;
  }

}
