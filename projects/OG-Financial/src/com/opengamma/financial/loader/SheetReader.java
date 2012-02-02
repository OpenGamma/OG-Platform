/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader;

import java.util.Map;

/**
 * An abstract table class for importing portfolio data from spreadsheets
 */
public abstract class SheetReader {
  
  private String[] _columns; // The column names and order
  
  public abstract Map<String, String> loadNextRow();

  protected String[] getColumns() {
    return _columns;
  }

  protected void setColumns(String[] columns) {
    _columns = columns;
  }
  
}
