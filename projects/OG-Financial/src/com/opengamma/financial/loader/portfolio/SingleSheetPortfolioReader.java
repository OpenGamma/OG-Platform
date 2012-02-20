/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader.portfolio;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.loader.PortfolioLoaderTool;
import com.opengamma.financial.loader.sheet.SheetReader;

/**
 * Abstract class for importing data from various 3rd party file formats
 */
public abstract class SingleSheetPortfolioReader implements PortfolioReader {
 
  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioLoaderTool.class);

  private SheetReader _sheet;         // The spreadsheet from which to import
     
  public SingleSheetPortfolioReader(SheetReader sheet) {
    _sheet = sheet;
  }
    
  public SheetReader getSheet() {
    return _sheet;
  }

  public void setSheet(SheetReader sheet) {
    _sheet = sheet;
  }
  
  protected void prettyPrintRow(Map<String, String> row) {
    String out = "Read in: | ";
    for (String s : row.keySet()) {
      out += (s + "=" + row.get(s) + " | ");
    }
    s_logger.info(out);
  }

}
