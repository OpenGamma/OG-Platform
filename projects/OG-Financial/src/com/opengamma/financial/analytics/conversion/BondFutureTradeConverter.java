/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.core.position.Trade;
import com.opengamma.financial.security.future.BondFutureSecurity;

/**
 * 
 */
public class BondFutureTradeConverter {
  private final BondFutureSecurityConverter _securityConverter;

  public BondFutureTradeConverter(final BondFutureSecurityConverter securityConverter) {
    Validate.notNull(securityConverter, "security converter");
    _securityConverter = securityConverter;
  }

  /**
   * Convert a Trade on a Bond FutureSecurity into the OG-Analytics Definition. 
   * TODO Consider extending the function arguments to allow dynamic treatment of the reference price of the future.
   * This is currently just a wrapper on the Security converter, but the Trade contains data we may wish to use for risk management. 
   * @param trade A trade containing a BondFutureSecurity
   * @return BondFutureDefinition
   */
  public BondFutureDefinition convert(final Trade trade) {
    Validate.notNull(trade, "trade");
    Validate.isTrue(trade.getSecurity() instanceof BondFutureSecurity, "Can only handle trades with security type BondFutureSecurity");
    final BondFutureDefinition underlyingFuture = (BondFutureDefinition) ((BondFutureSecurity) trade.getSecurity()).accept(_securityConverter);
    return underlyingFuture;
  }
}
