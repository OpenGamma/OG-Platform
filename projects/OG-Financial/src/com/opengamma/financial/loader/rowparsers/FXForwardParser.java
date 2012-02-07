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

import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.loader.RowParser;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

public class FXForwardParser extends RowParser {

  private static final String ID_SCHEME = "FX_FORWARD_LOADER";

  public String PAY_CURRENCY = "pay currency";
  public String RECEIVE_CURRENCY = "receive currency";
  public String PAY_AMOUNT = "pay amount";
  public String RECEIVE_AMOUNT = "receive amount";
  public String COUNTRY = "country";
  public String FORWARD_DATE = "forward date";

  @Override
  public ManageableSecurity[] constructSecurity(Map<String, String> fxForwardDetails) {
    Currency payCurrency = Currency.of(getWithException(fxForwardDetails, PAY_CURRENCY));
    Currency receiveCurrency = Currency.of(getWithException(fxForwardDetails, RECEIVE_CURRENCY));
    double payAmount = Double.parseDouble(getWithException(fxForwardDetails, PAY_AMOUNT));
    double receiveAmount = Double.parseDouble(getWithException(fxForwardDetails, RECEIVE_AMOUNT));
    ExternalId region = RegionUtils.countryRegionId(Country.of(getWithException(fxForwardDetails, COUNTRY)));
    String date = getWithException(fxForwardDetails, FORWARD_DATE);
    FXSecurity underlying = new FXSecurity(payCurrency, receiveCurrency, payAmount, receiveAmount, region);
    ExternalId underlyingId = ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString());
    underlying.addExternalId(underlyingId);
    ZonedDateTime forwardDate = ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(date, CSV_DATE_FORMATTER), 
        LocalTime.of(2, 0)), TimeZone.UTC);
    FXForwardSecurity fxForward = new FXForwardSecurity(underlyingId, forwardDate, region);
    fxForward.setName("Pay " + payCurrency.getCode() + " " + payAmount + ", receive " + receiveCurrency.getCode() + " " + receiveAmount + " on " + date);
    fxForward.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    
    ManageableSecurity[] result = {fxForward, underlying};
    return result;
  }

}
