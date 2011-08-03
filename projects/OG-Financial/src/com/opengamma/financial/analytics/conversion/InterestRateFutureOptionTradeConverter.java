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
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionPremiumSecurityDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionPremiumTransactionDefinition;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;

/**
 * 
 */
public class InterestRateFutureOptionTradeConverter {
  private final InterestRateFutureOptionSecurityConverter _securityConverter;

  public InterestRateFutureOptionTradeConverter(final HolidaySource holidaySource,
      final ConventionBundleSource conventionSource, final RegionSource regionSource,
      final SecuritySource securitySource) {
    _securityConverter = new InterestRateFutureOptionSecurityConverter(holidaySource, conventionSource, regionSource, securitySource);
  }

  public Object convert(final TradeImpl trade) {
    Validate.notNull(trade, "trade");
    Validate.isTrue(trade.getSecurity() instanceof IRFutureOptionSecurity,
        "Can only handle trades with security type IRFutureOptionSecurity");
    final Object securityDefinition = _securityConverter.convert((IRFutureOptionSecurity) trade.getSecurity());
    final int quantity = trade.getQuantity().intValue();
    //TODO trade time or premium time?
    //    final ZonedDateTime tradeDate = ZonedDateTime.of(trade.getPremiumDate().atTime(trade.getPremiumTime()),
    //        TimeZone.UTC); //TODO get the real time zone
    final ZonedDateTime tradeDate = ZonedDateTime.of(trade.getTradeDate().atTime(trade.getTradeTime()),
        TimeZone.UTC); //TODO get the real time zone
    final double tradePrice = trade.getPremium() == null ? 0.5 : trade.getPremium(); //TODO remove the default value and throw an exception
    if (securityDefinition instanceof InterestRateFutureOptionMarginSecurityDefinition) {
      final InterestRateFutureOptionMarginSecurityDefinition underlyingOption = (InterestRateFutureOptionMarginSecurityDefinition) securityDefinition;
      return new InterestRateFutureOptionMarginTransactionDefinition(underlyingOption, quantity, tradeDate, tradePrice);
    }
    final InterestRateFutureOptionPremiumSecurityDefinition underlyingOption = (InterestRateFutureOptionPremiumSecurityDefinition) securityDefinition;
    return new InterestRateFutureOptionPremiumTransactionDefinition(underlyingOption, quantity, tradeDate, tradePrice);
  }
}
