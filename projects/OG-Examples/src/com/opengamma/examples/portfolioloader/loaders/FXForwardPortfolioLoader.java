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
import com.opengamma.examples.portfolioloader.parsers.FXForwardParser;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;

public class FXForwardPortfolioLoader extends SimplePortfolioLoader {

  /*
   * Load one or more parsers for different types of securities/trades/whatever here
   */
//  private final RowParser _rowParser = new FXForwardParser();

  /*
   * Specify column order and names here (optional, may be inferred from sheet headers instead)
   */
  private static final String[] COLUMNS = { 
    FXForwardParser.PAY_CURRENCY,
    FXForwardParser.PAY_AMOUNT,
    FXForwardParser.RECEIVE_CURRENCY,
    FXForwardParser.RECEIVE_AMOUNT,
    FXForwardParser.COUNTRY,
    FXForwardParser.FORWARD_DATE
  };

  public FXForwardPortfolioLoader(SheetReader sheet) {
    super(sheet, new FXForwardParser(), COLUMNS);
  }
//
//  @Override
//  public void writeTo(PortfolioWriter portfolioWriter) {
//    Map<String, String> row;
//
//    // Get the next row from the sheet
//    while ((row = getSheet().loadNextRow()) != null) {
//    
//      prettyPrintRow(row);
//      
//      // Build the underlying security
//      ManageableSecurity[] security = _rowParser.constructSecurity(row);
//      
//      // Attempt to write securities and obtain the correct security (either newly written or original)
//      // Write array in reverse order as underlying is at position 0
//      for (int i = security.length - 1; i >= 0; i--) {
//        security[i] = portfolioWriter.writeSecurity(security[i]);        
//      }
//
//      // Build the position and trade(s)
//      ManageablePosition position = new ManageablePosition(BigDecimal.ONE, security[0].getExternalIdBundle());
//      ManageableTrade trade = _rowParser.constructTrade(row, security[0], position);
//      if (trade != null) { 
//        position.addTrade(trade);
//      }
//      
//      // Write positions/trade(s) to masters (trades are implicitly written with the position)
//      portfolioWriter.writePosition(position);
//    }
//  }
//
//  @Override
//  public String[] getColumns() {
//    return _columns;
//  }

}
