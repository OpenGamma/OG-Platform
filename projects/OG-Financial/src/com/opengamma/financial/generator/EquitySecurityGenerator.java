/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.exchange.ExchangeUtils;
import com.opengamma.core.region.Region;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.util.money.Currency;

/**
 * Source of random, but reasonable, equity security instances.
 */
public class EquitySecurityGenerator extends SecurityGenerator<EquitySecurity> {

  private static final String[][] NAME_MAIN = new String[][] {
      new String[] {"ALPHA", "BRAVO", "CHARLIE", "DELTA", "ECHO", "FOXTROT", "GOLF", "HOTEL", "INDIA", "JULIET", "KILO", "LIMA", "MIKE", "NOVEMBER", "OSCAR", "PAPA", "QUEBEC", "ROMEO", "SIERA",
          "TANGO", "UNIFORM", "VICTOR", "WHISKEY", "XRAY", "YANKEE", "ZULU" },
      new String[] {"RED", "ORANGE", "YELLOW", "GREEN", "BLUE", "INDIGO", "VIOLET" },
      new String[] {"ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN", "HUNDRED" }
  };

  private static final String[] NAME_SUFFIX = new String[] {"CORP", "CO", "INC", "LTD", "INTL" };

  private ExchangeSearchResult _exchangeInfo;

  protected String createName() {
    final StringBuilder sb = new StringBuilder();
    for (int i = getRandom(3); i >= 0; i--) {
      sb.append(getRandom(getRandom(NAME_MAIN))).append(' ');
    }
    sb.append(getRandom(NAME_SUFFIX));
    return sb.toString();
  }

  protected String getGicsCode(int key) {
    if (key < 0) {
      if (key == Integer.MIN_VALUE) {
        key = 0;
      } else {
        key = -key;
      }
    }
    final int first = (key % 5) + 1;
    key /= 5;
    final int second = (key % 5) + 1;
    key /= 5;
    final int third = (key % 5) + 1;
    key /= 5;
    final int fourth = (key % 5) + 1;
    return first + "0" + second + "0" + third + "0" + fourth + "0";
  }

  protected Exchange getExchange(final int key) {
    if (_exchangeInfo == null) {
      final ExchangeSearchRequest request = new ExchangeSearchRequest();
      _exchangeInfo = getExchangeMaster().search(request);
    }
    if (_exchangeInfo.getExchanges().isEmpty()) {
      return null;
    } else {
      return _exchangeInfo.getExchanges().get(key % _exchangeInfo.getExchanges().size());
    }
  }

  @Override
  public EquitySecurity createSecurity() {
    final String name = createName();
    final Exchange exchange = getExchange(name.hashCode());
    final Region region = (exchange != null) ? getRegionSource().getHighestLevelRegion(exchange.getRegionIdBundle()) : null;
    final Currency currency = (region != null) ? region.getCurrency() : getRandomCurrency();
    final String exchangeName = (exchange != null) ? exchange.getName() : "NEW YORK STOCK EXCHANGE INC.";
    final String exchangeCode = (exchange != null) ? exchange.getExternalIdBundle().getValue(ExchangeUtils.ISO_MIC) : "XYNS";
    final EquitySecurity security = new EquitySecurity(exchangeName, exchangeCode, name, currency);
    security.setName(security.getCompanyName());
    security.setShortName(name.substring(0, name.lastIndexOf(' ')));
    security.setGicsCode(GICSCode.of(getGicsCode(name.hashCode())));
    return security;
  }

}
