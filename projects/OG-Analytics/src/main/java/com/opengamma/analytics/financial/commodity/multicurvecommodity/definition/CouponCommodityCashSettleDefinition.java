/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.CouponCommodity;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.underlying.CommodityUnderlying;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * 
 */
public class CouponCommodityCashSettleDefinition extends CouponCommodityDefinition {

  /**
   * Constructor with all details.
   * @param paymentYearFraction payment year fraction, positive
   * @param underlying The commodity underlying, not null
   * @param unitName name of the unit of the commodity delivered, not null
   * @param settlementDate The settlement date, not null
   * @param calendar The holiday calendar, not null
   */
  public CouponCommodityCashSettleDefinition(final double paymentYearFraction, final CommodityUnderlying underlying, final String unitName, final ZonedDateTime settlementDate,
      final Calendar calendar) {
    super(paymentYearFraction, underlying, unitName, settlementDate, calendar);

  }

  @Override
  public CouponCommodity toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CouponCommodity toDerivative(final ZonedDateTime date) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    // TODO Auto-generated method stub
    return null;
  }

}
