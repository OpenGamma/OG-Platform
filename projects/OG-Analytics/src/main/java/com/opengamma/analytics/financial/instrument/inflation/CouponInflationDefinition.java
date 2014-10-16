/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.inflation;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing inflation coupon.
 */
public abstract class CouponInflationDefinition extends CouponDefinition 
  implements InstrumentDefinitionWithData<Payment, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * The price index associated to the coupon.
   */
  private final IndexPrice _indexPrice;

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
  public CouponInflationDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate, final double paymentYearFraction, final double notional, final IndexPrice priceIndex) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional);
    ArgumentChecker.notNull(priceIndex, "Price index");
    _indexPrice = priceIndex;
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

  @Override
  public abstract Coupon toDerivative(final ZonedDateTime date);
  
  @Override
  public abstract Coupon toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries);
  
  /**
   * Gets the price index associated to the coupon.
   * @return The price index.
   */
  public IndexPrice getPriceIndex() {
    return _indexPrice;
  }

  @Override
  public String toString() {
    return super.toString() + ", price index=" + _indexPrice.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _indexPrice.hashCode();
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
    final CouponInflationDefinition other = (CouponInflationDefinition) obj;
    if (!ObjectUtils.equals(_indexPrice, other._indexPrice)) {
      return false;
    }
    return true;
  }

}
