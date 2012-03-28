/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.loadsave.portfolio.reader;

import com.opengamma.integration.loadsave.sheet.reader.SheetReader;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract class for importing data from various 3rd party file formats
 */
public abstract class SingleSheetPortfolioReader implements PortfolioReader {
 
  private SheetReader _sheet;         // The spreadsheet from which to import
     
  public SingleSheetPortfolioReader(SheetReader sheet) {
    ArgumentChecker.notNull(sheet, "sheet");
    _sheet = sheet;
  }
    
  public SheetReader getSheet() {
    return _sheet;
  }

  public void setSheet(SheetReader sheet) {
    ArgumentChecker.notNull(sheet, "sheet");
    _sheet = sheet;
  }
  
}
