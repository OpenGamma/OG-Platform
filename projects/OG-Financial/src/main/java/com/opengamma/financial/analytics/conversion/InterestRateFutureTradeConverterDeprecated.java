/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.apache.commons.lang.Validate;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.core.position.Trade;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;

/**
 * Convert the Trade on Interest Rate Future to the Definition version.
 * @deprecated Use the generic FutureTradeConverter.
 */
@Deprecated
public class InterestRateFutureTradeConverterDeprecated {
  
  private final InterestRateFutureSecurityConverterDeprecated _securityConverter;

  public InterestRateFutureTradeConverterDeprecated(final InterestRateFutureSecurityConverterDeprecated securityConverter) {
    Validate.notNull(securityConverter, "security converter");
    _securityConverter = securityConverter;
  }

  public InterestRateFutureTransactionDefinition convert(final Trade trade) {
    Validate.notNull(trade, "trade");
    Validate.isTrue(trade.getSecurity() instanceof InterestRateFutureSecurity, "Can only handle trades with security type InterestRateFutureSecurity");
    final InterestRateFutureSecurityDefinition securityDefinition = _securityConverter.visitInterestRateFutureSecurity((InterestRateFutureSecurity) trade.getSecurity());
    final int quantity = trade.getQuantity().intValue();
    final LocalDate tradeDate = trade.getTradeDate();
    if (tradeDate == null) {
      throw new OpenGammaRuntimeException("Trade date should not be null");
    }
    final OffsetTime tradeTime = trade.getTradeTime();
    if (tradeTime == null) {
      throw new OpenGammaRuntimeException("Trade time should not be null");
    }
    final ZonedDateTime tradeDateTime = tradeDate.atTime(tradeTime).atZoneSameInstant(ZoneOffset.UTC);
    Double tradePrice = trade.getPremium(); // TODO: [PLAT-1958] The trade price is stored in the trade premium.
    if (tradePrice == null) {
      throw new OpenGammaRuntimeException("Trade premium should not be null.");
    }
    return new InterestRateFutureTransactionDefinition(securityDefinition, quantity, tradeDateTime, tradePrice);
  }

}
