/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.derivative;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
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
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   */
  public CouponInflation(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final IndexPrice priceIndex) {
    super(currency, paymentTime, paymentYearFraction, notional);
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _priceIndex.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CouponInflation other = (CouponInflation) obj;
    if (!ObjectUtils.equals(_priceIndex, other._priceIndex)) {
      return false;
    }
    return true;
  }

}
