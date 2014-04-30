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
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Interest rate future trade converter.
 */
public class InterestRateFutureTradeConverter implements TradeConverter {

  private final InterestRateFutureSecurityConverter _securityConverter;
  
  public InterestRateFutureTradeConverter(SecuritySource securitySource,
                                          HolidaySource holidaySource,
                                          ConventionSource conventionSource,
                                          RegionSource regionSource) {
    _securityConverter = new InterestRateFutureSecurityConverter(securitySource, holidaySource, conventionSource, regionSource);
  }
  
  public InstrumentDefinitionWithData<?, Double> convert(Trade trade) {
    ArgumentChecker.notNull(trade, "trade");
    final Security security = trade.getSecurity();
    if (security instanceof InterestRateFutureSecurity) {
      final InterestRateFutureSecurityDefinition securityDefinition = (InterestRateFutureSecurityDefinition) ((InterestRateFutureSecurity) security).accept(_securityConverter);
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
      return new InterestRateFutureTransactionDefinition(securityDefinition, quantity, tradeDateTime, tradePrice);
    }
    throw new IllegalArgumentException("Can only handle InterestRateFutureSecurity");
  }
}
