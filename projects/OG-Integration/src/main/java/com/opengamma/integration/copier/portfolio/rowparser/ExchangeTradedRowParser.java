/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio.rowparser;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.provider.security.SecurityProvider;
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
  
  private SecurityProvider _securityProvider;

  public ExchangeTradedRowParser(SecurityProvider securityProvider) {
    ArgumentChecker.notNull(securityProvider, "securityProvider");
    _securityProvider = securityProvider;
  }

  private static final ExternalScheme[] s_schemeWaterfall = {
    ExternalSchemes.BLOOMBERG_TICKER,
    ExternalSchemes.BLOOMBERG_TCM,
    ExternalSchemes.BLOOMBERG_BUID,
    ExternalSchemes.CUSIP,
    ExternalSchemes.ISIN
  };
  
  
  @Override
  public ManageableSecurity[] constructSecurity(Map<String, String> row) {
    ArgumentChecker.notNull(row, "row");
    
    for (ExternalScheme scheme : s_schemeWaterfall) {
      ExternalIdBundle id = ExternalId.of(scheme, getWithException(row, TICKER)).toBundle();
      Security security = _securityProvider.getSecurity(id);
      if (security != null && security instanceof ManageableSecurity) {
        return new ManageableSecurity[] {(ManageableSecurity) security};
      }
    }
    return new ManageableSecurity[] {};
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
              LocalTime.of(11, 11).atOffset(ZoneOffset.UTC), 
              counterpartyId);
      return result;
     
    } else {
      return null;
    }
    
  }

  @Override
  public Map<String, String> constructRow(ManageableTrade trade) {
    Map<String, String> map = new HashMap<String, String>();
    addValueIfNotNull(map, QUANTITY, trade.getQuantity());
    addValueIfNotNull(map, TRADE_DATE, trade.getTradeDate());
    addValueIfNotNull(map, PREMIUM, trade.getPremium());
    return map;
  }

  @Override
  public Map<String, String> constructRow(ManageablePosition position) {
    BigDecimal quantity = position.getQuantity();
    if (quantity != null) {
      return Collections.singletonMap(QUANTITY, quantity.toString());
    }
    return null;
  }

  @Override
  public Map<String, String> constructRow(ManageableSecurity[] securities) {
    if (securities.length < 1) {
      return null;
    }
    String ticker = securities[0].getExternalIdBundle().getValue(ExternalSchemes.BLOOMBERG_TICKER);
    if (ticker != null) {
      return Collections.singletonMap(TICKER, ticker);
    }
    return null;
  }

  @Override
  public String[] getColumns() {
    return _columns;
  }

}
