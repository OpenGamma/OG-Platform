/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.inflation.derivatives;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.IndexPrice;
import com.opengamma.financial.interestrate.market.MarketBundle;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.util.money.Currency;

/**
 * Interface to inflation coupons.
 */
public abstract class CouponInflation extends Coupon {

  /**
   * The price index associated to the coupon.
   */
  private final IndexPrice _priceIndex;

  /**
   * Inflation coupon constructor.
   * @param currency The coupon currency.
   * @param paymentTime The time to payment.
   * @param fundingCurveName The discounting curve name.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   */
  public CouponInflation(Currency currency, double paymentTime, String fundingCurveName, double paymentYearFraction, double notional, IndexPrice priceIndex) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional);
    Validate.notNull(priceIndex, "Price index");
    _priceIndex = priceIndex;
  }

  /**
   * Gets the price index associated to the coupon.
   * @return The price index.
   */
  public IndexPrice getPriceIndex() {
    return _priceIndex;
  }

  @Override
  public String toString() {
    return super.toString() + ", price index=" + _priceIndex.toString();
  }

  /**
   * Computes the estimated price index for the coupon with a given market. 
   * The estimation return the correct price index even if it is already fixed and the relevant data is in the price curve.
   * @param market The market curve data.
   * @return The estimated index.
   */
  public abstract double estimatedIndex(MarketBundle market);

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _priceIndex.hashCode();
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
    CouponInflation other = (CouponInflation) obj;
    if (!ObjectUtils.equals(_priceIndex, other._priceIndex)) {
      return false;
    }
    return true;
  }

}
