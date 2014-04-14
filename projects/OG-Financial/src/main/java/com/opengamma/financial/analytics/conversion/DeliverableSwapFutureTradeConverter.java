/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableTransactionDefinition;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts
 */
public class DeliverableSwapFutureTradeConverter implements TradeConverter {

  /**
   * Deliverable swap future security converter.
   */
  private final DeliverableSwapFutureSecurityConverter _securityConverter;
  
  public DeliverableSwapFutureTradeConverter(SecuritySource securitySource,
                                             SwapSecurityConverter swapSecurityConverter) {
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(swapSecurityConverter, "swapSecurityConverter");
    _securityConverter = new DeliverableSwapFutureSecurityConverter(securitySource, swapSecurityConverter);
  }
  
  public InstrumentDefinitionWithData<?, Double> convert(Trade trade) {
    ArgumentChecker.notNull(trade, "trade");
    final Security security = trade.getSecurity();
    if (security instanceof InterestRateFutureSecurity) {
      final SwapFuturesPriceDeliverableSecurityDefinition securityDefinition = (SwapFuturesPriceDeliverableSecurityDefinition) ((DeliverableSwapFutureSecurity) security).accept(_securityConverter);
      Double tradePrice = trade.getPremium(); // TODO: [PLAT-1958] The trade price is stored in the trade premium. 
      if (tradePrice == null) {
        throw new OpenGammaRuntimeException("Trade premium should not be null.");
      }
      final LocalDate tradeDate = trade.getTradeDate();
      if (tradeDate == null) {
        throw new OpenGammaRuntimeException("Trade date should not be null");
      }
      final OffsetTime tradeTime = trade.getTradeTime();
      if (tradeTime == null) {
        throw new OpenGammaRuntimeException("Trade time should not be null");
      }
      final ZonedDateTime tradeDateTime = tradeDate.atTime(tradeTime).atZoneSameInstant(ZoneOffset.UTC);
      final int quantity = trade.getQuantity().intValue();
      return new SwapFuturesPriceDeliverableTransactionDefinition(securityDefinition, quantity, tradeDateTime, tradePrice);
    }
    throw new IllegalArgumentException("Can only handle DeliverableSwapFutureSecurityDefinition");
  }
}
