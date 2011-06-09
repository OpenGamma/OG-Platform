/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.interestratefuture;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;

/**
 * 
 */
public class InterestRateFutureTradeConverter {
  private final InterestRateFutureSecurityConverter _securityConverter;

  public InterestRateFutureTradeConverter(final HolidaySource holidaySource,
      final ConventionBundleSource conventionSource, final RegionSource regionSource) {
    _securityConverter = new InterestRateFutureSecurityConverter(holidaySource, conventionSource, regionSource);
  }

  public InterestRateFutureTransactionDefinition convert(final TradeImpl trade) {
    Validate.notNull(trade, "trade");
    Validate.isTrue(trade.getSecurity() instanceof InterestRateFutureSecurity,
        "Can only handle trades with security type InterestRateFutureSecurity");
    final InterestRateFutureSecurityDefinition securityDefinition = _securityConverter
        .convert((InterestRateFutureSecurity) trade.getSecurity());
    final int quantity = trade.getQuantity().intValue();
    //TODO trade time or premium time?
    final ZonedDateTime tradeDate = ZonedDateTime.of(trade.getPremiumDate().atTime(trade.getPremiumTime()),
        TimeZone.UTC); //TODO need the zone of the exchange
    final double tradePrice = trade.getPremium();
    return new InterestRateFutureTransactionDefinition(securityDefinition, quantity, tradeDate, tradePrice);
  }
}
