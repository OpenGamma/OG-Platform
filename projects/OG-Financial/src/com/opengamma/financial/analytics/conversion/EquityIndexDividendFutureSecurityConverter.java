/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.financial.equity.future.definition.EquityIndexDividendFutureDefinition;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;

/**
 * 
 */
public class EquityIndexDividendFutureSecurityConverter extends AbstractFutureSecurityVisitor<EquityIndexDividendFutureDefinition> {

  public EquityIndexDividendFutureDefinition visitEquityIndexDividendFutureTrade(final SimpleTrade trade) {
    final EquityIndexDividendFutureSecurity security = (EquityIndexDividendFutureSecurity) trade.getSecurity();
    Double futuresPrice = trade.getPremium();
    if (futuresPrice == null) {
      futuresPrice = 0.;
    }
    // TODO Case Futures Refactor 2011.10.04 Instead of getting Premium from the trade, we might take previous close from time series in the ConventionSource.
    // Idea 1: Always pricing against yesterday's close, even on trade date.   Latter case is handled by tradePremium ~ (pricePrevClose - priceTradeTime)*unitAmount*nContracts
    // Idea 2: Set the referencePrice (futuresPrice) to 0.0. Handle economics through premium. This ensures risk and pnl are straightforward.
    // In the future, when ours or some back-office system supplies cash flows, we may construct the Definition with a non-zero ref price

    /* FIXME Case 2011-05-27 Revisit holiday conventions for input dates 
    final ConventionBundle conventions = super.getConventionSource().getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_EQFUTURE"));
    final Calendar calendar = CalendarUtil.getCalendar(super.getHolidaySource(), currency); //TODO use exchange holiday
    final BusinessDayConvention businessDayConvention = conventions.getBusinessDayConvention();
    */
    return new EquityIndexDividendFutureDefinition(security.getExpiry().getExpiry(), security.getSettlementDate(), futuresPrice, security.getCurrency(), security.getUnitAmount());
  }
  
  @Override
  public EquityIndexDividendFutureDefinition visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    // TODO Case Futures Refactor 2011.10.04 Instead of getting Premium from the trade, we might take previous close from time series in the ConventionSource.
    // Idea 1: Always pricing against yesterday's close, even on trade date.   Latter case is handled by tradePremium ~ (pricePrevClose - priceTradeTime)*unitAmount*nContracts
    // Idea 2: Set the referencePrice (futuresPrice) to 0.0. Handle economics through premium. This ensures risk and pnl are straightforward.
    // In the future, when ours or some back-office system supplies cash flows, we may construct the Definition with a non-zero ref price

    /* FIXME Case 2011-05-27 Revisit holiday conventions for input dates 
    final ConventionBundle conventions = super.getConventionSource().getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_EQFUTURE"));
    final Calendar calendar = CalendarUtil.getCalendar(super.getHolidaySource(), currency); //TODO use exchange holiday
    final BusinessDayConvention businessDayConvention = conventions.getBusinessDayConvention();
    */
    return new EquityIndexDividendFutureDefinition(security.getExpiry().getExpiry(), security.getSettlementDate(), 0, security.getCurrency(), security.getUnitAmount());
  }
}
