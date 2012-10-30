/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumTransactionDefinition;
import com.opengamma.core.position.Trade;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class InterestRateFutureOptionTradeConverter {
  private final InterestRateFutureOptionSecurityConverter _securityConverter;

  public InterestRateFutureOptionTradeConverter(final InterestRateFutureOptionSecurityConverter securityConverter) {
    ArgumentChecker.notNull(securityConverter, "security converter");
    _securityConverter = securityConverter;
  }

  public InstrumentDefinition<?> convert(final Trade trade) {
    ArgumentChecker.notNull(trade, "trade");
    ArgumentChecker.isTrue(trade.getSecurity() instanceof IRFutureOptionSecurity, "Can only handle trades with security type IRFutureOptionSecurity");
    final InstrumentDefinition<?> securityDefinition = ((IRFutureOptionSecurity) trade.getSecurity()).accept(_securityConverter);
    final int quantity = 1; // trade.getQuantity().intValue(); TODO: correct when position/trade dilemma is solved.
    //TODO trade time or premium time?
    //    final ZonedDateTime tradeDate = ZonedDateTime.of(trade.getPremiumDate().atTime(trade.getPremiumTime()),
    //        TimeZone.UTC); //TODO get the real time zone 
    final ZonedDateTime tradeDate = ZonedDateTime.of(trade.getTradeDate().atTime(trade.getTradeTime()), TimeZone.UTC); //TODO get the real time zone

    final Double tradePrice = trade.getPremium();
    ArgumentChecker.notNull(tradePrice, "IRFutureOption trade must have a premium set. The interpretation of premium is the market price, without unit, i.e. not %");
    // TODO: The premium is not the right place to store the trade price...

    if (securityDefinition instanceof InterestRateFutureOptionMarginSecurityDefinition) {
      final InterestRateFutureOptionMarginSecurityDefinition underlyingOption = (InterestRateFutureOptionMarginSecurityDefinition) securityDefinition;
      return new InterestRateFutureOptionMarginTransactionDefinition(underlyingOption, quantity, tradeDate, tradePrice);
    }
    final InterestRateFutureOptionPremiumSecurityDefinition underlyingOption = (InterestRateFutureOptionPremiumSecurityDefinition) securityDefinition;
    return new InterestRateFutureOptionPremiumTransactionDefinition(underlyingOption, quantity, tradeDate, tradePrice);
  }
}
