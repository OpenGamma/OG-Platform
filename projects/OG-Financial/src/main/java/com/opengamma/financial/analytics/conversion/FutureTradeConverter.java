/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Visits a Trade containing a FutureSecurity (OG-Financial)
 * Converts it to an InstrumentDefinitionWithData (OG-Analytics)
 */
public class FutureTradeConverter {
  private static final FutureSecurityConverter SECURITY_CONVERTER = new FutureSecurityConverter();

  /**
   * Converts an EquityFutureSecurity Trade to an EquityFutureDefinition
   * @param trade The trade
   * @param futuresPrice This is typically the last margin price. On trade date, this might be the traded level
   * @return EquityFutureDefinition
   */
  public InstrumentDefinitionWithData<?, Double> convert(final Trade trade, final double futuresPrice) {
    ArgumentChecker.notNull(trade, "trade");
    final Security security = trade.getSecurity();
    if (security instanceof FutureSecurity) {
      return SECURITY_CONVERTER.visit((FutureSecurity) security, futuresPrice);
    }
    throw new IllegalArgumentException("Can only handle FutureSecurity");
  }
}
