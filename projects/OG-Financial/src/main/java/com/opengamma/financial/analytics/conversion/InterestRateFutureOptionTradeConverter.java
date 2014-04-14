/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionPremiumTransactionDefinition;
import com.opengamma.core.position.Trade;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts interest rate future option trades into the form used by the analytics library
 */
public class InterestRateFutureOptionTradeConverter implements TradeConverter {
  /** Converter for the interest rate future option security */
  private final InterestRateFutureOptionSecurityConverter _securityConverter;

  /**
   * @param securityConverter The interest rate future option security, not null
   */
  public InterestRateFutureOptionTradeConverter(final InterestRateFutureOptionSecurityConverter securityConverter) {
    ArgumentChecker.notNull(securityConverter, "security converter");
    _securityConverter = securityConverter;
  }

  /**
   * @param trade An interest rate future option trade, not null
   * @return The instrument definition
   */
  public InstrumentDefinition<?> convert(final Trade trade) {
    ArgumentChecker.notNull(trade, "trade");
    ArgumentChecker.isTrue(trade.getSecurity() instanceof IRFutureOptionSecurity, "Can only handle trades with security type IRFutureOptionSecurity");
    final InstrumentDefinition<?> securityDefinition = ((IRFutureOptionSecurity) trade.getSecurity()).accept(_securityConverter);
    final int quantity = trade.getQuantity().intValue();
    final LocalTime tradeTime = trade.getTradeTime() == null ? LocalTime.of(0, 0) : trade.getTradeTime().toLocalTime();
    final ZonedDateTime tradeDate = trade.getTradeDate().atTime(tradeTime).atZone(ZoneOffset.UTC); //TODO get the real time zone
    final Double tradePrice = trade.getPremium();
    ArgumentChecker.notNull(tradePrice, "IRFutureOption trade must have a premium set. The interpretation of premium is the market price, without unit, i.e. not %");
    if (securityDefinition instanceof InterestRateFutureOptionMarginSecurityDefinition) {
      final InterestRateFutureOptionMarginSecurityDefinition underlyingOption = (InterestRateFutureOptionMarginSecurityDefinition) securityDefinition;
      return new InterestRateFutureOptionMarginTransactionDefinition(underlyingOption, quantity, tradeDate, tradePrice);
    }
    final InterestRateFutureOptionPremiumSecurityDefinition underlyingOption = (InterestRateFutureOptionPremiumSecurityDefinition) securityDefinition;
    return new InterestRateFutureOptionPremiumTransactionDefinition(underlyingOption, quantity, tradeDate, tradePrice);
  }
}
