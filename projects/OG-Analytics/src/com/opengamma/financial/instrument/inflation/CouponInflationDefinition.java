/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.inflation;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.IndexPrice;
import com.opengamma.financial.instrument.payment.CouponDefinition;
import com.opengamma.util.money.Currency;

/**
 * Class describing inflation coupon.
 */
public abstract class CouponInflationDefinition extends CouponDefinition {

  /**
   * The price index associated to the coupon.
   */
  private final IndexPrice _priceIndex;

  /**
   * Constructor from the coupon details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param priceIndex The price index.
   */
  public CouponInflationDefinition(Currency currency, ZonedDateTime paymentDate, ZonedDateTime accrualStartDate, ZonedDateTime accrualEndDate, double paymentYearFraction, double notional,
      IndexPrice priceIndex) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional);
    Validate.notNull(priceIndex, "Price index");
    this._priceIndex = priceIndex;
  }

  /**
   * Creates a new inflation coupon similar to the original one except that new payment, accrual dates and notional are given.
   * @param paymentDate The payment date.
   * @param accrualStartDate The accrual start date.
   * @param accrualEndDate The accrual end date.
   * @param notional The notional.
   * @return The coupon.
   */
  public abstract CouponInflationDefinition with(ZonedDateTime paymentDate, ZonedDateTime accrualStartDate, ZonedDateTime accrualEndDate, double notional);

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
    CouponInflationDefinition other = (CouponInflationDefinition) obj;
    if (!ObjectUtils.equals(_priceIndex, other._priceIndex)) {
      return false;
    }
    return true;
  }

}
