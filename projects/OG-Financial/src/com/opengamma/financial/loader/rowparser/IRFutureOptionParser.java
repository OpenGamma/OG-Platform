/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.loader.rowparser;

import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * This class parses standard OG import fields to generate an IR Future Option security
 */
public class IRFutureOptionParser extends RowParser {

  private static final String ID_SCHEME = "IR_FUTURE_OPTION_LOADER";

  //CSOFF
  protected String EXCHANGE = "exchange";
  protected String EXPIRY = "expiry";
  protected String UNDERLYING_ID = "underlying identifier";
  protected String POINT_VALUE = "point value";
  // private static final String IS_MARGINED = "margined";
  protected String CURRENCY = "currency";
  protected String STRIKE = "strike";
  protected String IS_CALL = "call";
  //CSON
  
  public IRFutureOptionParser(ToolContext toolContext) {
    super(toolContext);
  }
 
  public String[] getColumns() {
    return new String[] {EXCHANGE, EXPIRY, UNDERLYING_ID, POINT_VALUE, CURRENCY, STRIKE, IS_CALL };
  }

  @Override
  public ManageableSecurity[] constructSecurity(Map<String, String> irFutureOptionDetails) {
    final Currency currency = Currency.of(getWithException(irFutureOptionDetails, CURRENCY));
    final Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(
        getWithException(irFutureOptionDetails, EXPIRY), CSV_DATE_FORMATTER),
        LocalTime.of(16, 0)), TimeZone.UTC), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR); //TODO shouldn't be hard-coding time and zone
    final String exchange = getWithException(irFutureOptionDetails, EXCHANGE);
    final ExerciseType exerciseType = new AmericanExerciseType();
    final String bbgID = getWithException(irFutureOptionDetails, UNDERLYING_ID);
    final ExternalId underlyingID = ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, bbgID);
    final double pointValue = Double.parseDouble(getWithException(irFutureOptionDetails, POINT_VALUE));
    final boolean isMargined = true; //Boolean.parseBoolean(PortfolioLoaderHelper.getWithException(irFutureOptionDetails, IS_MARGINED));
    final double strike = Double.parseDouble(getWithException(irFutureOptionDetails, STRIKE));
    final boolean isCall = Boolean.parseBoolean(getWithException(irFutureOptionDetails, IS_CALL));
    final OptionType optionType = isCall ? OptionType.CALL : OptionType.PUT;
    final IRFutureOptionSecurity security = new IRFutureOptionSecurity(exchange, expiry, exerciseType, underlyingID, pointValue, isMargined, currency, strike, optionType);
    security.setName("American " + (isMargined ? "margined " : "") + (isCall ? "call " : "put ") + "on " + bbgID + ", strike = " + strike + ", expiring " + expiry.getExpiry().toLocalDate());
    security.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));

    ManageableSecurity[] result = {security};
    return result;
  }

  public Map<String, String> constructRow(ManageableSecurity security) {
    Map<String, String> result = new HashMap<String, String>();
    IRFutureOptionSecurity futureOption = (IRFutureOptionSecurity) security;
    
    result.put(EXCHANGE, futureOption.getExchange());
    result.put(EXPIRY, futureOption.getExpiry().getExpiry().toString(CSV_DATE_FORMATTER));
    result.put(UNDERLYING_ID, futureOption.getUnderlyingId().toString());
    result.put(POINT_VALUE, Double.toString(futureOption.getPointValue()));
    result.put(CURRENCY, futureOption.getCurrency().getCode());
    result.put(STRIKE, Double.toString(futureOption.getStrike()));
    result.put(IS_CALL, Boolean.toString(futureOption.getOptionType() == OptionType.CALL));
    
    return result;  
  }

}
