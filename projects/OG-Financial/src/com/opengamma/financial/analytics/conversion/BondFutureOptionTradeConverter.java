/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumTransactionDefinition;
import com.opengamma.core.position.Trade;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class BondFutureOptionTradeConverter {
  private final BondFutureOptionSecurityConverter _securityConverter;

  public BondFutureOptionTradeConverter(final BondFutureOptionSecurityConverter securityConverter) {
    ArgumentChecker.notNull(securityConverter, "security converter");
    _securityConverter = securityConverter;
  }

  //TODO see comments in InterestRateFutureOptionTradeConverter
  public InstrumentDefinition<?> convert(final Trade trade) {
    ArgumentChecker.notNull(trade, "trade");
    ArgumentChecker.isTrue(trade.getSecurity() instanceof BondFutureOptionSecurity, "Can only handle trades with security type BondFutureOptionSecurity");
    final InstrumentDefinition<?> securityDefinition = ((BondFutureOptionSecurity) trade.getSecurity()).accept(_securityConverter);
    final int quantity = 1; // trade.getQuantity().intValue(); TODO: correct when position/trade dilemma is solved.
    final ZonedDateTime tradeDate = ZonedDateTime.of(trade.getTradeDate().atTime(trade.getTradeTime()), TimeZone.UTC); //TODO get the real time zone
    final Double tradePrice = trade.getPremium();
    ArgumentChecker.notNull(tradePrice, "Bond future option trade must have a premium set. The interpretation of premium is the market price, without unit, i.e. not %");
    final BondFutureOptionPremiumSecurityDefinition underlyingOption = (BondFutureOptionPremiumSecurityDefinition) securityDefinition;
    return new BondFutureOptionPremiumTransactionDefinition(underlyingOption, quantity, tradeDate, tradePrice);
  }
}
