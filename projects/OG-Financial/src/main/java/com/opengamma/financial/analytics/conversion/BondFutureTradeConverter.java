/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.core.position.Trade;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts a bond future trade to a {@link BondFutureDefinition}
 */
public class BondFutureTradeConverter {
  private final BondFutureSecurityConverter _securityConverter;

  public BondFutureTradeConverter(final BondFutureSecurityConverter securityConverter) {
    ArgumentChecker.notNull(securityConverter, "security converter");
    _securityConverter = securityConverter;
  }

  /**
   * Convert a Trade on a Bond FutureSecurity into the OG-Analytics Definition.
   * This is currently just a wrapper on the Security converter, but the Trade contains data we may wish to use for risk management.
   * @param trade A trade containing a BondFutureSecurity
   * @return BondFutureDefinition
   */
  // TODO Consider extending the function arguments to allow dynamic treatment of the reference price of the future.
  public BondFutureDefinition convert(final Trade trade) {
    ArgumentChecker.notNull(trade, "trade");
    ArgumentChecker.isTrue(trade.getSecurity() instanceof BondFutureSecurity, "Can only handle trades with security type BondFutureSecurity");
    final BondFutureDefinition underlyingFuture = (BondFutureDefinition) ((BondFutureSecurity) trade.getSecurity()).accept(_securityConverter);
    return underlyingFuture;
  }
}
