/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.swap.ZZZSwapFixedIborDefinition;
import com.opengamma.util.money.Currency;

/**
 * Class describing a caplet/floorlet on CMS rate.
 */
public class CapFloorCMSDefinition extends CouponCMSDefinition {

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
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param underlyingSwap The underlying swap.
   * @param cmsIndex The CMS index associated to the cap/floor.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   */
  public CapFloorCMSDefinition(Currency currency, ZonedDateTime paymentDate, ZonedDateTime accrualStartDate, ZonedDateTime accrualEndDate, double accrualFactor, double notional,
      ZonedDateTime fixingDate, ZZZSwapFixedIborDefinition underlyingSwap, CMSIndex cmsIndex, double strike, boolean isCap) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, underlyingSwap, cmsIndex);
    _strike = strike;
    _isCap = isCap;
  }

  /**
   * Builder from all the cap/floor details.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param underlyingSwap The underlying swap.
   * @param cmsIndex The CMS index associated to the cap/floor.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @return The CMS cap/floor.
   */
  public CapFloorCMSDefinition from(ZonedDateTime paymentDate, ZonedDateTime accrualStartDate, ZonedDateTime accrualEndDate, double accrualFactor, double notional, ZonedDateTime fixingDate,
      ZZZSwapFixedIborDefinition underlyingSwap, CMSIndex cmsIndex, double strike, boolean isCap) {
    Validate.notNull(underlyingSwap, "underlying swap");
    return new CapFloorCMSDefinition(underlyingSwap.getCurrency(), paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, underlyingSwap, cmsIndex, strike, isCap);
  }

  /**
   * Gets the _strike field.
   * @return The strike
   */
  public double geStrike() {
    return _strike;
  }

  /**
   * Gets the _isCap field.
   * @return the isCap flag.
   */
  public boolean isCap() {
    return _isCap;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_isCap ? 1231 : 1237);
    long temp;
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
    CapFloorCMSDefinition other = (CapFloorCMSDefinition) obj;
    if (_isCap != other._isCap) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    return true;
  }

}
