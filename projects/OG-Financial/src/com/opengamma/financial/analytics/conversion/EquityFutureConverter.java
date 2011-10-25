/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.equity.future.definition.EquityFutureDefinition;
import com.opengamma.financial.security.future.EquityFutureSecurity;

/**
 * Visits a Trade of an EquityFutureSecurity (OG-Financial)
 * Converts it to a EquityFutureDefinition (OG-Analytics)  
 * TODO - Not sure this should extend from what looks to be an InterestRateFutureConverter
 */
public class EquityFutureConverter extends AbstractFutureSecurityVisitor<EquityFutureDefinition> {

  public EquityFutureConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource, final ExchangeSource exchangeSource) {
  }

  /**
   * Converts an EquityFutureSecurity Trade to an EquityFutureDefinition
   * @param trade The trade
   * @return EquityFutureDefinition
   */
  public EquityFutureDefinition visitEquityFutureTrade(final SimpleTrade trade) {

    final EquityFutureSecurity security = (EquityFutureSecurity) trade.getSecurity();

    // TODO Case 2011-5-27 Revisit use of trade._premium as a futures price (often simply be an index value). Ensure no payments are being automatically computed here.
    // What this futuresPrice represents is the last margin price, then when one computes pv, they get back the value expected if one unwinds the trade 
    final Double futuresPrice = trade.getPremium();

    // TODO Case 2011.10.04 Instead of getting Premium from the trade, we might take previous close from time series in the ConventionSource.
    // I spoke to Elaine about the idea of always pricing against yesterday's close, even on trade date. Latter case is handled by tradePremium ~ (pricePrevClose - priceTradeTime)*unitAmount*nContracts
    // New idea is to set the referencePrice (futuresPrice) to 0.0. In the future, when a ours or another trading system supplies cash flows, we may construct the Definition with a non-zero ref price

    /* FIXME Case 2011-05-27 Revisit holiday conventions for input dates 
    final ConventionBundle conventions = super.getConventionSource().getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_EQFUTURE"));
    final Calendar calendar = CalendarUtil.getCalendar(super.getHolidaySource(), currency); //TODO use exchange holiday
    final BusinessDayConvention businessDayConvention = conventions.getBusinessDayConvention();
    */

    return new EquityFutureDefinition(security.getExpiry().getExpiry(), security.getSettlementDate(), futuresPrice, security.getCurrency(), security.getUnitAmount());
  }
}
