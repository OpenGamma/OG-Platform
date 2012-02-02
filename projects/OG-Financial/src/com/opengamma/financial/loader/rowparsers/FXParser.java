/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.loader.rowparsers;

import java.math.BigDecimal;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.OffsetTime;
import javax.time.calendar.ZoneOffset;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.loader.RowParser;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

public class FXParser extends RowParser {

  private static final String ID_SCHEME = "FX_LOADER";

  public static final String PAY_CURRENCY = "pay currency";
  public static final String RECEIVE_CURRENCY = "receive currency";
  public static final String PAY_AMOUNT = "pay amount";
  public static final String RECEIVE_AMOUNT = "receive amount";
  public static final String COUNTRY = "country";

  @Override
  public ManageableSecurity[] constructSecurity(Map<String, String> fxDetails) {
    Currency payCurrency = Currency.of(getWithException(fxDetails, PAY_CURRENCY));
    Currency receiveCurrency = Currency.of(getWithException(fxDetails, RECEIVE_CURRENCY));
    double payAmount = Double.parseDouble(getWithException(fxDetails, PAY_AMOUNT));
    double receiveAmount = Double.parseDouble(getWithException(fxDetails, RECEIVE_AMOUNT));
    ExternalId region = RegionUtils.countryRegionId(Country.of(getWithException(fxDetails, COUNTRY)));
    ExternalId externalId = ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString());
    FXSecurity fx = new FXSecurity(payCurrency, receiveCurrency, payAmount, receiveAmount, region);
    fx.setName("FX: pay" + payCurrency.getCode() + " " + payAmount + ", receive " + receiveCurrency.getCode() + " " + receiveAmount);
    fx.setExternalIdBundle(ExternalIdBundle.of(externalId));
    
    ManageableSecurity[] result = {fx};
    return result;
  }

  @Override
  public ManageableTrade constructTrade(Map<String, String> fxDetails, ManageableSecurity security, ManageablePosition fxPosition) {
    Counterparty counterparty = new SimpleCounterparty(ExternalId.of(ID_SCHEME, "Cpty"));
    //SimpleTrade trade = new SimpleTrade(fxPosition.getUniqueId(), security, BigDecimal.ONE, counterparty, LocalDate.now(), OffsetTime.of(LocalTime.of(11, 0), ZoneOffset.ofHours(0)));
    ManageableTrade fxTrade = new ManageableTrade(BigDecimal.ONE, security.getExternalIdBundle(), 
        LocalDate.now(), OffsetTime.of(LocalTime.of(11, 0), ZoneOffset.ofHours(0)), counterparty.getExternalId());
    //ManageableTrade fxTrade = new ManageableTrade(trade);
    return fxTrade;
  }

}
