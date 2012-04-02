/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio.writer;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.integration.copier.portfolio.rowparser.JodaBeanRowParser;
import com.opengamma.integration.copier.portfolio.rowparser.RowParser;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.integration.copier.sheet.writer.SheetWriter;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Writes positions of a single security type to a single sheet
 */
public class SingleSheetSimplePortfolioWriter extends SingleSheetPortfolioWriter {

  private static final Logger s_logger = LoggerFactory.getLogger(SingleSheetSimplePortfolioWriter.class);

  private RowParser _rowParser;
  
  // current row context
  private Map<String, String> _currentRow  = new HashMap<String, String>();
  
  private ManageablePortfolioNode _currentNode;
  private ManageablePortfolio _portfolio;

  public SingleSheetSimplePortfolioWriter(SheetWriter sheet, RowParser rowParser) {
    super(sheet);
    
    ArgumentChecker.notNull(rowParser, "rowParser");
    _rowParser = rowParser;

    // create virtual manageable portfolio
    _currentNode = new ManageablePortfolioNode("Root");
    _portfolio = new ManageablePortfolio("Portfolio", _currentNode);
    _currentNode.setPortfolioId(_portfolio.getUniqueId());
  }

  public SingleSheetSimplePortfolioWriter(SheetWriter sheet, String securityType) {
    this(sheet, JodaBeanRowParser.newJodaBeanRowParser(securityType));    
  }

  public SingleSheetSimplePortfolioWriter(SheetFormat sheetFormat, OutputStream outputStream, RowParser rowParser) {
    this(SheetWriter.newSheetWriter(sheetFormat, outputStream, rowParser.getColumns()), rowParser);
  }  
 
  public SingleSheetSimplePortfolioWriter(String filename, RowParser rowParser) {
    this(SheetWriter.newSheetWriter(filename, rowParser.getColumns()), rowParser);
  }
  
  public SingleSheetSimplePortfolioWriter(String filename, String securityType) {
    this(filename, JodaBeanRowParser.newJodaBeanRowParser(securityType));
  }
  
  public SingleSheetSimplePortfolioWriter(SheetFormat sheetFormat, OutputStream outputStream, String securityType) {
    this(sheetFormat, outputStream, JodaBeanRowParser.newJodaBeanRowParser(securityType));
  }

  @Override
  public ManageableSecurity writeSecurity(ManageableSecurity security) {
    ArgumentChecker.notNull(security, "security");
    
    _currentRow.putAll(_rowParser.constructRow(security));
    
    return security;
  }

  @Override
  public ManageablePosition writePosition(ManageablePosition position) {
    ArgumentChecker.notNull(position, "position");
    
    if (_rowParser != null) {
      _currentRow.putAll(_rowParser.constructRow(position));
      
      List<ManageableTrade> trades = position.getTrades();
      if (trades.size() > 1) {
        s_logger.warn("Omitting extra trades: only one trade per position is currently supported");
      }
      if (trades.size() > 0) {
        _currentRow.putAll(_rowParser.constructRow(trades.get(0)));
      }
    }
    
    // Flush out the current row and prepare a new one
    if (!_currentRow.isEmpty()) {
      getSheet().writeNextRow(_currentRow);
      _currentRow = new HashMap<String, String>();
    }
    
    return position;
  }

  @Override
  public String[] getCurrentPath() {
    return new String[] {};
  }

  @Override
  public void setPath(String[] newPath) {
    // Nothing to do
  }

}
