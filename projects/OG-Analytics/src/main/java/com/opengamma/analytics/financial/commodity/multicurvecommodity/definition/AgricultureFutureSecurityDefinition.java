/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.AgricultureFutureSecurity;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.underlying.CommodityUnderlying;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class AgricultureFutureSecurityDefinition extends CommodityFutureSecurityDefinition<AgricultureFutureSecurity> {

  /**
   * Constructor with all details.
   * @param lastTradingDate The last trading date, not null
   * @param underlying The commodity underlying, not null
   * @param unitName name of the unit of the commodity delivered, not null
   * @param unitAmount The size of a unit, not null
   * @param noticeFirstDate  The notice first date, can be null 
   * @param noticeLastDate  The notice last date, can be null 
   * @param firstDeliveryDate The first delivery date, not null for physical contract
   * @param lastDeliveryDate The last delivery date, not null for physical contract
   * @param settlementType The settlement type, CASH or PHYSICAL
   * @param settlementDate The settlement date, not null
   * @param name The name of the future, not null
   * @param calendar The holiday calendar, not null
   */
  public AgricultureFutureSecurityDefinition(final ZonedDateTime lastTradingDate, final CommodityUnderlying underlying, final String unitName, final double unitAmount,
      final ZonedDateTime noticeFirstDate,
      final ZonedDateTime noticeLastDate, final ZonedDateTime firstDeliveryDate, final ZonedDateTime lastDeliveryDate, final SettlementType settlementType, final ZonedDateTime settlementDate,
      final String name, final Calendar calendar) {
    super(lastTradingDate, underlying, unitName, unitAmount, noticeFirstDate, noticeLastDate, firstDeliveryDate, lastDeliveryDate, settlementType, settlementDate, name, calendar);
  }

  @Override
  public AgricultureFutureSecurity toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AgricultureFutureSecurity toDerivative(final ZonedDateTime date) {
    ArgumentChecker.inOrderOrEqual(date, getLastTradingDate(), "date", "expiry date");
    final double lastTradingTime = TimeCalculator.getTimeBetween(date, getLastTradingDate());
    final double settlementTime = TimeCalculator.getTimeBetween(date, this.getSettlementDate());
    double noticeFirstTime = 0.0;
    if (getNoticeFirstDate() != null) {
      noticeFirstTime = TimeCalculator.getTimeBetween(date, getNoticeFirstDate());
    }
    double noticeLastTime = 0.0;
    if (getNoticeLastDate() != null) {
      noticeLastTime = TimeCalculator.getTimeBetween(date, getNoticeLastDate());
    }
    double firstDeliveryTime = 0.0;
    double lastDeliveryTime = 0.0;
    if (getSettlementType().equals(SettlementType.PHYSICAL)) {
      firstDeliveryTime = TimeCalculator.getTimeBetween(date, getFirstDeliveryDate());
      lastDeliveryTime = TimeCalculator.getTimeBetween(date, getLastDeliveryDate());
    }
    return new AgricultureFutureSecurity(lastTradingTime, getUnderlying(), getUnitName(), getUnitAmount(), noticeFirstTime, noticeLastTime, firstDeliveryTime, lastDeliveryTime, getSettlementType(),
        settlementTime, getName(), getCalendar());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitAgricultureFutureSecurityDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitAgricultureFutureSecurityDefinition(this);
  }

}
