/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader.portfolio;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import com.opengamma.financial.loader.rowparser.RowParser;
import com.opengamma.financial.loader.sheet.SheetWriter;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;

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

  
  public SingleSheetPortfolioWriter(String filename, String[] securityTypes, ToolContext toolContext) {
    
    Map<String, RowParser> rowParsers = new HashMap<String, RowParser>();
    for (String s : securityTypes) {
      rowParsers.put(s, RowParser.newRowParser(s, toolContext));
    }
    Set<String> columns = initParsers(rowParsers);
    
    // create virtual manageable portfolio
    _currentNode = new ManageablePortfolioNode("Root");
    _portfolio = new ManageablePortfolio(filename, _currentNode);
    _currentNode.setPortfolioId(_portfolio.getUniqueId());
    
    _sheet = SheetWriter.newSheetWriter(filename, columns.toArray(new String[columns.size()]));    
  }
  
  public SingleSheetPortfolioWriter(String filename, Map<String, RowParser> rowParsers) {
    
    Set<String> columns = initParsers(rowParsers);
    
    // create virtual manageable portfolio
    _currentNode = new ManageablePortfolioNode("Root");
    _portfolio = new ManageablePortfolio(filename, _currentNode);
    _currentNode.setPortfolioId(_portfolio.getUniqueId());
    
    _sheet = SheetWriter.newSheetWriter(filename, columns.toArray(new String[columns.size()]));
  }
  
  public SingleSheetPortfolioWriter(SheetWriter sheet, Map<String, RowParser> rowParsers) {
    
    initParsers(rowParsers);
    
    // create virtual manageable portfolio
    _currentNode = new ManageablePortfolioNode("Root");
    _portfolio = new ManageablePortfolio("Virtual Portfolio", _currentNode);
    _currentNode.setPortfolioId(_portfolio.getUniqueId());
    
    _sheet = sheet;
  }
  
  @Override
  public ManageableSecurity writeSecurity(ManageableSecurity security) {
    
    String className = security.getClass().toString();
    className = className.substring(className.lastIndexOf('.') + 1).replace("Security", "");
    if ((_currentParser = _parserMap.get(className)) != null) { //CSIGNORE
      _currentRow.putAll(_currentParser.constructRow(security));
    }
    
    return security;
  }

  @Override
  public ManageablePosition writePosition(ManageablePosition position) {

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
  public ManageablePortfolio getPortfolio() {
    return _portfolio;
  }

  @Override
  public ManageablePortfolioNode getCurrentNode() {
    return _currentNode;
  }

  @Override
  public ManageablePortfolioNode setCurrentNode(ManageablePortfolioNode node) {
    _currentNode = node;
    return node;
  }

  @Override
  public void flush() {
    _sheet.flush();
  }

  public void close() {
    flush();
    _sheet.close();
  }

  private Set<String> initParsers(Map<String, RowParser> rowParsers) {
    
    _parserMap = rowParsers;
    
    Set<String> columns = new HashSet<String>();
    for (RowParser rowParser : rowParsers.values()) {      
      // Combine columns from supplied row parsers
      for (String column : rowParser.getColumns()) {
        columns.add(column);
      }
    }
    return columns;
  }

}
