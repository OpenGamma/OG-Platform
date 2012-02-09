/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader.rowparsers;

import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.loader.RowParser;
import com.opengamma.financial.portfolio.loader.LoaderContext;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * This class reads standard OG import fields to generate an FX Option security
 */
public class FXVanillaOptionParser extends RowParser {

  private static final String ID_SCHEME = "VANILLA_FX_OPTION_LOADER";
  
  private static final int SETTLEMENT_DAYS = 2;
  
  //CSOFF
  protected String PUT_CURRENCY = "put currency";
  protected String CALL_CURRENCY = "call currency";
  protected String PUT_AMOUNT = "put amount";
  protected String CALL_AMOUNT = "call amount";
  protected String EXPIRY = "expiry";
  protected String IS_LONG = "is long";
  //CSON
  
  public FXVanillaOptionParser(LoaderContext loaderContext) {
    super(loaderContext);
  }

  @Override
  public ManageableSecurity[] constructSecurity(Map<String, String> fxOptionDetails) {
    
    
    Currency putCurrency = Currency.of(getWithException(fxOptionDetails, PUT_CURRENCY));
    Currency callCurrency = Currency.of(getWithException(fxOptionDetails, CALL_CURRENCY));
    
    double putAmount = Double.parseDouble(getWithException(fxOptionDetails, PUT_AMOUNT));
    double callAmount = Double.parseDouble(getWithException(fxOptionDetails, CALL_AMOUNT));
    
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(
        getWithException(fxOptionDetails, EXPIRY), CSV_DATE_FORMATTER),
        LocalTime.of(16, 0)), TimeZone.UTC), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR); //TODO shouldn't be hard-coding time and zone
    ZonedDateTime settlementDate;
    if (getLoaderContext() != null) {
      Calendar calendar = CalendarUtils.getCalendar(getLoaderContext().getHolidaySource(), putCurrency, callCurrency);
      settlementDate = ScheduleCalculator.getAdjustedDate(expiry.getExpiry().toLocalDate(), SETTLEMENT_DAYS, calendar).atStartOfDayInZone(TimeZone.UTC); //expiry.getExpiry().plusDays(2);
    } else {
      settlementDate = expiry.getExpiry().plusDays(SETTLEMENT_DAYS);
    }
    boolean isLong = Boolean.parseBoolean(getWithException(fxOptionDetails, IS_LONG));
    FXOptionSecurity security = new FXOptionSecurity(putCurrency, callCurrency, putAmount, callAmount, expiry, settlementDate, isLong, new EuropeanExerciseType()); 
    String name = (isLong ? "Long " : "Short ") + "put " + putCurrency.getCode() + " " + putAmount + ", call " + callCurrency.getCode() + " " + callAmount + " on " + expiry.getExpiry().toLocalDate();
    security.setName(name);
    security.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));

    ManageableSecurity[] result = {security};
    return result;
  }

}
