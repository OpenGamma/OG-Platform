/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.equity;

import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.financial.equity.future.definition.EquityFutureDefinition;
import com.opengamma.financial.security.future.EquityFutureSecurity;

/**
 * 
 */
public class EquityFutureConverter {

  public EquityFutureDefinition visitEquityFutureSecurity(TradeImpl trade) {

    final EquityFutureSecurity security = (EquityFutureSecurity) trade.getSecurity();

    final double strikePrice = 95;
    final double pointValue = 25;
    final int numContracts = 1;
    final String assetName = "FOO";

    return new EquityFutureDefinition(
          security.getExpiry().getExpiry(),
          security.getSettlementDate().toZonedDateTime(),
          strikePrice, pointValue, numContracts, assetName);
  }
}
