/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

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
    ArgumentChecker.notNull(trade.getPremium(), "Bond future option trade must have a premium set. The interpretation of premium is the market price, without unit, i.e. not %");
    //TODO fix the next two lines - it's here to avoid double-multiplying when stuff is scaled at the position level
    final int quantity = 1;
    final double premium = -trade.getPremium() * Math.signum(trade.getQuantity().doubleValue());
    final ZonedDateTime tradeDate = trade.getTradeDate().atTime(trade.getTradeTime()).atZoneSameInstant(ZoneOffset.UTC); //TODO get the real time zone
    final BondFutureOptionPremiumSecurityDefinition underlyingOption = (BondFutureOptionPremiumSecurityDefinition) securityDefinition;
    return new BondFutureOptionPremiumTransactionDefinition(underlyingOption, quantity, tradeDate, premium);
  }
}
