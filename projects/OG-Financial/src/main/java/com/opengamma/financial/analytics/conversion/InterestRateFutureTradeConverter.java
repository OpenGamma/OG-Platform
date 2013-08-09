/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.apache.commons.lang.Validate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.core.position.Trade;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;

/**
 * Convert the Trade on Interest Rate Future to the Definition version.
 * @deprecated Use the generic FutureTradeConverter.
 */
@Deprecated
public class InterestRateFutureTradeConverter {
  private final InterestRateFutureSecurityConverterDeprecated _securityConverter;

  public InterestRateFutureTradeConverter(final InterestRateFutureSecurityConverterDeprecated securityConverter) {
    Validate.notNull(securityConverter, "security converter");
    _securityConverter = securityConverter;
  }

  public InterestRateFutureTransactionDefinition convert(final Trade trade) {
    Validate.notNull(trade, "trade");
    Validate.isTrue(trade.getSecurity() instanceof InterestRateFutureSecurity, "Can only handle trades with security type InterestRateFutureSecurity");
    final InterestRateFutureSecurityDefinition securityDefinition = _securityConverter.visitInterestRateFutureSecurity((InterestRateFutureSecurity) trade.getSecurity());
    // REVIEW: Setting this quantity to one so that we don't double-count the number of trades when the position scaling takes place
    final int quantity = trade.getQuantity().intValue();
    ZonedDateTime tradeDate;
    if (trade.getTradeTime() != null) {
      final ZoneId zone = trade.getTradeTime().getOffset();
      tradeDate = trade.getTradeDate().atTime(trade.getTradeTime().toLocalTime()).atZone(zone);
    } else {
      tradeDate = trade.getTradeDate().atTime(LocalTime.NOON).atZone(ZoneOffset.UTC);
    }
    final double tradePrice = trade.getPremium() == null ? 0 : trade.getPremium(); //TODO remove the default value and throw an exception
    return new InterestRateFutureTransactionDefinition(securityDefinition, tradeDate, tradePrice, quantity);
    //tradeDate, tradePrice, securityDefinition.getLastTradingDate(), securityDefinition.getIborIndex(),
    //securityDefinition.getNotional(), securityDefinition.getPaymentAccrualFactor(), quantity, securityDefinition.getName());
  }

}
