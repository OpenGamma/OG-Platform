/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.loadsave.portfolio.rowparser;

import java.math.BigDecimal;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;
import javax.time.calendar.ZoneOffset;


import com.opengamma.bbg.BloombergSecuritySource;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * A row parser that reads in a ticker for an exchange-traded security, a quantity for a position, and
 * optionally a trade date, premium and counterparty for a trade.
 */
public class ExchangeTradedRowParser extends RowParser {

  private static final String TICKER = "ticker";
  private static final String QUANTITY = "quantity";
  private static final String TRADE_DATE = "trade date";
  private static final String PREMIUM = "premium";
  private static final String COUNTERPARTY = "counterparty";
  
  private String[] _columns = {TICKER, QUANTITY, TRADE_DATE, PREMIUM, COUNTERPARTY };
  
  private BloombergSecuritySource _bbgSecSource;

  public ExchangeTradedRowParser(BloombergSecuritySource bbgSecSource) {
    ArgumentChecker.notNull(bbgSecSource, "bbgSecSource");
    _bbgSecSource = bbgSecSource;
  }

  @Override
  public ManageableSecurity[] constructSecurity(Map<String, String> row) {
    
    ArgumentChecker.notNull(row, "row");
    
    // Look up security using ticker
    ManageableSecurity security = _bbgSecSource.getSecurity(ExternalIdBundle.of(
        SecurityUtils.bloombergTickerSecurityId(getWithException(row, TICKER))));
    if (security != null) {
      return new ManageableSecurity[] {security };
    } else {
      return new ManageableSecurity[] {};
    }
  }

  @Override
  public ManageablePosition constructPosition(Map<String, String> row, ManageableSecurity security) {
    // Create position using the quantity field
    
    ArgumentChecker.notNull(row, "row");
    ArgumentChecker.notNull(security, "security");
    
    if (row.containsKey(QUANTITY)) {      
      return new ManageablePosition(
          BigDecimal.valueOf(Integer.parseInt(getWithException(row, QUANTITY))), 
          security.getExternalIdBundle()
      );
    } else { 
      return new ManageablePosition(
          BigDecimal.ONE, 
          security.getExternalIdBundle()
      );
    }
  }
  
  @Override
  public ManageableTrade constructTrade(Map<String, String> row, ManageableSecurity security, ManageablePosition position) {
    
    ArgumentChecker.notNull(row, "row");
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(position, "position");
    
    // Create trade using trade date, premium and counterparty if available in current row
    if (row.containsKey(TRADE_DATE) && row.containsKey(PREMIUM) && row.containsKey(COUNTERPARTY)) {
      LocalDate tradeDate = getDateWithException(row, TRADE_DATE);
      ExternalId counterpartyId = ExternalId.of(Counterparty.DEFAULT_SCHEME, getWithException(row, COUNTERPARTY));
      ManageableTrade result = 
          new ManageableTrade(
              position.getQuantity(), 
              security.getExternalIdBundle(), 
              tradeDate, 
              OffsetTime.of(11, 11, ZoneOffset.UTC), 
              counterpartyId);
      return result;
     
    } else {
      return null;
    }
    
  }
  
  @Override
  public String[] getColumns() {
    return _columns;
  }

}
