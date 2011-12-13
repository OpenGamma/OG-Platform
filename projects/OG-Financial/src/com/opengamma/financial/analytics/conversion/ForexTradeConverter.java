/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.forex.definition.ForexDefinition;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ForexTradeConverter {
  private final HolidaySource _holidaySource;
  private final RegionSource _regionSource;

  public ForexTradeConverter(final HolidaySource holidaySource, final RegionSource regionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _regionSource = regionSource;
  }

  public InstrumentDefinition<?> convert(final SimpleTrade trade) {
    Validate.notNull(trade, "trade");
    Validate.isTrue(trade.getSecurity() instanceof FXSecurity, "Can only handle trades with security type FXSecurity");
    final ZonedDateTime tradeDate = ZonedDateTime.of(trade.getTradeDate().atTime(trade.getTradeTime()), TimeZone.UTC); //TODO need the zone
    final FXSecurity security = (FXSecurity) trade.getSecurity();
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, security.getRegionId());
    final ZonedDateTime settlementDate = ScheduleCalculator.getAdjustedDate(tradeDate, 2, calendar); //TODO are FX trades always 2 settlement days?
    final Currency payCurrency = security.getPayCurrency();
    final Currency receiveCurrency = security.getReceiveCurrency();
    final double payAmount = security.getPayAmount();
    final double receiveAmount = security.getReceiveAmount();
    final double fxRate = -receiveAmount / payAmount;
    return new ForexDefinition(payCurrency, receiveCurrency, settlementDate, payAmount, fxRate);
  }

}
