/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts Federal funds future trades into the definition form used by the analytics library.
 */
public class FederalFundsFutureTradeConverter implements TradeConverter {
  /** The security converter */
  private final FederalFundsFutureSecurityConverter _securityConverter;

  /**
   * @param securitySource The security source, not null.
   * @param holidaySource The holiday source, not null
   * @param conventionSource The convention source, not null
   * @param regionSource The region source, not null
   */
  public FederalFundsFutureTradeConverter(final SecuritySource securitySource, final HolidaySource holidaySource, final ConventionSource conventionSource, final RegionSource regionSource) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(regionSource, "region source");
    _securityConverter = new FederalFundsFutureSecurityConverter(securitySource, holidaySource, conventionSource, regionSource);
  }

  public InstrumentDefinitionWithData<?, DoubleTimeSeries<ZonedDateTime>[]> convert(final Trade trade) { //CSIGNORE
    ArgumentChecker.notNull(trade, "trade");
    final Security security = trade.getSecurity();
    if (security instanceof FederalFundsFutureSecurity) {
      final FederalFundsFutureSecurityDefinition securityDefinition = (FederalFundsFutureSecurityDefinition) ((FederalFundsFutureSecurity) security).accept(_securityConverter);
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
      return new FederalFundsFutureTransactionDefinition(securityDefinition, quantity, tradeDateTime, tradePrice);
    }
    throw new IllegalArgumentException("Can only handle FederalFundsFutureSecurity");
  }
}
