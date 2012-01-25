/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.examples.portfolioloader;

import java.math.BigDecimal;
import java.util.Map;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.tuple.Triple;

/**
 * Demo portfolio loader class
 */
public class EquityFuturePortfolioLoader extends PortfolioLoader {

  /*
   * Load one or more parsers for different types of securities/trades/whatever here
   */
  private final RowParser _equityFutureParser = new EquityFutureParser();

  /*
   * Specify column order and names here (optional, may be inferred from sheet headers instead)
   */
  private final String[] _columns = { 
    EquityFutureParser.NAME, 
    EquityFutureParser.NUMBER_OF_CONTRACTS, 
    EquityFutureParser.REFERENCE_PRICE, 
    EquityFutureParser.CURRENCY, 
    EquityFutureParser.TRADING_EXCHANGE, 
    EquityFutureParser.SETTLEMENT_EXCHANGE, 
    EquityFutureParser.UNIT_AMOUNT, 
    EquityFutureParser.TRADE_DATE, 
    EquityFutureParser.EXPIRY, 
    EquityFutureParser.SETTLEMENT_DATE, 
    EquityFutureParser.UNDERLYING_ID, 
    EquityFutureParser.BBG_CODE
  };
  
  public EquityFuturePortfolioLoader(SheetReader sheet) {
    super(sheet);
  }

  public String[] getColumns() {
    return _columns;
  }
  
  /*
   * This method builds a trade, position and/or security from the parsed row, using one or more parsers
   */
  @Override
  public Triple<ManageableTrade, ManageablePosition, ManageableSecurity> loadNext() {
    
    Map<String, String> row = getSheet().loadNextRow();

    if (row != null) {
      System.out.print("Read in: ");
      for (String s : row.keySet()) {
        System.out.print(s + ": " + row.get(s) + " | ");
      }
      System.out.println();
      
      ManageableSecurity security = _equityFutureParser.constructSecurity(row);
      ManageableTrade trade = _equityFutureParser.constructTrade(row, security);
      ManageablePosition position = new ManageablePosition(BigDecimal.ONE, security.getExternalIdBundle());
      position.addTrade(trade);
      
      return new Triple<ManageableTrade, ManageablePosition, ManageableSecurity>(trade, position, security);
    } else {
      return null;
    }
  }

}
