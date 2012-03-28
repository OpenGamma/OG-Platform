/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.loadsave.portfolio.writer;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.integration.loadsave.portfolio.rowparser.RowParser;
import com.opengamma.integration.loadsave.sheet.SheetFormat;
import com.opengamma.integration.loadsave.sheet.writer.SheetWriter;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * This class writes a portfolio that might contain multiple security types into a single sheet. The columns are
 * established from the set of row parsers that are supplied to the constructor.
 */
public class SingleSheetPortfolioWriter implements PortfolioWriter {

  private SheetWriter _sheet;
  private Map<String, RowParser> _parserMap = new HashMap<String, RowParser>();
  
  // current row context
  private Map<String, String> _currentRow  = new HashMap<String, String>();
  private RowParser _currentParser;
  
  private ManageablePortfolioNode _currentNode;
  private ManageablePortfolio _portfolio;

  
  public SingleSheetPortfolioWriter(SheetWriter sheet, Map<String, RowParser> rowParsers) {
    
    ArgumentChecker.notNull(sheet, "sheet");
    ArgumentChecker.notNull(rowParsers, "rowParsers");

    _parserMap = rowParsers;

    // create virtual manageable portfolio
    _currentNode = new ManageablePortfolioNode("Root");
    _portfolio = new ManageablePortfolio("Portfolio", _currentNode);
    _currentNode.setPortfolioId(_portfolio.getUniqueId());
    
    _sheet = sheet;
  }
    
  public SingleSheetPortfolioWriter(SheetWriter sheet, String[] securityTypes) {
    this(sheet, getParsers(securityTypes));    
  }

  public SingleSheetPortfolioWriter(SheetFormat sheetFormat, OutputStream outputStream, Map<String, RowParser> rowParsers) {
    this(SheetWriter.newSheetWriter(sheetFormat, outputStream, getColumns(rowParsers)), rowParsers);
  }  

  public SingleSheetPortfolioWriter(SheetFormat sheetFormat, OutputStream outputStream, String[] securityTypes) {
    this(SheetWriter.newSheetWriter(sheetFormat, outputStream, getColumns(getParsers(securityTypes))), getParsers(securityTypes));
  }
  
  public SingleSheetPortfolioWriter(String filename, Map<String, RowParser> rowParsers) {
    this(SheetWriter.newSheetWriter(filename, getColumns(rowParsers)), rowParsers);
  }
  
  public SingleSheetPortfolioWriter(String filename, String[] securityTypes) {
    this(filename, getParsers(securityTypes));
  }
  

  @Override
  public ManageableSecurity writeSecurity(ManageableSecurity security) {
    
    ArgumentChecker.notNull(security, "security");
    
    String className = security.getClass().toString();
    className = className.substring(className.lastIndexOf('.') + 1).replace("Security", "");
    if ((_currentParser = _parserMap.get(className)) != null) { //CSIGNORE
      _currentRow.putAll(_currentParser.constructRow(security));
    }
    
    return security;
  }

  @Override
  public ManageablePosition writePosition(ManageablePosition position) {

    ArgumentChecker.notNull(position, "position");
    
    if (_currentParser != null) {
      _currentRow.putAll(_currentParser.constructRow(position));
    }
    
    // Flush out the current row and prepare a new one
    if (!_currentRow.isEmpty()) {
      _sheet.writeNextRow(_currentRow);
      _currentRow = new HashMap<String, String>();
    }
    
    return position;
  }

  @Override
  public void flush() {
    _sheet.flush();
  }

  public void close() {
    flush();
    _sheet.close();
  }

  private static Map<String, RowParser> getParsers(String[] securityTypes) {
    Map<String, RowParser> rowParsers = new HashMap<String, RowParser>();
    if (securityTypes != null) {
      for (String s : securityTypes) {
        rowParsers.put(s, RowParser.newRowParser(s));
      }
    }
    return rowParsers;
  }

  private static String[] getColumns(Map<String, RowParser> rowParsers) {
    Set<String> columns = new HashSet<String>();
    for (RowParser rowParser : rowParsers.values()) {      
      // Combine columns from supplied row parsers
      for (String column : rowParser.getColumns()) {
        columns.add(column);
      }
    }
    return columns.toArray(new String[columns.size()]);
  }

  @Override
  public void setPath(String[] newPath) {
    // Nothing to do here (a specialised subclass might add a 'path' column to store the current path for each row)
  }

  @Override
  public String[] getCurrentPath() {
    return new String[] {};
  }

}
