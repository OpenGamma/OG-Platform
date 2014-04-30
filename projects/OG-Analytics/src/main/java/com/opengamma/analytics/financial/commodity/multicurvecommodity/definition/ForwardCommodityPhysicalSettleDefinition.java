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
public class ForwardCommodityPhysicalSettleDefinition extends CouponCommodityPhysicalSettleDefinition {

  /**
   * The Forward rate.
   */
  private final double _rate;

  /**
   * Constructor with all details.
   * @param rate The Forward rate.
   * @param paymentYearFraction The last trading date, not null
   * @param underlying The commodity underlying, not null
   * @param unitName name of the unit of the commodity delivered, not null
   * @param notional notional
   * @param settlementDate The settlement date, not null
   * @param calendar The holiday calendar, not null
   * @param noticeFirstDate  The notice first date, can be null 
   * @param noticeLastDate  The notice last date, can be null 
   * @param firstDeliveryDate The first delivery date, not null for physical contract
   * @param lastDeliveryDate The last delivery date, not null for physical contract
   */
  public ForwardCommodityPhysicalSettleDefinition(final double rate, final double paymentYearFraction, final CommodityUnderlying underlying, final String unitName, final double notional,
      final ZonedDateTime settlementDate, final Calendar calendar, final ZonedDateTime noticeFirstDate, final ZonedDateTime noticeLastDate, final ZonedDateTime firstDeliveryDate,
      final ZonedDateTime lastDeliveryDate) {
    super(paymentYearFraction, underlying, unitName, notional, settlementDate, calendar, noticeFirstDate, noticeLastDate, firstDeliveryDate, lastDeliveryDate);
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
    return visitor.visitForwardCommodityPhysicalSettleDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForwardCommodityPhysicalSettleDefinition(this);
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
    final ForwardCommodityPhysicalSettleDefinition other = (ForwardCommodityPhysicalSettleDefinition) obj;
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    return true;
  }

}
