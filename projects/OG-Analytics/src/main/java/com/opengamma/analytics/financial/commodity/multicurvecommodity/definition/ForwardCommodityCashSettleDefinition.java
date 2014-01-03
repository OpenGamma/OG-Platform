/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.underlying.CommodityUnderlying;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * 
 */
public class ForwardCommodityCashSettleDefinition extends CouponCommodityCashSettleDefinition {

  /**
   * The Forward rate.
   */
  private final double _rate;

  public ForwardCommodityCashSettleDefinition(final double rate, final double paymentYearFraction, final CommodityUnderlying underlying, final String unitName,
      final ZonedDateTime settlementDate, final Calendar calendar) {
    super(paymentYearFraction, underlying, unitName, settlementDate, calendar);
    _rate = rate;
  }

  /**
   * @return the Forward rate
   */
  public double getRate() {
    return _rate;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_rate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    final ForwardCommodityCashSettleDefinition other = (ForwardCommodityCashSettleDefinition) obj;
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    return true;
  }

}
