/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio.reader;

import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.integration.copier.portfolio.rowparser.JodaBeanRowParser;
import com.opengamma.integration.copier.portfolio.rowparser.RowParser;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.integration.copier.sheet.reader.SheetReader;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * A simple portfolio reader assumes that the input sheet only contains one asset class, and may also be used as a base
 * class for specific asset class loaders that follow this rule.
 */
public class SingleSheetSimplePositionReader extends SingleSheetPositionReader {

  private static final Logger s_logger = LoggerFactory.getLogger(SingleSheetSimplePositionReader.class);

  /*
   * Load one or more parsers for different types of securities/trades/whatever here
   */
  private RowParser _rowParser;
  /*
   * Specify column order and names here (optional, may be inferred from sheet headers instead)
   */
  private String[] _columns;

  public SingleSheetSimplePositionReader(SheetReader sheet, RowParser rowParser) {
    super(sheet);
    
    ArgumentChecker.notNull(rowParser, "rowParser");

    _columns = getSheet().getColumns();
    _rowParser = rowParser;
  }

  public SingleSheetSimplePositionReader(SheetReader sheet, String securityClass) {
    this(sheet, JodaBeanRowParser.newJodaBeanRowParser(securityClass));    
  }

  public SingleSheetSimplePositionReader(SheetFormat sheetFormat, InputStream inputStream, RowParser rowParser) {
    this(SheetReader.newSheetReader(sheetFormat, inputStream), rowParser);
  }

  public SingleSheetSimplePositionReader(SheetFormat sheetFormat, InputStream inputStream, String securityClass) {
    this(SheetReader.newSheetReader(sheetFormat, inputStream), securityClass);    
  }

  public SingleSheetSimplePositionReader(String filename, RowParser rowParser) {
    this(SheetReader.newSheetReader(filename), rowParser);
  }

  public SingleSheetSimplePositionReader(String filename, String securityClass) {
    this(SheetReader.newSheetReader(filename), securityClass);
  }
  
  @Override
  public ObjectsPair<ManageablePosition, ManageableSecurity[]> readNext() {
    
    Map<String, String> row = getSheet().loadNextRow();    
    if (row == null) {
      return null;
    }
    
    // Build the underlying security
    ManageableSecurity[] securities = _rowParser.constructSecurity(row);
    if (securities != null && securities.length > 0 && securities[0] != null) {
      
      // Build the position and trade(s) using security[0] (underlying)
      ManageablePosition position = _rowParser.constructPosition(row, securities[0]);      
      if (position != null) {
        ManageableTrade trade = _rowParser.constructTrade(row, securities[0], position);
        if (trade != null) { 
          position.addTrade(trade);
        }
      }
      return ObjectsPair.of(position, securities);
      
    } else {
      s_logger.warn("Row parser was unable to construct a security from row " + row);
      return ObjectsPair.of(null, null);
    }
    
  }

  public String[] getColumns() {
    return _columns;
  }

  @Override
  public String[] getCurrentPath() {
    return new String[0];
  }

  @Override
  public void close() {
    getSheet().close();
  }

  @Override
  public String getPortfolioName() {
    return null;
  }
}
