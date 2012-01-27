/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.examples.portfolioloader.loaders;

import java.math.BigDecimal;
import java.util.Map;

import com.opengamma.examples.portfolioloader.SingleSheetPortfolioLoader;
import com.opengamma.examples.portfolioloader.MasterPortfolioWriter;
import com.opengamma.examples.portfolioloader.RowParser;
import com.opengamma.examples.portfolioloader.SheetReader;
import com.opengamma.examples.portfolioloader.SimplePortfolioLoader;
import com.opengamma.examples.portfolioloader.parsers.EquityFutureParser;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Demo portfolio loader class
 */
public class EquityFuturePortfolioLoader extends SimplePortfolioLoader {

  /*
   * Load one or more parsers for different types of securities/trades/whatever here
   */
//  private final RowParser _rowParser = new EquityFutureParser();

  /*
   * Specify column order and names here (optional, may be inferred from sheet headers instead)
   */
  private static final String[] COLUMNS = { 
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
    super(sheet, new EquityFutureParser(), COLUMNS);
  }

//  @Override
//  public String[] getColumns() {
//    return _columns;
//  }
//  
//  @Override
//  public void writeTo(PortfolioWriter portfolioWriter) {
//    
//    Map<String, String> row;
//
//    // Test portfolio node creation
//    ManageablePortfolioNode node = portfolioWriter.getCurrentNode();
//    ManageablePortfolioNode newNode = new ManageablePortfolioNode("Test Node");
//    node.addChildNode(newNode);
//    portfolioWriter.setCurrentNode(newNode);
//    
//    // Get the next row from the sheet
//    while ((row = getSheet().loadNextRow()) != null) {
//    
//      prettyPrintRow(row);
//      
//      // Build the underlying security
//      ManageableSecurity security = _rowParser.constructSecurity(row)[0];
//      
//      // Attempt to write security and obtain the correct security (either newly written or original)
//      security = portfolioWriter.writeSecurity(security);
//
//      // Build the position and trade(s)
//      ManageablePosition position = new ManageablePosition(BigDecimal.ONE, security.getExternalIdBundle());
//      ManageableTrade trade = _rowParser.constructTrade(row, security, position);
//      position.addTrade(trade);
//      
//      // Write positions/trade(s) to masters (trades are implicitly written with the position)
//      portfolioWriter.writePosition(position);
//    }
//    
//  }
  
}
