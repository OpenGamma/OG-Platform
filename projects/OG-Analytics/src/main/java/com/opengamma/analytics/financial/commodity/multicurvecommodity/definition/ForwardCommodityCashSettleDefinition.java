/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.underlying.CommodityUnderlying;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ForwardCommodityCashSettleDefinition extends CouponCommodityCashSettleDefinition {

  /**
   * The Forward rate.
   */
  private final double _rate;

  /**
   * Constructor with all details.
   * @param rate The Forward rate. 
   * @param paymentYearFraction payment year fraction, positive
   * @param underlying The commodity underlying, not null
   * @param unitName name of the unit of the commodity delivered, not null
   * @param notional notional
   * @param settlementDate The settlement date, not null
   * @param calendar The holiday calendar, not null
   * @param fixingDate the fixing date
   */
  public ForwardCommodityCashSettleDefinition(final double rate, final double paymentYearFraction, final CommodityUnderlying underlying, final String unitName, final double notional,
      final ZonedDateTime settlementDate, final Calendar calendar, final ZonedDateTime fixingDate) {
    super(paymentYearFraction, underlying, unitName, notional, settlementDate, calendar, fixingDate);
    _rate = rate;
  }

  /**
   * @return the Forward rate
   */
  public double getRate() {
    return _rate;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForwardCommodityCashSettleDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForwardCommodityCashSettleDefinition(this);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ForwardCommodityCashSettleDefinition [_rate=" + _rate + "]";
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
