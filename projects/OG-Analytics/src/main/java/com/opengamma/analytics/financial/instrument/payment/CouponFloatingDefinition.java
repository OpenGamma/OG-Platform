/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a generic floating payment coupon with a unique fixing date.
 */
public abstract class CouponFloatingDefinition extends CouponDefinition implements InstrumentDefinitionWithData<Payment, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * The coupon fixing date.
   */
  private final ZonedDateTime _fixingDate;

  /**
   * Floating coupon constructor from all details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   */
  public CouponFloatingDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double accrualFactor, final double notional, final ZonedDateTime fixingDate) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional);
    ArgumentChecker.notNull(fixingDate, "fixing date");
    ArgumentChecker.isTrue(!fixingDate.isAfter(paymentDate), "fixing date {} must be strictly before payment {}", fixingDate, paymentDate);
    _fixingDate = fixingDate;
  }

  /**
   * Gets the fixing date.
   * @return The fixing date.
   */
  public ZonedDateTime getFixingDate() {
    return _fixingDate;
  }

  @Override
  public String toString() {
    final String result = super.toString() + ", Fixing date = " + _fixingDate;
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _fixingDate.hashCode();
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
    final CouponFloatingDefinition other = (CouponFloatingDefinition) obj;
    return ObjectUtils.equals(_fixingDate, other._fixingDate);
  }

}
