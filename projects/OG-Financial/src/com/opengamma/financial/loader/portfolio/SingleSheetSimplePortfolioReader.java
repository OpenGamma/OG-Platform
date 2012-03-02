/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.loader.portfolio;

import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.loader.rowparser.RowParser;
import com.opengamma.financial.loader.sheet.SheetReader;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;

/**
 * A simple portfolio reader assumes that the input sheet only contains one asset class, and may also be used as a base
 * class for specific asset class loaders that follow this rule.
 */
public class SingleSheetSimplePortfolioReader extends SingleSheetPortfolioReader {

  /*
   * Load one or more parsers for different types of securities/trades/whatever here
   */
  private RowParser _rowParser;
  /*
   * Specify column order and names here (optional, may be inferred from sheet headers instead)
   */
  private String[] _columns;

  public SingleSheetSimplePortfolioReader(String filename, RowParser rowParser) {
    super(SheetReader.newSheetReader(filename));
    _columns = getSheet().getColumns();
    _rowParser = rowParser;
  }

  public SingleSheetSimplePortfolioReader(SheetReader sheet, String[] columns, RowParser rowParser) {
    super(sheet);    
    _columns = getSheet().getColumns();
    _rowParser = rowParser;
  }

  public SingleSheetSimplePortfolioReader(String filename, String securityClass, ToolContext toolContext) {
    super(SheetReader.newSheetReader(filename));
    _columns = getSheet().getColumns();
    _rowParser = RowParser.newRowParser(securityClass, toolContext);
    if (_rowParser == null) {
      throw new OpenGammaRuntimeException("Could not identify an appropriate row parser for security class " + securityClass);
    }
  }

  public SingleSheetSimplePortfolioReader(SheetReader sheet, String[] columns, String securityClass, ToolContext toolContext) {
    super(sheet);
    _columns = getSheet().getColumns();
    _rowParser = RowParser.newRowParser(securityClass, toolContext);
    if (_rowParser == null) {
      throw new OpenGammaRuntimeException("Could not identify an appropriate row parser for security class " + securityClass);
    }
  }

  @Override
  public void writeTo(PortfolioWriter portfolioWriter) {
    Map<String, String> row;

    // Get the next row from the sheet
    while ((row = getSheet().loadNextRow()) != null) {
    
      // Print debugging output
      // prettyPrintRow(row);
      
      // Build the underlying security
      ManageableSecurity[] security = _rowParser.constructSecurity(row);
      
      // Attempt to write securities and obtain the correct security (either newly written or original)
      // Write array in reverse order as underlying is at position 0
      for (int i = security.length - 1; i >= 0; i--) {
        security[i] = portfolioWriter.writeSecurity(security[i]);        
      }

      // Build the position and trade(s) using security[0] (underlying)
      ManageablePosition position = _rowParser.constructPosition(row, security[0]);
      
      ManageableTrade trade = _rowParser.constructTrade(row, security[0], position);
      if (trade != null) { 
        position.addTrade(trade);
      }
      
      // Write positions/trade(s) to masters (trades are implicitly written with the position)
      portfolioWriter.writePosition(position);
    }
  }

  public String[] getColumns() {
    return _columns;
  }

}
