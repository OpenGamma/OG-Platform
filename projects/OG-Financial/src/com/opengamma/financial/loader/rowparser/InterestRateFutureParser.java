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
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * This class uses the standard OG import fields to generate an IR Future security
 */
public class InterestRateFutureParser extends RowParser {

  private static final String ID_SCHEME = "IR_FUTURE_LOADER";

  //CSOFF
  protected String EXPIRY = "expiry";
  protected String TRADING_EXCHANGE = "trading exchange";
  protected String SETTLEMENT_EXCHANGE = "settlement exchange";
  protected String CURRENCY = "currency";
  protected String UNIT_AMOUNT = "unit amount";
  protected String UNDERLYING_ID = "underlying id";
  protected String NAME = "name";
  protected String BBG_CODE = "bbg code";
  //CSON
  
  public InterestRateFutureParser(ToolContext toolContext) {
    super(toolContext);
  }

  public String[] getColumns() {
    return new String[] {EXPIRY, TRADING_EXCHANGE, SETTLEMENT_EXCHANGE, CURRENCY, UNIT_AMOUNT, UNDERLYING_ID, NAME, BBG_CODE };
  }

  @Override
  public ManageableSecurity[] constructSecurity(Map<String, String> irFutureDetails) {
    Currency ccy = Currency.of(getWithException(irFutureDetails, CURRENCY));
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(
        getWithException(irFutureDetails, EXPIRY), CSV_DATE_FORMATTER),
        LocalTime.of(16, 0)), TimeZone.UTC), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR); //TODO shouldn't be hard-coding time and zone
    String tradingExchange = getWithException(irFutureDetails, TRADING_EXCHANGE);
    String settlementExchange = getWithException(irFutureDetails, SETTLEMENT_EXCHANGE);
    double unitAmount = Double.parseDouble(getWithException(irFutureDetails, UNIT_AMOUNT));
    String bbgId = getWithException(irFutureDetails, UNDERLYING_ID);
    ExternalId underlyingID = ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, bbgId);
    InterestRateFutureSecurity irFuture = new InterestRateFutureSecurity(expiry, tradingExchange, settlementExchange, ccy, unitAmount, underlyingID);
    String identifierValue = getWithException(irFutureDetails, BBG_CODE);
    irFuture.addExternalId(ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, identifierValue));
    String name = getWithException(irFutureDetails, NAME);
    irFuture.setName(name);
    irFuture.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));

    ManageableSecurity[] result = {irFuture};
    return result;
  }

  public Map<String, String> constructRow(ManageableSecurity security) {
    Map<String, String> result = new HashMap<String, String>();
    InterestRateFutureSecurity future = (InterestRateFutureSecurity) security;
    
    result.put(EXPIRY, future.getExpiry().getExpiry().toString(CSV_DATE_FORMATTER));
    result.put(TRADING_EXCHANGE, future.getTradingExchange());
    result.put(SETTLEMENT_EXCHANGE, future.getSettlementExchange());
    result.put(CURRENCY, future.getCurrency().getCode());
    result.put(UNIT_AMOUNT, Double.toString(future.getUnitAmount()));
    result.put(UNDERLYING_ID, future.getUnderlyingId().toString());
    result.put(NAME, future.getName());
    result.put(BBG_CODE, future.getExternalIdBundle().getExternalId(SecurityUtils.BLOOMBERG_TICKER).getValue());
    
    return result;  
  }

}
