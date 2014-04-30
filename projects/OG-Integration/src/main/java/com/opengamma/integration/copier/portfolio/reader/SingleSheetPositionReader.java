/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.copier.portfolio.reader;

import com.opengamma.integration.copier.sheet.reader.SheetReader;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract class for importing data from various 3rd party file formats
 */
public abstract class SingleSheetPositionReader implements PositionReader {
 
  private SheetReader _sheet;         // The spreadsheet from which to import
     
  public SingleSheetPositionReader(SheetReader sheet) {
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

  @Override
  public String getPortfolioName() {
    return null;
  }
}
