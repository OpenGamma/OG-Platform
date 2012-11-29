/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import com.opengamma.analytics.financial.equity.future.definition.EquityFutureDefinition;
import com.opengamma.core.position.Trade;
import com.opengamma.financial.security.future.EquityFutureSecurity;

/**
 * Visits a Trade of an EquityFutureSecurity (OG-Financial)
 * Converts it to a EquityFutureDefinition (OG-Analytics)
 * TODO - Not sure this should extend from what looks to be an InterestRateFutureConverter
 */
public class EquityFutureConverter {

  /**
   * Converts an EquityFutureSecurity Trade to an EquityFutureDefinition
   * @param trade The trade
   * @param futuresPrice This is typically the last margin price. On trade date, this might be the traded level
   * @return EquityFutureDefinition
   */
  public EquityFutureDefinition visitEquityFutureTrade(final Trade trade, final double futuresPrice) {
    final EquityFutureSecurity security = (EquityFutureSecurity) trade.getSecurity();
    return new EquityFutureDefinition(security.getExpiry().getExpiry(), security.getSettlementDate(), futuresPrice, security.getCurrency(), security.getUnitAmount());
  }
}
