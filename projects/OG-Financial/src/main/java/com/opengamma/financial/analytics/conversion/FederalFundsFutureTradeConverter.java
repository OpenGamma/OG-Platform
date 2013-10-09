/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;

/**
 * Converts Federal funds future trades into the definition form used by the analytics library.
 */
public class FederalFundsFutureTradeConverter {
  /** The security converter */
  private final FederalFundsFutureSecurityConverter _securityConverter;

  /**
   * @param holidaySource The holiday source, not null
   * @param conventionSource The convention source, not null
   * @param regionSource The region source, not null
   */
  public FederalFundsFutureTradeConverter(final HolidaySource holidaySource, final ConventionSource conventionSource, final RegionSource regionSource) {
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(regionSource, "region source");
    _securityConverter = new FederalFundsFutureSecurityConverter(holidaySource, conventionSource, regionSource);
  }

  public InstrumentDefinitionWithData<?, DoubleTimeSeries<ZonedDateTime>[]> convert(final Trade trade) { //CSIGNORE
    ArgumentChecker.notNull(trade, "trade");
    final Security security = trade.getSecurity();
    if (security instanceof FederalFundsFutureSecurity) {
      final FederalFundsFutureSecurityDefinition securityDefinition = (FederalFundsFutureSecurityDefinition) ((FederalFundsFutureSecurity) security).accept(_securityConverter);
      double tradePremium = 0.0;
      if (trade.getPremium() != null) {
        tradePremium = trade.getPremium(); // TODO: The trade price is stored in the trade premium. This has to be corrected.
      }
      ZonedDateTime tradeDate = DateUtils.getUTCDate(1900, 1, 1);
      if ((trade.getTradeDate() != null) && trade.getTradeTime() != null && (trade.getTradeTime().toLocalTime() != null)) {
        tradeDate = trade.getTradeDate().atTime(trade.getTradeTime().toLocalTime()).atZone(ZoneOffset.UTC); //TODO get the real time zone
      }
      final int quantity = trade.getQuantity().intValue();
      return new FederalFundsFutureTransactionDefinition(securityDefinition, quantity, tradeDate, tradePremium);
    }
    throw new IllegalArgumentException("Can only handle FederalFundsFutureSecurity");
  }
}
