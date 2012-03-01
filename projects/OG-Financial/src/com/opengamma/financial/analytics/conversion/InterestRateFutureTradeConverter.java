/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.core.position.Trade;
import com.opengamma.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;

/**
 * Convert the Trade on Interest Rate Future to the Definition version.
 */
public class InterestRateFutureTradeConverter {
  private final InterestRateFutureSecurityConverter _securityConverter;

  public InterestRateFutureTradeConverter(final InterestRateFutureSecurityConverter securityConverter) {
    Validate.notNull(securityConverter, "security converter");
    _securityConverter = securityConverter;
  }

  public InterestRateFutureDefinition convert(final Trade trade) {
    Validate.notNull(trade, "trade");
    Validate.isTrue(trade.getSecurity() instanceof InterestRateFutureSecurity, "Can only handle trades with security type InterestRateFutureSecurity");
    final InterestRateFutureDefinition securityDefinition = _securityConverter.visitInterestRateFutureSecurity((InterestRateFutureSecurity) trade.getSecurity());
    final int quantity = trade.getQuantity().intValue();
    final ZonedDateTime tradeDate = ZonedDateTime.of(trade.getTradeDate().atTime(trade.getTradeTime()), TimeZone.UTC); //TODO get the real time zone
    final double tradePrice = trade.getPremium() == null ? 0 : trade.getPremium() / 100; //TODO remove the default value and throw an exception
    return new InterestRateFutureDefinition(tradeDate, tradePrice, securityDefinition.getLastTradingDate(), securityDefinition.getIborIndex(), 0.0, securityDefinition.getNotional(),
        securityDefinition.getPaymentAccrualFactor(), quantity, securityDefinition.getName());
  }

}
