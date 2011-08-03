/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.bond;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.financial.instrument.future.BondFutureSecurityDefinition;
import com.opengamma.financial.instrument.future.BondFutureTransactionDefinition;
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

  public BondFutureTransactionDefinition convert(final TradeImpl trade) {
    Validate.notNull(trade, "trade");
    Validate.isTrue(trade.getSecurity() instanceof BondFutureSecurity, "Can only handle trades with security type BondFutureSecurity");
    final BondFutureSecurityDefinition underlyingFuture = (BondFutureSecurityDefinition) ((BondFutureSecurity) trade.getSecurity()).accept(_securityConverter);
    final int quantity = trade.getQuantity().intValue();
    final ZonedDateTime tradeDate = ZonedDateTime.of(trade.getPremiumDate().atTime(trade.getPremiumTime()),
        TimeZone.UTC); //TODO need the zone of the exchange
    final double tradePrice = trade.getPremium();
    return new BondFutureTransactionDefinition(underlyingFuture, quantity, tradeDate, tradePrice);
  }
}
