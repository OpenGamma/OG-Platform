/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader.portfolio;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.loader.LoaderContext;
import com.opengamma.financial.loader.rowparser.RowParser;
import com.opengamma.financial.loader.sheet.SheetWriter;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;

public class SingleSheetPortfolioWriter implements PortfolioWriter {

  /** Path strings for constructing a fully qualified parser class name **/
  private static final String CLASS_PREFIX = "com.opengamma.financial.loader.rowparser.";
  private static final String CLASS_POSTFIX = "Parser";

  private SheetWriter _sheet;
  private Map<String, String> _outputRow;
  private RowParser _parser;
  
//  private String[] _path;
  
  public SingleSheetPortfolioWriter(SheetWriter sheet) {
        
    _outputRow = new HashMap<String, String>();
    _sheet = sheet;
    // Pre-load all the parsers
  }

  @Override
  public ManageableSecurity writeSecurity(ManageableSecurity security) {
    
    // maybe write from here to local securities list and flush eveything in flush()????
    
    //Security security = position.getSecurityLink().resolve(_securitySource);
    //String className = security.getClass().toString();
    //className = className.substring(className.lastIndexOf('.'));
    //className = CLASS_PREFIX + className + CLASS_POSTFIX;
    
    //RowParser parser = null;
    //ManageableSecurity manageableSecurity = null;
    
    _outputRow.putAll(_parser.constructRow(security));

    return security;
  }

  @Override
  public ManageablePosition writePosition(ManageablePosition position) {

    _outputRow.putAll(_parser.constructRow(position));

    _sheet.writeNextRow(_outputRow);
    _outputRow = new HashMap<String, String>(); 
    
    // or one row for each trade???
    
    return position;
  }

  @Override
  public ManageablePortfolio getPortfolio() {
    return null;
  }

  @Override
  public ManageablePortfolioNode getCurrentNode() {
    return null;
  }

  @Override
  public ManageablePortfolioNode setCurrentNode(ManageablePortfolioNode node) {
    return node;
  }

  @Override
  public void flush() {
    // use flush to combine position and securities into a single row?
  }

}
