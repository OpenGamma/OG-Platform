/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.loader.rowparser;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * This class parses standard OG import fields to generate an FX Forward security
 */
public class FXForwardParser extends RowParser {

  private static final String ID_SCHEME = "FX_FORWARD_LOADER";

  //CSOFF
  protected String PAY_CURRENCY = "pay currency";
  protected String RECEIVE_CURRENCY = "receive currency";
  protected String PAY_AMOUNT = "pay amount";
  protected String RECEIVE_AMOUNT = "receive amount";
  protected String COUNTRY = "country";
  protected String FORWARD_DATE = "forward date";
  //CSON
  
  public FXForwardParser(ToolContext toolContext) {
    super(toolContext);
  }

  public String[] getColumns() {
    return new String[] {PAY_CURRENCY, RECEIVE_CURRENCY, PAY_AMOUNT, RECEIVE_AMOUNT, COUNTRY, FORWARD_DATE };
  }

  @Override
  public ManageableSecurity[] constructSecurity(Map<String, String> fxForwardDetails) {
    Currency payCurrency = Currency.of(getWithException(fxForwardDetails, PAY_CURRENCY));
    Currency receiveCurrency = Currency.of(getWithException(fxForwardDetails, RECEIVE_CURRENCY));
    double payAmount = Math.abs(Double.parseDouble(getWithException(fxForwardDetails, PAY_AMOUNT)));
    double receiveAmount = Math.abs(Double.parseDouble(getWithException(fxForwardDetails, RECEIVE_AMOUNT)));
    ExternalId region = RegionUtils.countryRegionId(Country.of(getWithException(fxForwardDetails, COUNTRY)));
    String date = getWithException(fxForwardDetails, FORWARD_DATE);
    ZonedDateTime forwardDate = ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(date, CSV_DATE_FORMATTER), 
        LocalTime.of(2, 0)), TimeZone.UTC);
    FXForwardSecurity fxForward = new FXForwardSecurity(payCurrency, payAmount, receiveCurrency, receiveAmount, forwardDate, region);
    fxForward.setName("Pay " + payCurrency.getCode() + " " + payAmount + ", receive " + receiveCurrency.getCode() + " " + receiveAmount + " on " + date);
    fxForward.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    
    ManageableSecurity[] result = {fxForward};
    return result;
  }
  
  public ManageablePosition constructPosition(Map<String, String> row, ManageableSecurity security) {
    ManageablePosition result = new ManageablePosition(BigDecimal.ONE, security.getExternalIdBundle());
    
    return result; 
  }

  public Map<String, String> constructRow(ManageableSecurity security) {
    Map<String, String> result = new HashMap<String, String>();
    FXForwardSecurity fxForward = (FXForwardSecurity) security;
    
    result.put(PAY_CURRENCY, fxForward.getPayCurrency().getCode());
    result.put(RECEIVE_CURRENCY, fxForward.getReceiveCurrency().getCode());
    result.put(PAY_AMOUNT, Double.toString(fxForward.getPayAmount()));
    result.put(RECEIVE_AMOUNT, Double.toString(fxForward.getReceiveAmount()));
    result.put(COUNTRY, RegionUtils.getRegions(getToolContext().getRegionSource(), fxForward.getRegionId()).iterator().next().getFullName());
    result.put(FORWARD_DATE, fxForward.getForwardDate().toString(CSV_DATE_FORMATTER));
    
    return result;
  }
  
}
